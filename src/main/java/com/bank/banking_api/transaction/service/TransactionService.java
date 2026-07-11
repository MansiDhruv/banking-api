package com.bank.banking_api.transaction.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.bank.banking_api.account.entity.Account;
import com.bank.banking_api.common.enums.TransactionStatus;
import com.bank.banking_api.common.enums.TransactionType;
import com.bank.banking_api.transaction.entity.AccountTransaction;
import com.bank.banking_api.transaction.repository.AccountTransactionRepository;

@Service
public class TransactionService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountTransactionRepository accountTransactionRepository;

    public TransactionService(AccountTransactionRepository accountTransactionRepository) {
        this.accountTransactionRepository = accountTransactionRepository;
    }

    public AccountTransaction recordTransaction(Account account, TransactionType type, java.math.BigDecimal amount, String description) {
        String reference = generateUniqueTransactionReference();

        AccountTransaction transaction = new AccountTransaction(
                account,
                reference,
                type,
                amount,
                account.getCurrency(),
                account.getBalance(),
                TransactionStatus.SUCCESS,
                description
        );

        return accountTransactionRepository.save(transaction);
    }
    
    private String generateUniqueTransactionReference() {
        String reference;

        do {
            reference = "TXN" + System.currentTimeMillis() + SECURE_RANDOM.nextInt(10000);
        } while (accountTransactionRepository.existsByTransactionReference(reference));

        return reference;
    }
}