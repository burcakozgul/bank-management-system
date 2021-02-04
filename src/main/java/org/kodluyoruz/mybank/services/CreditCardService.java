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
    private static final int RECEIPT_DAY=1;

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

    public void createCreditCard(Long customerId) throws GeneralException {
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
                    saveReceipt(creditCard.getId(),-request.getAmount(),ProcessName.SHOPPING);
                } else throw new GeneralException("Credit card limit doesn't enough");
            } else throw new GeneralException("Card Date expired!");
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
        CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId()).orElseThrow(() -> new GeneralException("Credit card doesn't exist!"));
        Account account = accountRepository.findByIban(request.getIban()).orElseThrow(() -> new GeneralException("Account doesn't exist"));
        if (!account.getAccountType().equals(AccountType.SAVING)){
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
            saveReceipt(creditCard.getId(),paidAmount,ProcessName.PAY_LOAN_FROM_ACCOUNT);
        } else throw new GeneralException("Account amount doesn't enough.");
    }

    public void payLoanFromAtm(PayLoanFromAtmRequest request) throws GeneralException {
        double amount;
        CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId()).orElseThrow(() -> new GeneralException("Credit card doesn't exist!"));
        amount = creditCard.getBalance() + request.getAmount();
        creditCard.setBalance(amount);
        creditCardRepository.save(creditCard);
        saveReceipt(creditCard.getId(),request.getAmount(),ProcessName.PAY_LOAN_FROM_ATM);
    }

    public void saveReceipt(Long id, double amount, ProcessName processName){
        CreditCardReceipt receipt = new CreditCardReceipt();
        receipt.setCreditCardId(id);
        receipt.setProcessName(processName);
        receipt.setDate(LocalDateTime.now());
        receipt.setAmount(amount);
        receiptRepository.save(receipt);
    }

    public GetCreditCardReceiptResponse getReceipt(Long id, int month, int year) throws GeneralException {
        creditCardRepository.findById(id).orElseThrow(()->new GeneralException("Credit card id doesn't exist!"));
        List<CreditCardReceipt> receipts = receiptRepository.findByCreditCardIdAndDateGreaterThanEqualAndDateLessThanEqual(
                id,LocalDateTime.of(year,month,RECEIPT_DAY,0, 0),LocalDateTime.of(year,month,RECEIPT_DAY,0,0).plusMonths(1));
        GetCreditCardReceiptResponse receiptResponse = new GetCreditCardReceiptResponse();
        if (!receipts.isEmpty()){
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

//    public GetCreditCardReceiptResponse getExpenses(Long id) throws GeneralException {
//        creditCardRepository.findById(id).orElseThrow(()->new GeneralException("Credit card id doesn't exist!"));
//        List<CreditCardReceipt> receipts= receiptRepository.findByCreditCardIdAndDateGreaterThanEqualAndDateLessThanEqual(
//                id,LocalDateTime.now().withDayOfMonth(1),LocalDateTime.now());
//        GetCreditCardReceiptResponse receiptResponse = new GetCreditCardReceiptResponse();
//        if (!receipts.isEmpty()){
//            for (CreditCardReceipt creditCardReceipt : receipts) {
//                Expenses expenses = new Expenses();
//                expenses.setProcessName(creditCardReceipt.getProcessName());
//                expenses.setAmount(creditCardReceipt.getAmount());
//                expenses.setDateTime(creditCardReceipt.getDate());
//                receiptResponse.getExpenses().add(expenses);
//            }
//            return receiptResponse;
//        } else throw new GeneralException("Don't have credit card loan now from 1 of this month");
//    }
}
