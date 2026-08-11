package com.bank.banking_api.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_ShouldReturnSuccess_WhenRequestIsValid() throws Exception {
        String requestBody = """
                {
                  "email": "integration.user@example.com",
                  "password": "Password123",
                  "firstName": "Integration",
                  "lastName": "User",
                  "phone": "1234567890",
                  "dateOfBirth": "1995-05-15"
                }
                """;

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User registered successfully"))
				.andExpect(jsonPath("$.data.email").value("integration.user@example.com"))
				.andExpect(jsonPath("$.data.role").value("CUSTOMER"))
				.andExpect(jsonPath("$.data.kycStatus").value("PENDING"));
	}

    @Test
    void login_ShouldReturnAccessToken_WhenCredentialsAreValid() throws Exception {
        String registerBody = """
                {
                  "email": "integration.login@example.com",
                  "password": "Password123",
                  "firstName": "Login",
                  "lastName": "User",
                  "phone": "1234567890",
                  "dateOfBirth": "1995-05-15"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                  "email": "integration.login@example.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.email").value("integration.login@example.com"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }
}