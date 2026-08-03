package com.bank.banking_api.audit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bank.banking_api.audit.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog>  {
	
	//List<AuditLog> findAllByOrderByCreatedAtDesc();

	//List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}