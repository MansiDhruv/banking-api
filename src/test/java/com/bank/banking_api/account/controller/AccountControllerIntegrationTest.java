package com.bank.banking_api.account.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void accountFlow_ShouldCreateAccountDepositAndReturnBalance() throws Exception {
        String email = "account.flow@example.com";
        String password = "Password123";

        String registerBody = """
                {
                  "email": "%s",
                  "password": "%s",
                  "firstName": "Account",
                  "lastName": "Flow",
                  "phone": "1234567890",
                  "dateOfBirth": "1995-05-15"
                }
                """.formatted(email, password);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = JsonPath.read(loginResponse, "$.data.accessToken");

        String createAccountBody = """
                {
                  "accountType": "SAVINGS",
                  "currency": "USD"
                }
                """;

        String createAccountResponse = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAccountBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer accountId = JsonPath.read(createAccountResponse, "$.data.accountId");

        String depositBody = """
                {
                  "amount": 500.00
                }
                """;

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(500.0000));

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/balance")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(500.0000))
                .andExpect(jsonPath("$.data.currency").value("USD"));
    }
}