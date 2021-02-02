package org.kodluyoruz.mybank.repositories;

import org.kodluyoruz.mybank.entities.BankCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankCardRepository extends CrudRepository<BankCard, Long> {
    Optional<BankCard> findByCardNumber(long cardNumber);

}
