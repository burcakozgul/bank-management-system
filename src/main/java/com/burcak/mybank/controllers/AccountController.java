package com.burcak.mybank.controllers;

import com.burcak.mybank.models.CreateAccountRequest;
import com.burcak.mybank.models.MoneyTransferRequest;
import com.burcak.mybank.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/{customerId}")
    public void createAccount(@PathVariable Long customerId, @RequestBody CreateAccountRequest request) {
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
