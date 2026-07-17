package com.bank.banking_api.transfer.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bank.banking_api.common.response.ApiResponse;
import com.bank.banking_api.transfer.dto.TransferRequest;
import com.bank.banking_api.transfer.dto.TransferResponse;
import com.bank.banking_api.transfer.service.TransferService;

import jakarta.validation.Valid;

@RestController
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/api/v1/transfers")
    public ApiResponse<TransferResponse> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.transfer(authentication.getName(), request);
        return ApiResponse.success("Transfer completed successfully", response);
    }

    @GetMapping("/api/v1/transfers")
    public ApiResponse<List<TransferResponse>> getMyTransfers(Authentication authentication) {
        List<TransferResponse> response = transferService.getMyTransfers(authentication.getName());
        return ApiResponse.success("Transfers fetched successfully", response);
    }
    
    @GetMapping("/api/v1/transfers/{transferId}")
    public ApiResponse<TransferResponse> getMyTransferById(
            Authentication authentication,
            @PathVariable Long transferId) {
        TransferResponse response = transferService.getMyTransferById(authentication.getName(), transferId);
        return ApiResponse.success("Transfer fetched successfully", response);
    }
}