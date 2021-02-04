package org.kodluyoruz.mybank.controllers;

import org.kodluyoruz.mybank.exception.GeneralException;
import org.kodluyoruz.mybank.models.CreateCreditCardResponse;
import org.kodluyoruz.mybank.models.GetCreditCardReceiptResponse;
import org.kodluyoruz.mybank.models.PayLoanFromAccountRequest;
import org.kodluyoruz.mybank.models.ShoppingRequest;
import org.kodluyoruz.mybank.services.CreditCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/creditCard")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @PostMapping("/{customer_id}")
    public CreateCreditCardResponse createCreditCard(@PathVariable("customer_id") Long customerId) throws GeneralException {
        return creditCardService.createCreditCard(customerId);
    }

    @GetMapping("/{id}/loan")
    public double getCreditCardLoan(@PathVariable Long id) throws GeneralException {
        return creditCardService.inquireLoan(id);
    }

    @PostMapping("/shopping")
    public void shoppingByCreditCard(@RequestBody ShoppingRequest request) throws GeneralException {
        creditCardService.shoppingByCreditCard(request);
    }

    @PostMapping("/payLoanFromAccount")
    public void payLoanFromAccount(@RequestBody PayLoanFromAccountRequest request) throws GeneralException {
        creditCardService.payLoanFromAccount(request);
    }

    @PostMapping("/{id}/payLoanFromAtm")
    public void payLoanFromAtm(@PathVariable Long id,@RequestParam double amount) throws GeneralException {
        creditCardService.payLoanFromAtm(id,amount);
    }
    @GetMapping("/{id}/receipt")
    public GetCreditCardReceiptResponse getReceipt(@PathVariable Long id) throws GeneralException {
        return creditCardService.getReceipt(id);
    }

    @PostMapping("/{id}/withdrawMoneyFromAtm")
    public void withdrawMoneyFromAtm(@PathVariable Long id,@RequestParam double amount) throws GeneralException {
        creditCardService.withdrawMoneyFromAtm(id,amount);
    }

}
