package com.bank.banking_api.customer.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.banking_api.audit.service.AuditService;
import com.bank.banking_api.common.enums.KycStatus;
import com.bank.banking_api.common.exception.ResourceNotFoundException;
import com.bank.banking_api.common.response.PagedResponse;
import com.bank.banking_api.customer.dto.CustomerProfileResponse;
import com.bank.banking_api.customer.entity.Customer;
import com.bank.banking_api.customer.repository.CustomerRepository;
import com.bank.banking_api.user.repository.UserRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    
    private final AuditService auditService;
    private final UserRepository userRepository;

	public CustomerService(CustomerRepository customerRepository, AuditService auditService,
			UserRepository userRepository) {
		this.customerRepository = customerRepository;
		this.auditService = auditService;
		this.userRepository = userRepository;
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
    
    
    @Transactional(readOnly = true)
    public PagedResponse<CustomerProfileResponse> searchCustomers(String email, KycStatus kycStatus,
                                                                  int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Customer> spec = Specification.where(null);

        if (email != null && !email.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("user").get("email"), "%" + email + "%"));
        }

        if (kycStatus != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("kycStatus"), kycStatus));
        }

        Page<Customer> customerPage = customerRepository.findAll(spec, pageable);

        return new PagedResponse<>(
                customerPage.getContent().stream()
                        .map(this::mapToProfileResponse)
                        .toList(),
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return mapToProfileResponse(customer);
    }

    @Transactional
    public CustomerProfileResponse updateKycStatus(String adminEmail, Long customerId, KycStatus kycStatus) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.updateKycStatus(kycStatus);

        Customer savedCustomer = customerRepository.save(customer);

        userRepository.findByEmail(adminEmail).ifPresent(adminUser ->
                auditService.log(
                        adminUser,
                        "KYC_STATUS_UPDATED",
                        "CUSTOMER",
                        savedCustomer.getId().toString(),
                        "Updated KYC status to " + savedCustomer.getKycStatus().name()
                )
        );

        return mapToProfileResponse(savedCustomer);
    }

    private CustomerProfileResponse mapToProfileResponse(Customer customer) {
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