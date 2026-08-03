package com.bank.banking_api.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.banking_api.common.enums.KycStatus;
import com.bank.banking_api.common.response.ApiResponse;
import com.bank.banking_api.common.response.PagedResponse;
import com.bank.banking_api.customer.dto.CustomerProfileResponse;
import com.bank.banking_api.customer.dto.UpdateKycStatusRequest;
import com.bank.banking_api.customer.service.CustomerService;

import jakarta.validation.Valid;

@RestController
public class AdminCustomerController {

    private final CustomerService customerService;

    public AdminCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BANK_STAFF')")
    @GetMapping("/api/v1/admin/customers")
    public ApiResponse<PagedResponse<CustomerProfileResponse>> searchCustomers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) KycStatus kycStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CustomerProfileResponse> response =
                customerService.searchCustomers(email, kycStatus, page, size);

        return ApiResponse.success("Customers fetched successfully", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BANK_STAFF')")
    @GetMapping("/api/v1/admin/customers/{customerId}")
    public ApiResponse<CustomerProfileResponse> getCustomerById(@PathVariable Long customerId) {
        CustomerProfileResponse response = customerService.getCustomerById(customerId);
        return ApiResponse.success("Customer fetched successfully", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BANK_STAFF')")
    @PatchMapping("/api/v1/admin/customers/{customerId}/kyc-status")
    public ApiResponse<CustomerProfileResponse> updateKycStatus(
            Authentication authentication,
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateKycStatusRequest request) {
        CustomerProfileResponse response =
                customerService.updateKycStatus(authentication.getName(), customerId, request.getKycStatus());

        return ApiResponse.success("KYC status updated successfully", response);
    }
}