package com.chatbot.chatbot_service;

import com.chatbot.chatbot_service.DTOs.RegisterRequest;
import com.chatbot.chatbot_service.models.Account;
import com.chatbot.chatbot_service.repositories.AccountRepository;
import com.chatbot.chatbot_service.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterSuccess() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserName("testUser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        when(accountRepository.existsByUserName("testUser")).thenReturn(false);
        when(accountRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L);
            account.setCreatedAt(LocalDateTime.now());
            return account;
        });

        // Act
        ResponseEntity<?> response = accountService.save(registerRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account Created Successfully!", response.getBody());
    }

    @Test
    public void testRegisterFailDuplicateUsername() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserName("testUser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        when(accountRepository.existsByUserName("testUser")).thenReturn(true);

        // Act
        ResponseEntity<?> response = accountService.save(registerRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username already exists!", response.getBody());
    }

    @Test
    public void testRegisterFailDuplicateEmail() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUserName("uniqueUser");
        registerRequest.setEmail("duplicate@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        when(accountRepository.existsByUserName("uniqueUser")).thenReturn(false);
        when(accountRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        // Act
        ResponseEntity<?> response = accountService.save(registerRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email is already in use!", response.getBody());
    }
}
