package com.bank.banking_api.auth.dto;

public class RegisterResponse {

    private Long userId;
    private Long customerId;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private String kycStatus;

    public RegisterResponse(Long userId, Long customerId, String email, String fullName,
                            String role, String status, String kycStatus) {
        this.userId = userId;
        this.customerId = customerId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.kycStatus = kycStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getKycStatus() {
        return kycStatus;
    }
}