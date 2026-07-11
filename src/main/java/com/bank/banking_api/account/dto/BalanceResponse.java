package com.bank.banking_api.account.dto;

import java.math.BigDecimal;

public class BalanceResponse {

    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;

    public BalanceResponse(Long accountId, String accountNumber, BigDecimal balance, String currency) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}