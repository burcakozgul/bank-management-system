package org.kodluyoruz.mybank.controllers;

import org.kodluyoruz.mybank.exception.GeneralException;
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
    public void createBankCard(@PathVariable("account_id") Long accountId) throws GeneralException {
        bankCardService.createBankCard(accountId);
    }

    @PostMapping("/shopping")
    public void shoppingByBankCard(@RequestBody ShoppingRequest request) throws GeneralException {
        bankCardService.shoppingByBankCard(request);
    }
}
