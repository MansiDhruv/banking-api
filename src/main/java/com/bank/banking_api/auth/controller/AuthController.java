package com.bank.banking_api.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bank.banking_api.auth.dto.RegisterRequest;
import com.bank.banking_api.auth.dto.RegisterResponse;
import com.bank.banking_api.auth.service.AuthService;
import com.bank.banking_api.common.response.ApiResponse;
import com.bank.banking_api.auth.dto.LoginRequest;
import com.bank.banking_api.auth.dto.LoginResponse;

import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/v1/auth/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ApiResponse.success("User registered successfully", response);
    }
    
    @PostMapping("/api/v1/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("Login successful", response);
    }
    
    @GetMapping("/api/v1/auth/me")
    public ApiResponse<String> me(Authentication authentication) {
        return ApiResponse.success("Authenticated user fetched", authentication.getName());
    }
}