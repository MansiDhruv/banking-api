package com.bank.banking_api.transfer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.banking_api.transfer.entity.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    boolean existsByReference(String reference);

    List<Transfer> findByFromAccountCustomerUserEmailOrderByCreatedAtDesc(String email);
    
    Optional<Transfer> findByIdAndFromAccountCustomerUserEmail(Long transferId, String email);
}