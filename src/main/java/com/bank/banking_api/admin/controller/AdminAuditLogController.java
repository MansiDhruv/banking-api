package com.bank.banking_api.admin.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.banking_api.audit.dto.AuditLogResponse;
import com.bank.banking_api.audit.service.AuditService;
import com.bank.banking_api.common.response.ApiResponse;
import com.bank.banking_api.common.response.PagedResponse;

@RestController
public class AdminAuditLogController {

    private final AuditService auditService;

    public AdminAuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BANK_STAFF')")
    @GetMapping("/api/v1/admin/audit-logs")
    public ApiResponse<List<AuditLogResponse>> getAllAuditLogs() {
        List<AuditLogResponse> response = auditService.getAllAuditLogs();
        return ApiResponse.success("Audit logs fetched successfully", response);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'BANK_STAFF')")
    @GetMapping("/api/v1/admin/audit-logs/user/{userId}")
    public ApiResponse<PagedResponse<AuditLogResponse>> getAuditLogsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<AuditLogResponse> response = auditService.getAuditLogsByUserId(userId, page, size);

        return ApiResponse.success("User audit logs fetched successfully", response);
    }
}