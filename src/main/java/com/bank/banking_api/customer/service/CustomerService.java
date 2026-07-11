package com.bank.banking_api.customer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.banking_api.common.exception.ResourceNotFoundException;
import com.bank.banking_api.customer.dto.CustomerProfileResponse;
import com.bank.banking_api.customer.entity.Customer;
import com.bank.banking_api.customer.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getCurrentCustomerProfile(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        return new CustomerProfileResponse(
                customer.getId(),
                customer.getUser().getId(),
                customer.getUser().getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhone(),
                customer.getDateOfBirth(),
                customer.getKycStatus().name()
        );
    }
}