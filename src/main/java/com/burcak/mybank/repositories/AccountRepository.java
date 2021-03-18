package com.burcak.mybank.repositories;

import com.burcak.mybank.entities.Account;
import com.burcak.mybank.entities.AccountType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    Optional<Account> findByIban(String iban);

    Optional<Account> findByIdAndAccountType(Long id, AccountType accountType);
}
