package com.bank.banking_api.account.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.banking_api.account.dto.AccountResponse;
import com.bank.banking_api.account.dto.BalanceResponse;
import com.bank.banking_api.account.dto.CreateAccountRequest;
import com.bank.banking_api.account.dto.MoneyRequest;
import com.bank.banking_api.account.entity.Account;
import com.bank.banking_api.account.repository.AccountRepository;
import com.bank.banking_api.common.enums.AccountStatus;
import com.bank.banking_api.common.enums.TransactionType;
import com.bank.banking_api.common.exception.InsufficientBalanceException;
import com.bank.banking_api.common.exception.InvalidAccountStateException;
import com.bank.banking_api.common.exception.ResourceNotFoundException;
import com.bank.banking_api.customer.entity.Customer;
import com.bank.banking_api.customer.repository.CustomerRepository;
import com.bank.banking_api.transaction.dto.TransactionResponse;
import com.bank.banking_api.transaction.service.TransactionService;

@Service
public class AccountService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    
    private final TransactionService transactionService;

    
	public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository,
			TransactionService transactionService) {
		this.accountRepository = accountRepository;
		this.customerRepository = customerRepository;
		this.transactionService = transactionService;
	}

    @Transactional
    public AccountResponse createAccount(String email, CreateAccountRequest request) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        String accountNumber = generateUniqueAccountNumber();

        Account account = new Account(
                customer,
                accountNumber,
                request.getAccountType(),
                request.getCurrency()
        );

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(String email) {
        return accountRepository.findByCustomerUserEmail(email)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getMyAccountById(String email, Long accountId) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getMyAccountBalance(String email, Long accountId) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return new BalanceResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency()
        );
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType().name(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name(),
                account.getCreatedAt()
        );
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;

        do {
            accountNumber = "10" + String.format("%010d", SECURE_RANDOM.nextLong(1_000_000_0000L));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
    
    
    @Transactional
    public BalanceResponse deposit(String email, Long accountId, MoneyRequest request) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }

        account.deposit(request.getAmount());

        Account savedAccount = accountRepository.save(account);
        
        transactionService.recordTransaction(
                savedAccount,
                TransactionType.DEPOSIT,
                request.getAmount(),
                "Deposit to account"
        );

        return new BalanceResponse(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getBalance(),
                savedAccount.getCurrency()
        );
    }
    
    
    @Transactional
    public BalanceResponse withdraw(String email, Long accountId, MoneyRequest request) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.withdraw(request.getAmount());

        Account savedAccount = accountRepository.save(account);
        
        transactionService.recordTransaction(
                savedAccount,
                TransactionType.WITHDRAWAL,
                request.getAmount(),
                "Withdrawal from account"
        );

        return new BalanceResponse(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getBalance(),
                savedAccount.getCurrency()
        );
    }
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyAccountTransactions(String email, Long accountId) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return transactionService.getTransactionsForAccount(account.getId());
    }
    
    @Transactional
    public AccountResponse closeAccount(String email, Long accountId) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new InvalidAccountStateException("Account is already closed");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidAccountStateException("Account balance must be zero before closing");
        }

        account.close();

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }
}