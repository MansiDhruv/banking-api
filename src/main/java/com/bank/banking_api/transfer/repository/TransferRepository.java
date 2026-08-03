package com.bank.banking_api.transfer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bank.banking_api.transfer.entity.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long>, JpaSpecificationExecutor<Transfer> {

    boolean existsByReference(String reference);

    Optional<Transfer> findByIdAndFromAccountCustomerUserEmail(Long transferId, String email);
}