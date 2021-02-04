package org.kodluyoruz.mybank.services;

import org.kodluyoruz.mybank.entities.Account;
import org.kodluyoruz.mybank.models.CreateCustomerRequest;
import org.kodluyoruz.mybank.entities.Customer;
import org.kodluyoruz.mybank.models.UpdateCustomerRequest;
import org.kodluyoruz.mybank.exception.GeneralException;
import org.kodluyoruz.mybank.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public void updateCustomer(Long id, UpdateCustomerRequest request) throws GeneralException {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new GeneralException("Customer id is not exist!"));
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customerRepository.save(customer);
    }

    public void createCustomer(CreateCustomerRequest request) throws GeneralException {
        if (customerRepository.findByPersonalIdNo(request.getPersonalIdNo()).isPresent())
            throw new GeneralException("This personal Id exist!");
        else {
            Customer customer = new Customer();
            customer.setFullName(request.getFullName());
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setEmail(request.getEmail());
            customer.setAddress(request.getAddress());
            customer.setPersonalIdNo(request.getPersonalIdNo());
            customerRepository.save(customer);
        }
    }

    public void deleteCustomer(Long id) throws GeneralException {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new GeneralException("Customer id is not exist!"));
        if (customer.getCreditCard() != null) {
            if (customer.getCreditCard().getBalance() < 0)
                throw new GeneralException("This customer has a credit card balance!");
        } else {
            if (customer.getAccounts() != null || !customer.getAccounts().isEmpty()) {
                List<Account> accounts = customer.getAccounts();
                for (Account account : accounts) {
                    if (account.getAmount() > 0) {
                        throw new GeneralException("This customer has a account balance!");
                    }
                }
            }
        }
        customerRepository.delete(customer);
    }
}
