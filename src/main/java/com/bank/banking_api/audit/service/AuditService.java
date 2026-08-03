package com.bank.banking_api.audit.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.bank.banking_api.audit.dto.AuditLogResponse;
import com.bank.banking_api.audit.entity.AuditLog;
import com.bank.banking_api.audit.repository.AuditLogRepository;
import com.bank.banking_api.common.response.PagedResponse;
import com.bank.banking_api.user.entity.User;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(User user, String action, String entityType, String entityId, String details) {
        AuditLog auditLog = new AuditLog(user, action, entityType, entityId, details);
        auditLogRepository.save(auditLog);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogsByUserId(Long userId, int page, int size) {
        return searchAuditLogs(userId, null, null, page, size);
    }
    
    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        Long userId = auditLog.getUser() != null ? auditLog.getUser().getId() : null;
        String userEmail = auditLog.getUser() != null ? auditLog.getUser().getEmail() : null;

        return new AuditLogResponse(
                auditLog.getId(),
                userId,
                userEmail,
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
        );
    }
    
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> searchAuditLogs(Long userId, String action, String entityType,
                                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<AuditLog> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        if (action != null && !action.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }

        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }

        Page<AuditLog> auditLogPage = auditLogRepository.findAll(spec, pageable);

        return new PagedResponse<>(
                auditLogPage.getContent().stream()
                        .map(this::mapToResponse)
                        .toList(),
                auditLogPage.getNumber(),
                auditLogPage.getSize(),
                auditLogPage.getTotalElements(),
                auditLogPage.getTotalPages(),
                auditLogPage.isLast()
        );
    }
}