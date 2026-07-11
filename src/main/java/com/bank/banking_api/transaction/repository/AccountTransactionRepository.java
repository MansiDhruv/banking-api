package com.bank.banking_api.transaction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.banking_api.transaction.entity.AccountTransaction;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    boolean existsByTransactionReference(String transactionReference);
}