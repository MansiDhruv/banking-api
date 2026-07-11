package com.bank.banking_api.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long transactionId;
    private Long accountId;
    private String transactionReference;
    private String type;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceAfter;
    private String status;
    private String description;
    private LocalDateTime createdAt;

    public TransactionResponse(Long transactionId, Long accountId, String transactionReference,
                               String type, BigDecimal amount, String currency,
                               BigDecimal balanceAfter, String status, String description,
                               LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionReference = transactionReference;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.balanceAfter = balanceAfter;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}