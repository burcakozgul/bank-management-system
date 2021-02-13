package org.kodluyoruz.mybank.services;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kodluyoruz.mybank.entities.Customer;
import org.kodluyoruz.mybank.models.CreateCustomerRequest;
import org.kodluyoruz.mybank.repositories.CustomerRepository;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;

    @Test
    void createCustomer() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFullName("burcak");
        request.setPhoneNumber("05066489526");
        request.setAddress("istanbul");
        request.setEmail("burcak@gmail.com");
        request.setTckn("48521458962");
        customerService.createCustomer(request);

        Customer customer = new Customer();
        customer.setFullName("burcak");
        customer.setPhoneNumber("05066489526");
        customer.setAddress("istanbul");
        customer.setEmail("burcak@gmail.com");
        customer.setTckn("48521458962");

        Mockito.verify(customerRepository,Mockito.times(1)).save(ArgumentMatchers.refEq(customer));

    }
}