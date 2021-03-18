package com.burcak.mybank.repositories;

import com.burcak.mybank.entities.CreditCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditCardRepository extends CrudRepository<CreditCard, Long> {
    Optional<CreditCard> findByCardNumber(long cardNumber);
}
