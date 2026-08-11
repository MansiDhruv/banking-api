package com.bank.banking_api.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.bank.banking_api.account.dto.AccountResponse;
import com.bank.banking_api.account.dto.BalanceResponse;
import com.bank.banking_api.account.dto.CreateAccountRequest;
import com.bank.banking_api.account.dto.MoneyRequest;
import com.bank.banking_api.account.entity.Account;
import com.bank.banking_api.account.repository.AccountRepository;
import com.bank.banking_api.audit.service.AuditService;
import com.bank.banking_api.common.enums.AccountStatus;
import com.bank.banking_api.common.enums.AccountType;
import com.bank.banking_api.common.enums.Role;
import com.bank.banking_api.common.enums.UserStatus;
import com.bank.banking_api.common.exception.InsufficientBalanceException;
import com.bank.banking_api.common.exception.InvalidAccountStateException;
import com.bank.banking_api.customer.entity.Customer;
import com.bank.banking_api.customer.repository.CustomerRepository;
import com.bank.banking_api.transaction.service.TransactionService;
import com.bank.banking_api.user.entity.User;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User(
                "test@example.com",
                "hashedPassword",
                Role.CUSTOMER,
                UserStatus.ACTIVE
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        customer = new Customer(
                user,
                "Test",
                "User",
                "1234567890",
                LocalDate.of(1995, 5, 15)
        );
        ReflectionTestUtils.setField(customer, "id", 1L);

        account = new Account(
                customer,
                "100000000001",
                AccountType.SAVINGS,
                "USD"
        );
        ReflectionTestUtils.setField(account, "id", 1L);
    }

    @Test
    void createAccount_ShouldCreateAccount_WhenCustomerExists() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountType(AccountType.SAVINGS);
        request.setCurrency("USD");

        when(customerRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber(any(String.class))).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.createAccount("test@example.com", request);

        assertNotNull(response);
        assertEquals(1L, response.getAccountId());
        assertEquals("SAVINGS", response.getAccountType());
        assertEquals("USD", response.getCurrency());
        assertEquals("ACTIVE", response.getStatus());

        verify(accountRepository).save(any(Account.class));
        verify(auditService).log(any(User.class), any(String.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void deposit_ShouldIncreaseBalance_WhenAccountIsActive() {
        MoneyRequest request = new MoneyRequest();
        request.setAmount(new BigDecimal("500.00"));

        when(accountRepository.findByIdAndCustomerUserEmail(1L, "test@example.com"))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        BalanceResponse response = accountService.deposit("test@example.com", 1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getBalance());

        verify(transactionService).recordTransaction(any(Account.class), any(), any(BigDecimal.class), any(String.class));
        verify(auditService).log(any(User.class), any(String.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void withdraw_ShouldDecreaseBalance_WhenBalanceIsSufficient() {
        account.deposit(new BigDecimal("500.00"));

        MoneyRequest request = new MoneyRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByIdAndCustomerUserEmail(1L, "test@example.com"))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        BalanceResponse response = accountService.withdraw("test@example.com", 1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("400.00"), response.getBalance());

        verify(transactionService).recordTransaction(any(Account.class), any(), any(BigDecimal.class), any(String.class));
        verify(auditService).log(any(User.class), any(String.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void withdraw_ShouldThrowInsufficientBalanceException_WhenBalanceIsLow() {
        MoneyRequest request = new MoneyRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByIdAndCustomerUserEmail(1L, "test@example.com"))
                .thenReturn(Optional.of(account));

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw("test@example.com", 1L, request)
        );

        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void closeAccount_ShouldThrowInvalidAccountStateException_WhenBalanceIsGreaterThanZero() {
        account.deposit(new BigDecimal("100.00"));

        when(accountRepository.findByIdAndCustomerUserEmail(1L, "test@example.com"))
                .thenReturn(Optional.of(account));

        InvalidAccountStateException exception = assertThrows(
                InvalidAccountStateException.class,
                () -> accountService.closeAccount("test@example.com", 1L)
        );

        assertEquals("Account balance must be zero before closing", exception.getMessage());
    }
}