package com.burcak.mybank.controllers;

import com.burcak.mybank.models.CreateBankCardResponse;
import com.burcak.mybank.models.ShoppingRequest;
import com.burcak.mybank.services.BankCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bankCard")
public class BankCardController {

    @Autowired
    private BankCardService bankCardService;

    @PostMapping("{accountId}")
    public CreateBankCardResponse createBankCard(@PathVariable Long accountId) {
        return bankCardService.createBankCard(accountId);
    }

    @PostMapping("/shopping")
    public void shoppingByBankCard(@RequestBody ShoppingRequest request) {
        bankCardService.shoppingByBankCard(request);
    }

    @PostMapping("/{id}/withdrawMoneyFromAtm")
    public void withdrawMoneyFromAtm(@PathVariable Long id,@RequestParam double amount) {
        bankCardService.withdrawMoneyFromAtm(id,amount);
    }

    @PostMapping("/{id}/depositMoneyInAtm")
    public void depositMoneyInAtm(@PathVariable Long id,@RequestParam double amount) {
        bankCardService.depositMoneyInAtm(id,amount);
    }
}
