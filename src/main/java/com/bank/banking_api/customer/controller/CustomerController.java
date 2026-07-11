package com.bank.banking_api.customer.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.banking_api.common.response.ApiResponse;
import com.bank.banking_api.customer.dto.CustomerProfileResponse;
import com.bank.banking_api.customer.service.CustomerService;

@RestController
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/api/v1/customers/me")
    public ApiResponse<CustomerProfileResponse> getMyProfile(Authentication authentication) {
        CustomerProfileResponse response = customerService.getCurrentCustomerProfile(authentication.getName());
        return ApiResponse.success("Customer profile fetched successfully", response);
    }
}