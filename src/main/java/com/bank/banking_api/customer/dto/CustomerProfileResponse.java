package com.bank.banking_api.customer.dto;

import java.time.LocalDate;

public class CustomerProfileResponse {

    private Long customerId;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String kycStatus;

    public CustomerProfileResponse(Long customerId, Long userId, String email,
                                   String firstName, String lastName, String phone,
                                   LocalDate dateOfBirth, String kycStatus) {
        this.customerId = customerId;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.kycStatus = kycStatus;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getKycStatus() {
        return kycStatus;
    }
}