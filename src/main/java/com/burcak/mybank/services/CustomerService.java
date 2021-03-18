package com.burcak.mybank.services;

import com.burcak.mybank.entities.Customer;
import com.burcak.mybank.exception.CustomerException;
import com.burcak.mybank.exception.ExceptionMessages;
import com.burcak.mybank.repositories.CustomerRepository;
import com.burcak.mybank.entities.Account;
import com.burcak.mybank.models.CreateCustomerRequest;
import com.burcak.mybank.models.UpdateCustomerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public void updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerException(ExceptionMessages.CUSTOMER_NOT_EXIST));
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customerRepository.save(customer);
    }

    public void createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByTckn(request.getTckn())) {
            throw new CustomerException(ExceptionMessages.CUSTOMER_EXIST);
        }
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setTckn(request.getTckn());
        customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerException(ExceptionMessages.CUSTOMER_NOT_EXIST));
        if (customer.getCreditCard() != null && customer.getCreditCard().getBalance() < 0) {
            throw new CustomerException(ExceptionMessages.CREDIT_CARD_BALANCE);
        } else {
            if (customer.getAccounts() != null) {
                List<Account> accounts = customer.getAccounts();
                for (Account account : accounts) {
                    if (account.getAmount() > 0) {
                        throw new CustomerException(ExceptionMessages.ACCOUNT_BALANCE);
                    }
                }
            }
        }
        customerRepository.delete(customer);
    }
}
