package com.bank.banking_api.auth.dto;

public class RegisterResponse {

    private Long userId;
    private String email;
    private String role;
    private String status;

    public RegisterResponse(Long userId, String email, String role, String status) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}