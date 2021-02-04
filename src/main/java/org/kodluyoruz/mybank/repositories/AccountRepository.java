package org.kodluyoruz.mybank.repositories;

import org.kodluyoruz.mybank.entities.Account;
import org.kodluyoruz.mybank.entities.AccountType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    Optional<Account> findByIban(String iban);

    Optional<Account> findByIdAndAccountType(Long id, AccountType accountType);
}
