package com.burcak.mybank.services;

import com.burcak.mybank.entities.*;
import com.burcak.mybank.models.*;
import com.burcak.mybank.repositories.AccountRepository;
import com.burcak.mybank.repositories.CreditCardReceiptRepository;
import com.burcak.mybank.repositories.CreditCardRepository;
import com.burcak.mybank.repositories.CustomerRepository;
import com.burcak.mybank.client.ExchangeAPI;
import com.burcak.mybank.entities.*;
import com.burcak.mybank.exception.AccountException;
import com.burcak.mybank.exception.CreditCardException;
import com.burcak.mybank.exception.CustomerException;
import com.burcak.mybank.exception.ExceptionMessages;
import com.burcak.mybank.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreditCardService {

    private static final int CREDIT_CARD_EXPIRED_DURATION = 2;
    private static final int SPENDING_LIMIT = 2000;
    private static final int RECEIPT_DAY = 1;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CreditCardReceiptRepository receiptRepository;

    @Autowired
    ExchangeAPI exchangeAPI;

    public CreateCreditCardResponse createCreditCard(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerException(ExceptionMessages.CUSTOMER_NOT_EXIST));
        if (customer.getCreditCard() != null) {
            throw new CustomerException(ExceptionMessages.CUSTOMER_HAS_CREDIT_CARD);
        }
        CreditCard creditCard = new CreditCard();
        creditCard.setCustomer(customer);
        creditCard.setBalance(0);
        creditCard.setCardHolder(customer.getFullName());
        creditCard.setCvvNumber(generateCvvNumber());
        creditCard.setCardNumber(generateCardNumber());
        creditCard.setExpiredMonth(LocalDate.now().getMonthValue());
        creditCard.setExpiredYear(LocalDate.now().getYear() + CREDIT_CARD_EXPIRED_DURATION);
        creditCard.setSpendingLimit(SPENDING_LIMIT);
        creditCardRepository.save(creditCard);
        return setResponse(creditCard);
    }

    private CreateCreditCardResponse setResponse(CreditCard creditCard) {
        CreateCreditCardResponse response = new CreateCreditCardResponse();
        response.setCardHolder(creditCard.getCardHolder());
        response.setCardNumber(creditCard.getCardNumber());
        response.setCvvNumber(creditCard.getCvvNumber());
        response.setExpiredMonth(creditCard.getExpiredMonth());
        response.setExpiredYear(creditCard.getExpiredYear());
        response.setBalance(creditCard.getBalance());
        response.setSpendingLimit(creditCard.getSpendingLimit());
        return response;
    }

    private long generateCardNumber() {
        return (long) (Math.random() * 10000000000000000L);
    }

    private int generateCvvNumber() {
        return (int) (Math.random() * (999 - 100 + 1) + 100);
    }


    public GetCreditCardLoanResponse inquireLoan(Long id) {
        CreditCard creditCard = creditCardRepository.findById(id).orElseThrow(() -> new CreditCardException(ExceptionMessages.CREDIT_CARD_NOT_EXIST));
        GetCreditCardLoanResponse response = new GetCreditCardLoanResponse();
        response.setCardNumber(creditCard.getCardNumber());
        response.setSpendingLimit(creditCard.getSpendingLimit());
        response.setLoan(creditCard.getBalance());
        return response;
    }


    public void shoppingByCreditCard(ShoppingRequest request) {
        CreditCard creditCard = creditCardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new CreditCardException(ExceptionMessages.CREDIT_CARD_NOT_EXIST));
        validationCreditCard(request, creditCard);
        double amount = creditCard.getSpendingLimit() - request.getAmount();
        creditCard.setSpendingLimit(amount);
        double balance = creditCard.getBalance() - request.getAmount();
        creditCard.setBalance(balance);
        creditCardRepository.save(creditCard);
        saveReceipt(creditCard.getId(), -request.getAmount(), ProcessName.SHOPPING);
    }

    private void validationCreditCard(ShoppingRequest request, CreditCard creditCard) {
        if (!checkCardInformation(request, creditCard)) {
            throw new CreditCardException(ExceptionMessages.CARD_INFORMATION);
        }
        if (isExpired(creditCard)) {
            throw new CreditCardException(ExceptionMessages.CARD_EXPIRED);
        }
        if (creditCard.getSpendingLimit() < request.getAmount()) {
            throw new CreditCardException(ExceptionMessages.NOT_ENOUGH_BALANCE);
        }
    }

    private boolean isExpired(CreditCard creditCard) {
        if (creditCard.getExpiredYear() < LocalDate.now().getYear())
            return true;
        else
            return creditCard.getExpiredYear() == LocalDate.now().getYear() && creditCard.getExpiredMonth() > LocalDate.now().getMonthValue();
    }

    private boolean checkCardInformation(ShoppingRequest request, CreditCard creditCard) {
        return request.getCvvNumber() == creditCard.getCvvNumber() && request.getCardHolderName().equals(creditCard.getCardHolder())
                && request.getExpiredMonth() == creditCard.getExpiredMonth() && request.getExpiredYear() == creditCard.getExpiredYear();
    }

    public void payLoanFromAccount(PayLoanFromAccountRequest request) {
        CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId()).orElseThrow(() -> new CreditCardException(ExceptionMessages.CREDIT_CARD_NOT_EXIST));
        Account account = accountRepository.findByIban(request.getIban()).orElseThrow(() -> new AccountException(ExceptionMessages.ACCOUNT_NOT_EXIST));
        if (account.getAccountType().equals(AccountType.SAVING)) {
            throw new AccountException(ExceptionMessages.PAY_LOAN_FROM_SAVING_ACCOUNT);
        }
        if (!creditCard.getCustomer().getId().equals(account.getCustomer().getId())) {
            throw new CreditCardException(ExceptionMessages.NOT_SAME_CUSTOMER);
        }
        checkCurrency(creditCard, account, request.getAmount());
    }

    private void checkCurrency(CreditCard creditCard, Account account, double paidAmount) {
        double exchangeAmount;
        if (!account.getCurrency().equals(Currency.TRY)) {
            Exchange exchange = exchangeAPI.exchange(account.getCurrency());
            exchangeAmount = paidAmount / exchange.getRates().getTr();
            setAmount(exchangeAmount, paidAmount, creditCard, account);
        } else {
            setAmount(paidAmount, paidAmount, creditCard, account);
        }
    }

    private void setAmount(double amount, double paidAmount, CreditCard creditCard, Account account) {
        double creditCardAmount;
        double creditCardLimit;
        double accountAmount;
        if (account.getAmount() < amount) {
            throw new AccountException(ExceptionMessages.NOT_ENOUGH_BALANCE);
        }
        accountAmount = account.getAmount() - amount;
        creditCardAmount = creditCard.getBalance() + paidAmount;
        creditCardLimit = creditCard.getSpendingLimit() + paidAmount;
        account.setAmount(accountAmount);
        creditCard.setBalance(creditCardAmount);
        creditCard.setSpendingLimit(creditCardLimit);
        creditCardRepository.save(creditCard);
        accountRepository.save(account);
        saveReceipt(creditCard.getId(), paidAmount, ProcessName.PAY_LOAN_FROM_ACCOUNT);
    }

    public void payLoanFromAtm(Long id, double amount) {
        CreditCard creditCard = creditCardRepository.findById(id).orElseThrow(() -> new CreditCardException(ExceptionMessages.CREDIT_CARD_NOT_EXIST));
        if (isExpired(creditCard)) {
            throw new CreditCardException(ExceptionMessages.CARD_EXPIRED);
        }
        double lastAmount;
        double lastLimit;
        lastAmount = creditCard.getBalance() + amount;
        lastLimit = creditCard.getSpendingLimit() + amount;
        creditCard.setBalance(lastAmount);
        creditCard.setSpendingLimit(lastLimit);
        creditCardRepository.save(creditCard);
        saveReceipt(creditCard.getId(), amount, ProcessName.PAY_LOAN_FROM_ATM);
    }

    public void withdrawMoneyFromAtm(Long id, double amount) {
        CreditCard creditCard = creditCardRepository.findById(id).orElseThrow(() -> new CreditCardException(ExceptionMessages.CREDIT_CARD_NOT_EXIST));
        if (isExpired(creditCard)) {
            throw new CreditCardException(ExceptionMessages.CARD_EXPIRED);
        }
        if (creditCard.getSpendingLimit() < amount) {
            throw new CreditCardException(ExceptionMessages.NOT_ENOUGH_BALANCE);
        }
        double lastAmount = creditCard.getSpendingLimit() - amount;
        creditCard.setSpendingLimit(lastAmount);
        double balance = creditCard.getBalance() - amount;
        creditCard.setBalance(balance);
        creditCardRepository.save(creditCard);
        saveReceipt(creditCard.getId(), -amount, ProcessName.WITHDRAW_MONEY_FROM_ATM);
    }

    public void saveReceipt(Long id, double amount, ProcessName processName) {
        CreditCardReceipt receipt = new CreditCardReceipt();
        receipt.setCreditCardId(id);
        receipt.setProcessName(processName);
        receipt.setDate(LocalDateTime.now().minusMonths(1));
        receipt.setAmount(amount);
        receiptRepository.save(receipt);
    }

    public GetCreditCardReceiptResponse getReceipt(Long id) {
        if (!creditCardRepository.existsById(id)) {
            throw new CreditCardException(ExceptionMessages.CREDIT_CARD_NOT_EXIST);
        }
        List<CreditCardReceipt> receipts = receiptRepository.findByCreditCardIdAndDateGreaterThanEqualAndDateLessThanEqual(
                id, LocalDateTime.now().withDayOfMonth(RECEIPT_DAY).minusMonths(1), LocalDateTime.now().withDayOfMonth(RECEIPT_DAY));
        if (receipts.isEmpty()) {
            throw new CreditCardException(ExceptionMessages.NO_CREDIT_CARD_RECEIPT);
        }
        return setReceipt(receipts);
    }

    private GetCreditCardReceiptResponse setReceipt(List<CreditCardReceipt> receipts) {
        GetCreditCardReceiptResponse receiptResponse = new GetCreditCardReceiptResponse();
        for (CreditCardReceipt creditCardReceipt : receipts) {
            Expenses expenses = new Expenses();
            expenses.setProcessName(creditCardReceipt.getProcessName());
            expenses.setAmount(creditCardReceipt.getAmount());
            expenses.setDateTime(creditCardReceipt.getDate());
            receiptResponse.getExpenses().add(expenses);
        }
        return receiptResponse;
    }
}