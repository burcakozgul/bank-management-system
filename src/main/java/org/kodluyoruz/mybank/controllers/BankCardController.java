package org.kodluyoruz.mybank.controllers;

import org.kodluyoruz.mybank.models.CreateBankCardResponse;
import org.kodluyoruz.mybank.models.ShoppingRequest;
import org.kodluyoruz.mybank.services.BankCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bankCard")
public class BankCardController {

    @Autowired
    private BankCardService bankCardService;

    @PostMapping("{account_id}")
    public CreateBankCardResponse createBankCard(@PathVariable("account_id") Long accountId) {
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
