package org.kodluyoruz.mybank.controllers;

import org.kodluyoruz.mybank.models.CreateAccountRequest;
import org.kodluyoruz.mybank.models.MoneyTransferRequest;
import org.kodluyoruz.mybank.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/{customer_id}")
    public void createAccount(@PathVariable("customer_id") Long customerId, @RequestBody CreateAccountRequest request) {
        accountService.createAccount(customerId, request);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }

    @PostMapping("/moneyTransfer")
    public void moneyTransfer(@RequestBody MoneyTransferRequest request) {
        accountService.moneyTransfer(request);
    }
}
