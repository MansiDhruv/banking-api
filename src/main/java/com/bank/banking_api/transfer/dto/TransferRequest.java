package com.bank.banking_api.transfer.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransferRequest {

    @NotNull(message = "From account ID is required")
    private Long fromAccountId;

    @NotBlank(message = "To account number is required")
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String description;

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}