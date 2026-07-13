package com.bank.banking_api.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferResponse {

    private Long transferId;
    private String reference;
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public TransferResponse(Long transferId, String reference,
                            Long fromAccountId, String fromAccountNumber,
                            Long toAccountId, String toAccountNumber,
                            BigDecimal amount, String currency, String status,
                            String description, LocalDateTime createdAt,
                            LocalDateTime completedAt) {
        this.transferId = transferId;
        this.reference = reference;
        this.fromAccountId = fromAccountId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountId = toAccountId;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getTransferId() {
        return transferId;
    }

    public String getReference() {
        return reference;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}