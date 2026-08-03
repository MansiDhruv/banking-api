package com.bank.banking_api.customer.dto;

import com.bank.banking_api.common.enums.KycStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateKycStatusRequest {

    @NotNull(message = "KYC status is required")
    private KycStatus kycStatus;

    public KycStatus getKycStatus() {
        return kycStatus;
    }
}