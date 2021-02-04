package org.kodluyoruz.mybank.controllers;

import org.kodluyoruz.mybank.exception.GeneralException;
import org.kodluyoruz.mybank.models.GetCreditCardReceiptResponse;
import org.kodluyoruz.mybank.models.PayLoanFromAccountRequest;
import org.kodluyoruz.mybank.models.PayLoanFromAtmRequest;
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
    public void createCreditCard(@PathVariable("customer_id") Long customerId) throws GeneralException {
        creditCardService.createCreditCard(customerId);
    }

    @GetMapping("/loan/{id}")
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

    @PostMapping("/payLoanFromAtm")
    public void payLoanFromAtm(@RequestBody PayLoanFromAtmRequest request) throws GeneralException {
        creditCardService.payLoanFromAtm(request);
    }
    @GetMapping("/receipt/{id}")
    public GetCreditCardReceiptResponse getReceipt(@PathVariable Long id,@RequestParam("month")int month,@RequestParam("year")int year) throws GeneralException {
        return creditCardService.getReceipt(id,month,year);
    }

//    @GetMapping("/expenses/{id}")
//    public GetCreditCardReceiptResponse getExpenses(@PathVariable Long id) throws GeneralException {
//        return creditCardService.getExpenses(id);
//    }
}
