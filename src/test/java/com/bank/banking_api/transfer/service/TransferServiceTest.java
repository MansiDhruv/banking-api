package com.bank.banking_api.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.bank.banking_api.account.entity.Account;
import com.bank.banking_api.account.repository.AccountRepository;
import com.bank.banking_api.audit.service.AuditService;
import com.bank.banking_api.common.enums.AccountType;
import com.bank.banking_api.common.enums.Role;
import com.bank.banking_api.common.enums.UserStatus;
import com.bank.banking_api.common.exception.InsufficientBalanceException;
import com.bank.banking_api.common.exception.ResourceNotFoundException;
import com.bank.banking_api.customer.entity.Customer;
import com.bank.banking_api.transaction.service.TransactionService;
import com.bank.banking_api.transfer.dto.TransferRequest;
import com.bank.banking_api.transfer.dto.TransferResponse;
import com.bank.banking_api.transfer.entity.Transfer;
import com.bank.banking_api.transfer.repository.TransferRepository;
import com.bank.banking_api.user.entity.User;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransferService transferService;

    private User senderUser;
    private Customer senderCustomer;
    private Account fromAccount;

    private User receiverUser;
    private Customer receiverCustomer;
    private Account toAccount;

    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        senderUser = new User("sender@example.com", "hashedPassword", Role.CUSTOMER, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(senderUser, "id", 1L);

        senderCustomer = new Customer(senderUser, "Sender", "User", "1111111111", LocalDate.of(1995, 5, 15));
        ReflectionTestUtils.setField(senderCustomer, "id", 1L);

        fromAccount = new Account(senderCustomer, "100000000001", AccountType.SAVINGS, "USD");
        ReflectionTestUtils.setField(fromAccount, "id", 1L);
        fromAccount.deposit(new BigDecimal("500.00"));

        receiverUser = new User("receiver@example.com", "hashedPassword", Role.CUSTOMER, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(receiverUser, "id", 2L);

        receiverCustomer = new Customer(receiverUser, "Receiver", "User", "2222222222", LocalDate.of(1995, 5, 15));
        ReflectionTestUtils.setField(receiverCustomer, "id", 2L);

        toAccount = new Account(receiverCustomer, "100000000002", AccountType.SAVINGS, "USD");
        ReflectionTestUtils.setField(toAccount, "id", 2L);

        transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(1L);
        transferRequest.setToAccountNumber("100000000002");
        transferRequest.setAmount(new BigDecimal("100.00"));
        transferRequest.setDescription("Test transfer");
    }

    @Test
    void transfer_ShouldCompleteTransfer_WhenRequestIsValid() {
        when(accountRepository.findByIdAndCustomerUserEmail(1L, "sender@example.com"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("100000000002"))
                .thenReturn(Optional.of(toAccount));
        when(transferRepository.existsByReference(any(String.class))).thenReturn(false);

        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            ReflectionTestUtils.setField(transfer, "id", 1L);
            return transfer;
        });

        TransferResponse response = transferService.transfer("sender@example.com", transferRequest);

        assertNotNull(response);
        assertEquals(1L, response.getTransferId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(new BigDecimal("400.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("100.00"), toAccount.getBalance());

        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
        verify(transactionService, times(2))
        		.recordTransaction(any(Account.class), any(), any(BigDecimal.class), any(String.class));
        verify(auditService).log(any(User.class), any(String.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void transfer_ShouldThrowInsufficientBalanceException_WhenBalanceIsLow() {
        transferRequest.setAmount(new BigDecimal("999.00"));

        when(accountRepository.findByIdAndCustomerUserEmail(1L, "sender@example.com"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("100000000002"))
                .thenReturn(Optional.of(toAccount));

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> transferService.transfer("sender@example.com", transferRequest)
        );

        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void transfer_ShouldThrowResourceNotFoundException_WhenDestinationAccountNotFound() {
        when(accountRepository.findByIdAndCustomerUserEmail(1L, "sender@example.com"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("100000000002"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transferService.transfer("sender@example.com", transferRequest)
        );

        assertEquals("Destination account not found", exception.getMessage());
    }

    @Test
    void transfer_ShouldThrowIllegalStateException_WhenCurrencyMismatch() {
        Account cadAccount = new Account(receiverCustomer, "100000000003", AccountType.SAVINGS, "CAD");
        ReflectionTestUtils.setField(cadAccount, "id", 3L);

        transferRequest.setToAccountNumber("100000000003");

        when(accountRepository.findByIdAndCustomerUserEmail(1L, "sender@example.com"))
                .thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("100000000003"))
                .thenReturn(Optional.of(cadAccount));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> transferService.transfer("sender@example.com", transferRequest)
        );

        assertEquals("Currency mismatch between accounts", exception.getMessage());
    }
}