package org.kodluyoruz.mybank.services;

import org.kodluyoruz.mybank.entities.Account;
import org.kodluyoruz.mybank.entities.AccountType;
import org.kodluyoruz.mybank.entities.BankCard;
import org.kodluyoruz.mybank.exception.AccountException;
import org.kodluyoruz.mybank.exception.BankCardException;
import org.kodluyoruz.mybank.exception.ExceptionMessages;
import org.kodluyoruz.mybank.models.CreateBankCardResponse;
import org.kodluyoruz.mybank.models.ShoppingRequest;
import org.kodluyoruz.mybank.repositories.AccountRepository;
import org.kodluyoruz.mybank.repositories.BankCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BankCardService {

    private static final int BANK_CARD_EXPIRED_DURATION = 2;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private AccountRepository accountRepository;

    public CreateBankCardResponse createBankCard(Long accountId) {
        Account account = accountRepository.findByIdAndAccountType(accountId, AccountType.CHECKING).orElseThrow(() ->
                new AccountException(ExceptionMessages.ACCOUNT_NOT_VALID));
        if (account.getBankCard() == null) {
            BankCard bankCard = new BankCard();
            bankCard.setAccount(account);
            bankCard.setCardHolder(account.getCustomer().getFullName());
            bankCard.setCardNumber(generateCard());
            bankCard.setCvvNumber(generateCvvNumber());
            bankCard.setExpiredMonth(LocalDate.now().getMonthValue());
            bankCard.setExpiredYear(LocalDate.now().getYear() + BANK_CARD_EXPIRED_DURATION);
            bankCardRepository.save(bankCard);

            CreateBankCardResponse response = new CreateBankCardResponse();
            response.setCardHolder(bankCard.getCardHolder());
            response.setCardNumber(bankCard.getCardNumber());
            response.setCvvNumber(bankCard.getCvvNumber());
            response.setExpiredMonth(bankCard.getExpiredMonth());
            response.setExpiredYear(bankCard.getExpiredYear());
            response.setAmount(bankCard.getAccount().getAmount());
            return response;
        } else throw new AccountException(ExceptionMessages.ACCOUNT_HAS_BANKCARD);
    }

    private long generateCard() {
        return (long) (Math.random() * 10000000000000000L);
    }

    private int generateCvvNumber() {
        return (int) (Math.random() * (999 - 100 + 1) + 100);
    }


    public void shoppingByBankCard(ShoppingRequest request) {
        BankCard bankCard = bankCardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new BankCardException(ExceptionMessages.BANK_CARD_NOT_EXIST));
        if (checkCardInformation(request, bankCard)) {
            if (!isExpired(bankCard)) {
                if (bankCard.getAccount().getAmount() >= request.getAmount()) {
                    double lastAmount = bankCard.getAccount().getAmount() - request.getAmount();
                    bankCard.getAccount().setAmount(lastAmount);
                    bankCardRepository.save(bankCard);
                } else throw new BankCardException(ExceptionMessages.NOT_ENOUGH_BALANCE);
            } else throw new BankCardException(ExceptionMessages.CARD_EXPIRED);
        } else throw new BankCardException(ExceptionMessages.CARD_INFORMATION);

    }

    private boolean isExpired(BankCard bankCard) {
        if (bankCard.getExpiredYear() < LocalDate.now().getYear())
            return true;
        else
            return bankCard.getExpiredYear() == LocalDate.now().getYear() && bankCard.getExpiredMonth() > LocalDate.now().getMonthValue();
    }

    boolean checkCardInformation(ShoppingRequest request, BankCard bankCard) {
        return request.getCvvNumber() == bankCard.getCvvNumber() && request.getCardHolderName().equals(bankCard.getCardHolder())
                && request.getExpiredMonth() == bankCard.getExpiredMonth() && request.getExpiredYear() == bankCard.getExpiredYear();
    }

    public void withdrawMoneyFromAtm(Long id, double amount) {
        BankCard bankCard = bankCardRepository.findById(id).orElseThrow(() -> new BankCardException(ExceptionMessages.BANK_CARD_NOT_EXIST));
        if (!isExpired(bankCard)) {
            if (bankCard.getAccount().getAmount() >= amount) {
                double lastAmount = bankCard.getAccount().getAmount() - amount;
                bankCard.getAccount().setAmount(lastAmount);
                bankCardRepository.save(bankCard);
            } else throw new BankCardException(ExceptionMessages.NOT_ENOUGH_BALANCE);
        } else throw new BankCardException(ExceptionMessages.CARD_EXPIRED);
    }

    public void depositMoneyInAtm(Long id, double amount) {
        BankCard bankCard = bankCardRepository.findById(id).orElseThrow(() -> new BankCardException(ExceptionMessages.BANK_CARD_NOT_EXIST));
        if (!isExpired(bankCard)){
            double lastAmount = bankCard.getAccount().getAmount() + amount;
            bankCard.getAccount().setAmount(lastAmount);
            bankCardRepository.save(bankCard);
        }else throw new BankCardException(ExceptionMessages.CARD_EXPIRED);
    }
}
