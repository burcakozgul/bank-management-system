package org.kodluyoruz.mybank.services;

import org.kodluyoruz.mybank.client.ExchangeAPI;
import org.kodluyoruz.mybank.entities.*;
import org.kodluyoruz.mybank.exception.GeneralException;
import org.kodluyoruz.mybank.models.*;
import org.kodluyoruz.mybank.repositories.AccountRepository;
import org.kodluyoruz.mybank.repositories.CreditCardReceiptRepository;
import org.kodluyoruz.mybank.repositories.CreditCardRepository;
import org.kodluyoruz.mybank.repositories.CustomerRepository;
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

    public CreateCreditCardResponse createCreditCard(Long customerId) throws GeneralException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new GeneralException("Customer id doesn't exist"));
        if (customer.getCreditCard() == null) {
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

            CreateCreditCardResponse response = new CreateCreditCardResponse();
            response.setCardHolder(creditCard.getCardHolder());
            response.setCardNumber(creditCard.getCardNumber());
            response.setCvvNumber(creditCard.getCvvNumber());
            response.setExpiredMonth(creditCard.getExpiredMonth());
            response.setExpiredYear(creditCard.getExpiredYear());
            response.setBalance(creditCard.getBalance());
            response.setSpendingLimit(creditCard.getSpendingLimit());
            return response;
        } else throw new GeneralException("Customer has credit card");
    }

    private long generateCardNumber() {
        return (long) (Math.random() * 10000000000000000L);
    }

    private int generateCvvNumber() {
        return (int) (Math.random() * (999 - 100 + 1) + 100);
    }


    public double inquireLoan(Long id) throws GeneralException {
        CreditCard creditCard = creditCardRepository.findById(id).orElseThrow(() -> new GeneralException("Credit card id doesn't exist!"));
        return creditCard.getBalance();
    }


    public void shoppingByCreditCard(ShoppingRequest request) throws GeneralException {
        CreditCard creditCard = creditCardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new GeneralException("Credit card id doesn't exist!"));
        if (checkCardInformation(request, creditCard)) {
            if (!isExpired(creditCard)) {
                if (creditCard.getSpendingLimit() >= request.getAmount()) {
                    double amount = creditCard.getSpendingLimit() - request.getAmount();
                    creditCard.setSpendingLimit(amount);
                    double balance = creditCard.getBalance() - request.getAmount();
                    creditCard.setBalance(balance);
                    creditCardRepository.save(creditCard);
                    saveReceipt(creditCard.getId(), -request.getAmount(), ProcessName.SHOPPING);
                } else throw new GeneralException("Credit card limit doesn't enough");
            } else throw new GeneralException("Credit card expired!");
        } else throw new GeneralException("Please check credit card information");

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

    public void payLoanFromAccount(PayLoanFromAccountRequest request) throws GeneralException {
        CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId()).orElseThrow(() -> new GeneralException("Credit card id doesn't exist!"));
        Account account = accountRepository.findByIban(request.getIban()).orElseThrow(() -> new GeneralException("Account doesn't exist"));
        if (!account.getAccountType().equals(AccountType.SAVING)) {
            if (creditCard.getCustomer().getId().equals(account.getCustomer().getId())) {
                checkCurrency(creditCard, account, request.getAmount());
            } else throw new GeneralException("Credit card and Account don't belong to same customer.");
        } else throw new GeneralException("You cannot pay from Saving account!");
    }

    private void checkCurrency(CreditCard creditCard, Account account, double paidAmount) throws GeneralException {
        double exchangeAmount;
        if (!account.getCurrency().equals(Currency.TRY)) {
            Exchange exchange = exchangeAPI.exchange(account.getCurrency());
            exchangeAmount = paidAmount / exchange.getRates().getTr();
            setAmount(exchangeAmount, paidAmount, creditCard, account);
        } else {
            setAmount(paidAmount, paidAmount, creditCard, account);
        }
    }

    private void setAmount(double amount, double paidAmount, CreditCard creditCard, Account account) throws GeneralException {
        double creditCardAmount;
        double accountAmount;
        if (account.getAmount() >= amount) {
            accountAmount = account.getAmount() - amount;
            creditCardAmount = creditCard.getBalance() + paidAmount;
            account.setAmount(accountAmount);
            creditCard.setBalance(creditCardAmount);
            creditCardRepository.save(creditCard);
            accountRepository.save(account);
            saveReceipt(creditCard.getId(), paidAmount, ProcessName.PAY_LOAN_FROM_ACCOUNT);
        } else throw new GeneralException("Account amount doesn't enough.");
    }

    public void payLoanFromAtm(Long id, double amount) throws GeneralException {
        CreditCard creditCard = creditCardRepository.findById(id).orElseThrow(() -> new GeneralException("Credit card id doesn't exist!"));
        if (!isExpired(creditCard)){
            double lastAmount;
            lastAmount = creditCard.getBalance() + amount;
            creditCard.setBalance(lastAmount);
            creditCardRepository.save(creditCard);
            saveReceipt(creditCard.getId(), amount, ProcessName.PAY_LOAN_FROM_ATM);
        } else throw new GeneralException("Credit card expired!");
    }

    public void withdrawMoneyFromAtm(Long id, double amount) throws GeneralException {
        CreditCard creditCard = creditCardRepository.findById(id).orElseThrow(() -> new GeneralException("Credit card id doesn't exist!"));
        if (!isExpired(creditCard)) {
            if (creditCard.getSpendingLimit() >= amount) {
                double lastAmount = creditCard.getSpendingLimit() - amount;
                creditCard.setSpendingLimit(lastAmount);
                double balance = creditCard.getBalance() - amount;
                creditCard.setBalance(balance);
                creditCardRepository.save(creditCard);
                saveReceipt(creditCard.getId(), -amount, ProcessName.WITHDRAW_MONEY_FROM_ATM);
            } else throw new GeneralException("Credit card limit doesn't enough");
        } else throw new GeneralException("Credit card expired!");
    }

    public void saveReceipt(Long id, double amount, ProcessName processName) {
        CreditCardReceipt receipt = new CreditCardReceipt();
        receipt.setCreditCardId(id);
        receipt.setProcessName(processName);
        receipt.setDate(LocalDateTime.now());
        receipt.setAmount(amount);
        receiptRepository.save(receipt);
    }

    public GetCreditCardReceiptResponse getReceipt(Long id) throws GeneralException {
        creditCardRepository.findById(id).orElseThrow(() -> new GeneralException("Credit card id doesn't exist!"));
        List<CreditCardReceipt> receipts = receiptRepository.findByCreditCardIdAndDateGreaterThanEqualAndDateLessThanEqual(
                id, LocalDateTime.now().withDayOfMonth(RECEIPT_DAY).minusMonths(1), LocalDateTime.now().withDayOfMonth(RECEIPT_DAY));
        GetCreditCardReceiptResponse receiptResponse = new GetCreditCardReceiptResponse();
        if (!receipts.isEmpty()) {
            for (CreditCardReceipt creditCardReceipt : receipts) {
                Expenses expenses = new Expenses();
                expenses.setProcessName(creditCardReceipt.getProcessName());
                expenses.setAmount(creditCardReceipt.getAmount());
                expenses.setDateTime(creditCardReceipt.getDate());
                receiptResponse.getExpenses().add(expenses);
            }
            return receiptResponse;
        } else throw new GeneralException("no credit card loan in selected month or year");
    }
}
