package com.bank.banking_api.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bank.banking_api.transaction.entity.AccountTransaction;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long>,
        JpaSpecificationExecutor<AccountTransaction> {

    boolean existsByTransactionReference(String transactionReference);
}