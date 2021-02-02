package org.kodluyoruz.mybank.controllers;

import org.kodluyoruz.mybank.models.CreateCustomerRequest;
import org.kodluyoruz.mybank.models.UpdateCustomerRequest;
import org.kodluyoruz.mybank.exception.GeneralException;
import org.kodluyoruz.mybank.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {


    @Autowired
    private CustomerService customerService;

    @PostMapping
    public void createCustomer(@RequestBody CreateCustomerRequest request) throws GeneralException {
        customerService.createCustomer(request);
    }

    @PutMapping("/{id}")
    public void updateCustomer(@PathVariable Long id, @RequestBody UpdateCustomerRequest request) throws GeneralException {
        customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) throws GeneralException {
        customerService.deleteCustomer(id);
    }
}
