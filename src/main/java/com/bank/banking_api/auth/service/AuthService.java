package com.bank.banking_api.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.banking_api.auth.dto.RegisterRequest;
import com.bank.banking_api.auth.dto.RegisterResponse;
import com.bank.banking_api.common.enums.Role;
import com.bank.banking_api.common.enums.UserStatus;
import com.bank.banking_api.common.exception.DuplicateResourceException;
import com.bank.banking_api.user.entity.User;
import com.bank.banking_api.user.repository.UserRepository;


import com.bank.banking_api.auth.dto.LoginRequest;
import com.bank.banking_api.auth.dto.LoginResponse;
import com.bank.banking_api.common.exception.InvalidCredentialsException;
import com.bank.banking_api.customer.entity.Customer;
import com.bank.banking_api.customer.repository.CustomerRepository;
import com.bank.banking_api.security.JwtTokenProvider;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomerRepository customerRepository;
    
	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider, CustomerRepository customerRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.customerRepository = customerRepository;
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request) {
	    if (userRepository.existsByEmail(request.getEmail())) {
	        throw new DuplicateResourceException("Email already registered");
	    }

	    String hashedPassword = passwordEncoder.encode(request.getPassword());

	    User user = new User(
	            request.getEmail(),
	            hashedPassword,
	            Role.CUSTOMER,
	            UserStatus.ACTIVE
	    );

	    User savedUser = userRepository.save(user);

	    Customer customer = new Customer(
	            savedUser,
	            request.getFirstName(),
	            request.getLastName(),
	            request.getPhone(),
	            request.getDateOfBirth()
	    );

	    Customer savedCustomer = customerRepository.save(customer);

	    return new RegisterResponse(
	            savedUser.getId(),
	            savedCustomer.getId(),
	            savedUser.getEmail(),
	            savedCustomer.getFirstName() + " " + savedCustomer.getLastName(),
	            savedUser.getRole().name(),
	            savedUser.getStatus().name(),
	            savedCustomer.getKycStatus().name()
	    );
	}
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("User account is not active");
        }
        
        String accessToken = jwtTokenProvider.generateToken(user);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                accessToken,
                "Bearer"
        );
    }
}