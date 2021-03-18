package com.burcak.mybank.services;

import com.burcak.mybank.entities.*;
import com.burcak.mybank.exception.AccountException;
import com.burcak.mybank.exception.CustomerException;
import com.burcak.mybank.exception.ExceptionMessages;
import com.burcak.mybank.repositories.AccountRepository;
import com.burcak.mybank.repositories.CustomerRepository;
import com.burcak.mybank.client.ExchangeAPI;
import com.burcak.mybank.entities.*;
import com.burcak.mybank.models.CreateAccountRequest;
import com.burcak.mybank.models.MoneyTransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    ExchangeAPI exchangeAPI;

    public void createAccount(Long customerId, CreateAccountRequest request) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerException(ExceptionMessages.CUSTOMER_NOT_EXIST));
        Account account = new Account();
        account.setCustomer(customer);
        account.setName(request.getName());
        account.setIban(generateIban());
        account.setAccountType(request.getAccountType());
        account.setAmount(request.getAmount());
        account.setBranch(request.getBranch());
        account.setCreatedDate(LocalDateTime.now());
        account.setCurrency(request.getCurrency());
        accountRepository.save(account);
    }

    private String generateIban() {
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            result.append(digits[(int) Math.floor(Math.random() * 10)]);
        }
        return "TR" + result.toString();
    }

    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountException(ExceptionMessages.ACCOUNT_NOT_EXIST));
        if (account.getAmount() > 0){
            throw new AccountException(ExceptionMessages.ACCOUNT_BALANCE);
        }
        accountRepository.delete(account);
    }

    public void moneyTransfer(MoneyTransferRequest request) {
        Account senderAccount = accountRepository.findByIban(request.getSenderIban()).orElseThrow(() -> new AccountException(ExceptionMessages.SENDER_IBAN_NOT_FOUND));
        Account receiverAccount = accountRepository.findByIban(request.getReceiverIban()).orElseThrow(() -> new AccountException(ExceptionMessages.RECEIVER_IBAN_NOT_FOUND));
        double amount = request.getAmount();
        if (senderAccount.getAmount() < amount) {
            throw new AccountException(ExceptionMessages.NOT_ENOUGH_BALANCE);
        }
        if ((senderAccount.getAccountType().equals(AccountType.SAVING) || receiverAccount.getAccountType().equals(AccountType.SAVING))
                && !senderAccount.getCustomer().getId().equals(receiverAccount.getCustomer().getId())) {
            throw new AccountException(ExceptionMessages.MONEY_TRANSFER_OTHER_CUSTOMER_SAVING_ACCOUNT);
        } else {
            checkCurrency(senderAccount, receiverAccount, amount);
        }
    }

    private void checkCurrency(Account senderAccount, Account receiverAccount, double amount) {
        double exchangeAmount = 0;
        if (senderAccount.getCurrency() != receiverAccount.getCurrency()) {
            Exchange exchange = exchangeAPI.exchange(senderAccount.getCurrency());
            if (receiverAccount.getCurrency().equals(Currency.EUR)) {
                exchangeAmount = amount * exchange.getRates().getEur();
            } else if (receiverAccount.getCurrency().equals(Currency.TRY)) {
                exchangeAmount = amount * exchange.getRates().getTr();
            } else if (receiverAccount.getCurrency().equals(Currency.USD)) {
                exchangeAmount = amount * exchange.getRates().getUsd();
            }
            setLastAmount(senderAccount, receiverAccount, amount, exchangeAmount);
        } else {
            setLastAmount(senderAccount, receiverAccount, amount, amount);
        }
    }

    private void setLastAmount(Account senderAccount, Account receiverAccount, double amount, double amount2) {
        double senderAmount;
        double receiverAmount;
        senderAmount = senderAccount.getAmount() - amount;
        receiverAmount = receiverAccount.getAmount() + amount2;
        senderAccount.setAmount(senderAmount);
        receiverAccount.setAmount(receiverAmount);
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
    }

}