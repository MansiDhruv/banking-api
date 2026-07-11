package com.bank.banking_api.account.dto;

import com.bank.banking_api.common.enums.AccountType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase code")
    private String currency = "USD";

    public AccountType getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }
}