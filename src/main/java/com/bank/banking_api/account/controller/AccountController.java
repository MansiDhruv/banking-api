package com.bank.banking_api.account.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bank.banking_api.account.dto.AccountResponse;
import com.bank.banking_api.account.dto.BalanceResponse;
import com.bank.banking_api.account.dto.CreateAccountRequest;
import com.bank.banking_api.account.dto.MoneyRequest;
import com.bank.banking_api.account.service.AccountService;
import com.bank.banking_api.common.response.ApiResponse;
import com.bank.banking_api.transaction.dto.TransactionResponse;

import jakarta.validation.Valid;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/api/v1/accounts")
    public ApiResponse<AccountResponse> createAccount(
            Authentication authentication,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(authentication.getName(), request);
        return ApiResponse.success("Account created successfully", response);
    }

    @GetMapping("/api/v1/accounts")
    public ApiResponse<List<AccountResponse>> getMyAccounts(Authentication authentication) {
        List<AccountResponse> response = accountService.getMyAccounts(authentication.getName());
        return ApiResponse.success("Accounts fetched successfully", response);
    }

    @GetMapping("/api/v1/accounts/{accountId}")
    public ApiResponse<AccountResponse> getMyAccountById(
            Authentication authentication,
            @PathVariable Long accountId) {
        AccountResponse response = accountService.getMyAccountById(authentication.getName(), accountId);
        return ApiResponse.success("Account fetched successfully", response);
    }

    @GetMapping("/api/v1/accounts/{accountId}/balance")
    public ApiResponse<BalanceResponse> getMyAccountBalance(
            Authentication authentication,
            @PathVariable Long accountId) {
        BalanceResponse response = accountService.getMyAccountBalance(authentication.getName(), accountId);
        return ApiResponse.success("Account balance fetched successfully", response);
    }
    
    @PostMapping("/api/v1/accounts/{accountId}/deposit")
    public ApiResponse<BalanceResponse> deposit(
            Authentication authentication,
            @PathVariable Long accountId,
            @Valid @RequestBody MoneyRequest request) {
        BalanceResponse response = accountService.deposit(authentication.getName(), accountId, request);
        return ApiResponse.success("Amount deposited successfully", response);
    }
    
    @PostMapping("/api/v1/accounts/{accountId}/withdraw")
    public ApiResponse<BalanceResponse> withdraw(
            Authentication authentication,
            @PathVariable Long accountId,
            @Valid @RequestBody MoneyRequest request) {
        BalanceResponse response = accountService.withdraw(authentication.getName(), accountId, request);
        return ApiResponse.success("Amount withdrawn successfully", response);
    }
    
    
    @GetMapping("/api/v1/accounts/{accountId}/transactions")
    public ApiResponse<List<TransactionResponse>> getMyAccountTransactions(
            Authentication authentication,
            @PathVariable Long accountId) {
        List<TransactionResponse> response =
                accountService.getMyAccountTransactions(authentication.getName(), accountId);

        return ApiResponse.success("Account transactions fetched successfully", response);
    }
}