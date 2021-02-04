package org.kodluyoruz.mybank.services;

import org.kodluyoruz.mybank.client.ExchangeAPI;
import org.kodluyoruz.mybank.entities.*;
import org.kodluyoruz.mybank.exception.GeneralException;
import org.kodluyoruz.mybank.models.CreateAccountRequest;
import org.kodluyoruz.mybank.models.MoneyTransferRequest;
import org.kodluyoruz.mybank.repositories.AccountRepository;
import org.kodluyoruz.mybank.repositories.CustomerRepository;
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

    public void createAccount(Long customerId, CreateAccountRequest request) throws GeneralException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new GeneralException("Customer doesn't exist."));
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
        char[] digits = {'0','1','2','3','4','5','6','7','8','9'};
        StringBuilder result = new StringBuilder();
        for(int i=0; i<24; i++) {
            result.append(digits[(int)Math.floor(Math.random() * 10)]);
        }
        return "TR"+result.toString();
    }

    public void deleteAccount(Long id) throws GeneralException {
        Account account = accountRepository.findById(id).orElseThrow(() -> new GeneralException("Account id doesn't exist"));
        if (account.getAmount() > 0) throw new GeneralException("Account has amount.");
        accountRepository.delete(account);
    }

    public void moneyTransfer(MoneyTransferRequest request) throws GeneralException {
        Account senderAccount = accountRepository.findByIban(request.getSenderIban()).orElseThrow(()-> new GeneralException(""));
        Account receiverAccount = accountRepository.findByIban(request.getReceiverIban()).orElseThrow(()-> new GeneralException(""));
        double amount = request.getAmount();
        if (senderAccount.getAmount() >= amount) {
            if (senderAccount.getAccountType().equals(AccountType.SAVING) || receiverAccount.getAccountType().equals(AccountType.SAVING)) {
                if (senderAccount.getCustomer().getId().equals(receiverAccount.getCustomer().getId())) {
                    checkCurrency(senderAccount, receiverAccount, amount);

                } else throw new GeneralException("Money cannot be transferred to other customers' savings accounts");
            } else {
                checkCurrency(senderAccount, receiverAccount, amount);
            }
        } else throw new GeneralException("Sender Account doesn't have enough amount");
    }

    private void checkCurrency(Account senderAccount, Account receiverAccount, double amount) {
        double exchangeAmount = 0;
        if (senderAccount.getCurrency() != receiverAccount.getCurrency()) {
            Exchange exchange= exchangeAPI.exchange(senderAccount.getCurrency());
            if (receiverAccount.getCurrency().equals(Currency.EUR)){
                exchangeAmount=amount*exchange.getRates().getEur();
            }
            else if (receiverAccount.getCurrency().equals(Currency.TRY)){
                exchangeAmount=amount*exchange.getRates().getTr();
            }
            else if (receiverAccount.getCurrency().equals(Currency.USD)){
                exchangeAmount=amount*exchange.getRates().getUsd();
            }
            setLastAmount(senderAccount,receiverAccount,amount,exchangeAmount);
        } else {
            setLastAmount(senderAccount,receiverAccount,amount,amount);
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
