package org.kodluyoruz.mybank.services;

import org.kodluyoruz.mybank.entities.Account;
import org.kodluyoruz.mybank.entities.BankCard;
import org.kodluyoruz.mybank.exception.GeneralException;
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

    public void createBankCard(Long accountId) throws GeneralException {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new GeneralException("Account id doesn't exist"));
        if (account.getBankCard() == null) {
            BankCard bankCard = new BankCard();
            bankCard.setAccount(account);
            bankCard.setCardHolder(account.getCustomer().getFullName());
            bankCard.setCardNumber(generateCard());
            bankCard.setCvvNumber(generateCvvNumber());
            bankCard.setExpiredMonth(LocalDate.now().getMonthValue());
            bankCard.setExpiredYear(LocalDate.now().getYear() + BANK_CARD_EXPIRED_DURATION);
            bankCardRepository.save(bankCard);
        } else throw new GeneralException("Account has bank card");
    }

    private long generateCard() {
        return (long) (Math.random() * 10000000000000000L);
    }

    private int generateCvvNumber() {
        return (int) (Math.random() * (999 - 100 + 1) + 100);
    }


    public void shoppingByBankCard(ShoppingRequest request) throws GeneralException {
        BankCard bankCard = bankCardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new GeneralException("Bank card doesn't exist!"));
        if (checkCardInformation(request, bankCard)) {
            if (!isExpired(bankCard)) {
                if (bankCard.getAccount().getAmount() >= request.getAmount()) {
                    double amount = bankCard.getAccount().getAmount() - request.getAmount();
                    bankCard.getAccount().setAmount(amount);
                    bankCardRepository.save(bankCard);
                } else throw new GeneralException("Bank Card amount doesn't enough");
            } else throw new GeneralException("Card Date expired!");
        } else throw new GeneralException("Please check credit card information");

    }

    private boolean isExpired(BankCard bankCard) {
        if (bankCard.getExpiredYear() < LocalDate.now().getYear())
            return true;
        else return bankCard.getExpiredYear() == LocalDate.now().getYear() && bankCard.getExpiredMonth() > LocalDate.now().getMonthValue();
    }

    boolean checkCardInformation(ShoppingRequest request, BankCard bankCard) {
        return request.getCvvNumber() == bankCard.getCvvNumber() && request.getCardHolderName().equals(bankCard.getCardHolder())
                && request.getExpiredMonth() == bankCard.getExpiredMonth() && request.getExpiredYear() == bankCard.getExpiredYear();
    }
}
