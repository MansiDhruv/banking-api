package com.bank.banking_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI bankingApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking API")
                        .version("v1")
                        .description("REST API for banking operations including customers, accounts, transactions, and transfers.")
                        .contact(new Contact()
                                .name("Banking API Team")
                                .email("support@bankingapi.com")));
    }
}