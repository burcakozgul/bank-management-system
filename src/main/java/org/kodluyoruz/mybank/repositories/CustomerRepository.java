package org.kodluyoruz.mybank.repositories;

import org.kodluyoruz.mybank.entities.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<Customer> findByPersonalIdNo(String personalIdNo);
}
