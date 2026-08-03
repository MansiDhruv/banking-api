package com.bank.banking_api.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.banking_api.account.dto.AccountResponse;
import com.bank.banking_api.account.service.AccountService;
import com.bank.banking_api.common.enums.AccountStatus;
import com.bank.banking_api.common.enums.AccountType;
import com.bank.banking_api.common.response.ApiResponse;
import com.bank.banking_api.common.response.PagedResponse;

@RestController
public class AdminAccountController {

    private final AccountService accountService;

    public AdminAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BANK_STAFF')")
    @PatchMapping("/api/v1/admin/accounts/{accountId}/freeze")
    public ApiResponse<AccountResponse> freezeAccount(@PathVariable Long accountId) {
        AccountResponse response = accountService.freezeAccount(accountId);
        return ApiResponse.success("Account frozen successfully", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BANK_STAFF')")
    @PatchMapping("/api/v1/admin/accounts/{accountId}/unfreeze")
    public ApiResponse<AccountResponse> unfreezeAccount(@PathVariable Long accountId) {
        AccountResponse response = accountService.unfreezeAccount(accountId);
        return ApiResponse.success("Account unfrozen successfully", response);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'BANK_STAFF')")
    @GetMapping("/api/v1/admin/accounts")
    public ApiResponse<PagedResponse<AccountResponse>> searchAccounts(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) AccountType accountType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<AccountResponse> response =
                accountService.searchAccounts(email, status, accountType, page, size);

        return ApiResponse.success("Accounts fetched successfully", response);
    }
}