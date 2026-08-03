package com.bank.banking_api.transfer.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.banking_api.account.entity.Account;
import com.bank.banking_api.account.repository.AccountRepository;
import com.bank.banking_api.audit.service.AuditService;
import com.bank.banking_api.common.enums.AccountStatus;
import com.bank.banking_api.common.enums.TransactionType;
import com.bank.banking_api.common.enums.TransferStatus;
import com.bank.banking_api.common.exception.InsufficientBalanceException;
import com.bank.banking_api.common.exception.ResourceNotFoundException;
import com.bank.banking_api.common.response.PagedResponse;
import com.bank.banking_api.transaction.service.TransactionService;
import com.bank.banking_api.transfer.dto.TransferRequest;
import com.bank.banking_api.transfer.dto.TransferResponse;
import com.bank.banking_api.transfer.entity.Transfer;
import com.bank.banking_api.transfer.repository.TransferRepository;

@Service
public class TransferService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    
    private final AuditService auditService;

    public TransferService(TransferRepository transferRepository,
                           AccountRepository accountRepository,
                           TransactionService transactionService,
                           AuditService auditService) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.auditService = auditService;
    }

    @Transactional
    public TransferResponse transfer(String email, TransferRequest request) {
        Account fromAccount = accountRepository.findByIdAndCustomerUserEmail(request.getFromAccountId(), email)
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalStateException("Cannot transfer to the same account");
        }

        if (fromAccount.getStatus() != AccountStatus.ACTIVE || toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Both accounts must be active");
        }

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new IllegalStateException("Currency mismatch between accounts");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        String reference = generateUniqueTransferReference();

        Transfer transfer = new Transfer(
                fromAccount,
                toAccount,
                request.getAmount(),
                fromAccount.getCurrency(),
                reference,
                request.getDescription()
        );

        Transfer savedTransfer = transferRepository.save(transfer);

        fromAccount.withdraw(request.getAmount());
        toAccount.deposit(request.getAmount());

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionService.recordTransaction(
                fromAccount,
                TransactionType.TRANSFER_OUT,
                request.getAmount(),
                "Transfer to account " + toAccount.getAccountNumber()
        );

        transactionService.recordTransaction(
                toAccount,
                TransactionType.TRANSFER_IN,
                request.getAmount(),
                "Transfer from account " + fromAccount.getAccountNumber()
        );

        savedTransfer.markSuccess();

        Transfer completedTransfer = transferRepository.save(savedTransfer);
        
        auditService.log(
                fromAccount.getCustomer().getUser(),
                "TRANSFER_COMPLETED",
                "TRANSFER",
                completedTransfer.getId().toString(),
                "Transferred " + request.getAmount() + " " + completedTransfer.getCurrency()
                        + " from account " + fromAccount.getAccountNumber()
                        + " to account " + toAccount.getAccountNumber()
        );

        return mapToResponse(completedTransfer);
    }
    
    @Transactional(readOnly = true)
    public PagedResponse<TransferResponse> searchMyTransfers(String email, TransferStatus status,
                                                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Transfer> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("fromAccount").get("customer").get("user").get("email"), email)
        );

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Transfer> transferPage = transferRepository.findAll(spec, pageable);

        return new PagedResponse<>(
                transferPage.getContent().stream()
                        .map(this::mapToResponse)
                        .toList(),
                transferPage.getNumber(),
                transferPage.getSize(),
                transferPage.getTotalElements(),
                transferPage.getTotalPages(),
                transferPage.isLast()
        );
    }

    private TransferResponse mapToResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getReference(),
                transfer.getFromAccount().getId(),
                transfer.getFromAccount().getAccountNumber(),
                transfer.getToAccount().getId(),
                transfer.getToAccount().getAccountNumber(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus().name(),
                transfer.getDescription(),
                transfer.getCreatedAt(),
                transfer.getCompletedAt()
        );
    }

    private String generateUniqueTransferReference() {
        String reference;

        do {
            reference = "TRF" + System.currentTimeMillis() + SECURE_RANDOM.nextInt(10000);
        } while (transferRepository.existsByReference(reference));

        return reference;
    }	
    
    @Transactional(readOnly = true)
    public TransferResponse getMyTransferById(String email, Long transferId) {
        Transfer transfer = transferRepository.findByIdAndFromAccountCustomerUserEmail(transferId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));

        return mapToResponse(transfer);
    }
}