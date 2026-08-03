package com.bank.banking_api.transaction.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.banking_api.account.entity.Account;
import com.bank.banking_api.common.enums.TransactionStatus;
import com.bank.banking_api.common.enums.TransactionType;
import com.bank.banking_api.transaction.dto.TransactionResponse;
import com.bank.banking_api.transaction.entity.AccountTransaction;
import com.bank.banking_api.transaction.repository.AccountTransactionRepository;
import com.bank.banking_api.common.exception.ResourceNotFoundException;
import com.bank.banking_api.common.response.PagedResponse;


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
    
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> searchTransactionsForAccount(
            Long accountId,
            TransactionType type,
            TransactionStatus status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<AccountTransaction> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("account").get("id"), accountId)
        );

        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<AccountTransaction> transactionPage = accountTransactionRepository.findAll(spec, pageable);

        return new PagedResponse<>(
                transactionPage.getContent().stream()
                        .map(this::mapToResponse)
                        .toList(),
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }		
    
    private TransactionResponse mapToResponse(AccountTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getTransactionReference(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getBalanceAfter(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}