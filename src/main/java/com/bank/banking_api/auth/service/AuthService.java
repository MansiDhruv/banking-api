package com.bank.banking_api.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.bank.banking_api.security.JwtTokenProvider;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

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

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getStatus().name()
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