package com.bank.banking_api.account.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.banking_api.account.dto.AccountResponse;
import com.bank.banking_api.account.dto.BalanceResponse;
import com.bank.banking_api.account.dto.CreateAccountRequest;
import com.bank.banking_api.account.dto.MoneyRequest;
import com.bank.banking_api.account.entity.Account;
import com.bank.banking_api.account.repository.AccountRepository;
import com.bank.banking_api.audit.service.AuditService;
import com.bank.banking_api.common.enums.AccountStatus;
import com.bank.banking_api.common.enums.AccountType;
import com.bank.banking_api.common.enums.TransactionStatus;
import com.bank.banking_api.common.enums.TransactionType;
import com.bank.banking_api.common.exception.InsufficientBalanceException;
import com.bank.banking_api.common.exception.InvalidAccountStateException;
import com.bank.banking_api.common.exception.ResourceNotFoundException;
import com.bank.banking_api.common.response.PagedResponse;
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

	private final AuditService auditService;

	public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository,
			TransactionService transactionService, AuditService auditService) {
		this.accountRepository = accountRepository;
		this.customerRepository = customerRepository;
		this.transactionService = transactionService;
		this.auditService = auditService;
	}

	@Transactional
	public AccountResponse createAccount(String email, CreateAccountRequest request) {
		Customer customer = customerRepository.findByUserEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

		String accountNumber = generateUniqueAccountNumber();

		Account account = new Account(customer, accountNumber, request.getAccountType(), request.getCurrency());

		Account savedAccount = accountRepository.save(account);

		 auditService.log(
		            customer.getUser(),
		            "ACCOUNT_CREATED",
		            "ACCOUNT",
		            savedAccount.getId().toString(),
		            "Created " + savedAccount.getAccountType().name()
		                    + " account with number " + savedAccount.getAccountNumber()
		                    + " in " + savedAccount.getCurrency()
		    );
		return mapToResponse(savedAccount);
	}

	@Transactional(readOnly = true)
	public List<AccountResponse> getMyAccounts(String email) {
		return accountRepository.findByCustomerUserEmail(email).stream().map(this::mapToResponse).toList();
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

		return new BalanceResponse(account.getId(), account.getAccountNumber(), account.getBalance(),
				account.getCurrency());
	}

	private AccountResponse mapToResponse(Account account) {
		return new AccountResponse(account.getId(), account.getAccountNumber(), account.getAccountType().name(),
				account.getBalance(), account.getCurrency(), account.getStatus().name(), account.getCreatedAt());
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

		transactionService.recordTransaction(savedAccount, TransactionType.DEPOSIT, request.getAmount(),
				"Deposit to account");
		
		auditService.log(
		        savedAccount.getCustomer().getUser(),
		        "AMOUNT_DEPOSITED",
		        "ACCOUNT",
		        savedAccount.getId().toString(),
		        "Deposited " + request.getAmount() + " " + savedAccount.getCurrency()
		);

		return new BalanceResponse(savedAccount.getId(), savedAccount.getAccountNumber(), savedAccount.getBalance(),
				savedAccount.getCurrency());
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

		transactionService.recordTransaction(savedAccount, TransactionType.WITHDRAWAL, request.getAmount(),
				"Withdrawal from account");
		
		auditService.log(
		        savedAccount.getCustomer().getUser(),
		        "AMOUNT_WITHDRAWN",
		        "ACCOUNT",
		        savedAccount.getId().toString(),
		        "Withdrawn " + request.getAmount() + " " + savedAccount.getCurrency()
		);

		return new BalanceResponse(savedAccount.getId(), savedAccount.getAccountNumber(), savedAccount.getBalance(),
				savedAccount.getCurrency());
	}
	@Transactional(readOnly = true)
	public PagedResponse<TransactionResponse> getMyAccountTransactions(
	        String email,
	        Long accountId,
	        TransactionType type,
	        TransactionStatus status,
	        int page,
	        int size) {

	    Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
	            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

	    return transactionService.searchTransactionsForAccount(
	            account.getId(),
	            type,
	            status,
	            page,
	            size
	    );
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
		
		auditService.log(
		        savedAccount.getCustomer().getUser(),
		        "ACCOUNT_CLOSED",
		        "ACCOUNT",
		        savedAccount.getId().toString(),
		        "Closed account " + savedAccount.getAccountNumber()
		);

		return mapToResponse(savedAccount);
	}

	@Transactional
	public AccountResponse freezeAccount(Long accountId) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		if (account.getStatus() == AccountStatus.CLOSED) {
			throw new InvalidAccountStateException("Closed account cannot be frozen");
		}

		if (account.getStatus() == AccountStatus.FROZEN) {
			throw new InvalidAccountStateException("Account is already frozen");
		}

		account.freeze();

		Account savedAccount = accountRepository.save(account);
		
		auditService.log(
		        savedAccount.getCustomer().getUser(),
		        "ACCOUNT_FROZEN",
		        "ACCOUNT",
		        savedAccount.getId().toString(),
		        "Frozen account " + savedAccount.getAccountNumber()
		);

		return mapToResponse(savedAccount);
	}

	@Transactional
	public AccountResponse unfreezeAccount(Long accountId) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		if (account.getStatus() == AccountStatus.CLOSED) {
			throw new InvalidAccountStateException("Closed account cannot be unfrozen");
		}

		if (account.getStatus() == AccountStatus.ACTIVE) {
			throw new InvalidAccountStateException("Account is already active");
		}

		account.unfreeze();

		Account savedAccount = accountRepository.save(account);
		
		auditService.log(
		        savedAccount.getCustomer().getUser(),
		        "ACCOUNT_UNFROZEN",
		        "ACCOUNT",
		        savedAccount.getId().toString(),
		        "Unfrozen account " + savedAccount.getAccountNumber()
		);

		return mapToResponse(savedAccount);
	}
	
	@Transactional(readOnly = true)
	public PagedResponse<AccountResponse> searchAccounts(String email, AccountStatus status,
	                                                     AccountType accountType, int page, int size) {
	    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

	    Specification<Account> spec = Specification.where(null);

	    if (email != null && !email.isBlank()) {
	        spec = spec.and((root, query, cb) ->
	                cb.like(root.get("customer").get("user").get("email"), "%" + email + "%"));
	    }

	    if (status != null) {
	        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
	    }

	    if (accountType != null) {
	        spec = spec.and((root, query, cb) -> cb.equal(root.get("accountType"), accountType));
	    }

	    Page<Account> accountPage = accountRepository.findAll(spec, pageable);

	    return new PagedResponse<>(
	            accountPage.getContent().stream()
	                    .map(this::mapToResponse)
	                    .toList(),
	            accountPage.getNumber(),
	            accountPage.getSize(),
	            accountPage.getTotalElements(),
	            accountPage.getTotalPages(),
	            accountPage.isLast()
	    );
	}
}