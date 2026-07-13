package com.bank.banking_api.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.banking_api.account.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByCustomerUserEmail(String email);

    Optional<Account> findByIdAndCustomerUserEmail(Long accountId, String email);

    boolean existsByAccountNumber(String accountNumber);
    
    Optional<Account> findByAccountNumber(String accountNumber);
}