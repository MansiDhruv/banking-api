package com.bank.banking_api.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bank.banking_api.audit.service.AuditService;
import com.bank.banking_api.auth.dto.LoginRequest;
import com.bank.banking_api.auth.dto.LoginResponse;
import com.bank.banking_api.auth.dto.RegisterRequest;
import com.bank.banking_api.auth.dto.RegisterResponse;
import com.bank.banking_api.common.enums.Role;
import com.bank.banking_api.common.enums.UserStatus;
import com.bank.banking_api.common.exception.DuplicateResourceException;
import com.bank.banking_api.common.exception.InvalidCredentialsException;
import com.bank.banking_api.customer.entity.Customer;
import com.bank.banking_api.customer.repository.CustomerRepository;
import com.bank.banking_api.security.JwtTokenProvider;
import com.bank.banking_api.user.entity.User;
import com.bank.banking_api.user.repository.UserRepository;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User savedUser;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setPhone("1234567890");
        registerRequest.setDateOfBirth(LocalDate.of(1995, 5, 15));

        savedUser = new User(
                "test@example.com",
                "hashedPassword",
                Role.CUSTOMER,
                UserStatus.ACTIVE
        );
        
        setId(savedUser, 1L);

        savedCustomer = new Customer(
                savedUser,
                "Test",
                "User",
                "1234567890",
                LocalDate.of(1995, 5, 15)
        );
        
        setId(savedCustomer, 1L);
    }
    @Test
    void register_ShouldCreateUserAndCustomer_WhenEmailDoesNotExist() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        RegisterResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("PENDING", response.getKycStatus());

        verify(userRepository).save(any(User.class));
        verify(customerRepository).save(any(Customer.class));
        verify(auditService).log(any(User.class), any(String.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void register_ShouldThrowDuplicateResourceException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), savedUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(savedUser)).thenReturn("jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());

        verify(auditService).log(any(User.class), any(String.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void login_ShouldThrowInvalidCredentialsException_WhenPasswordIsWrong() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), savedUser.getPasswordHash())).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }
    
    private void setId(Object target, Long id) {
        try {
            var field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}