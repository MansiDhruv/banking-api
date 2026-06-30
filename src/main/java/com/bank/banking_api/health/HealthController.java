package com.bank.banking_api.health;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bank.banking_api.common.dto.ValidationTestRequest;
import com.bank.banking_api.common.exception.ResourceNotFoundException;
import com.bank.banking_api.common.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ApiResponse<Map<String, Object>> health() {
    	Map<String, Object> healthData =  Map.of(
                "status", "UP",
                "service", "banking-api",
                "timestamp", LocalDateTime.now()
        );
    	
    	return ApiResponse.success("Banking API is running",healthData);
    }
    
    @GetMapping("/api/v1/health/error")
    public ApiResponse<Object> testError() {
        throw new ResourceNotFoundException("Test resource not found");
    }
    
    @PostMapping("/api/v1/health/validate")
    public ApiResponse<ValidationTestRequest> testValidation(@Valid @RequestBody ValidationTestRequest request) {
        return ApiResponse.success("Validation passed", request);
    }
}