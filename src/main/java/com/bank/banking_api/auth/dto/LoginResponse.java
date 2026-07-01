package com.bank.banking_api.auth.dto;

public class LoginResponse {

    private Long userId;
    private String email;
    private String role;
    private String status;
    private String accessToken;
    private String tokenType;

    public LoginResponse(Long userId, String email, String role, String status, String accessToken, String tokenType) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.status = status;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
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

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}