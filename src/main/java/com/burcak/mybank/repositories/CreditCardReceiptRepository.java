package com.burcak.mybank.repositories;

import com.burcak.mybank.entities.CreditCardReceipt;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CreditCardReceiptRepository extends CrudRepository<CreditCardReceipt, Long> {

    List<CreditCardReceipt> findByCreditCardIdAndDateGreaterThanEqualAndDateLessThanEqual(Long id, LocalDateTime previousMonth, LocalDateTime currentMonth);
}
