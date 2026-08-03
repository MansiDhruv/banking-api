package com.bank.banking_api.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bank.banking_api.customer.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByUserEmail(String email);

    Optional<Customer> findByUserId(Long userId);

    boolean existsByUserEmail(String email);
}