# Banking API

A Spring Boot based Banking REST API built with Java 17, MySQL, Spring Security, JWT planned, Hibernate/JPA, Flyway, Validation, and Swagger/OpenAPI.

## Tech Stack

- Java 17
- Spring Boot 3.5.16
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- MySQL
- Flyway
- Bean Validation
- Swagger / OpenAPI
- Maven

## Current Features

- Spring Boot project foundation with layered package structure
- MySQL database connection
- Flyway database migrations
- Swagger/OpenAPI documentation
- Spring Security with JWT authentication
- BCrypt password hashing
- Standard API response wrapper
- Global exception handling
- Validation error handling
- User registration and login
- BCrypt password hashing
- Duplicate email validation
- Customer profile creation during registration
- Authenticated customer profile API
- Account creation
- Account listing and balance inquiry
- Deposit and withdrawal
- Transaction recording for money movement
- Transaction history by account
- Internal account-to-account transfer
- Transfer history for source account owner

## APIs Implemented

### Health


GET /api/v1/health
GET /api/v1/health/error
POST /api/v1/health/validate


### Authentication

POST /api/v1/auth/register
POST /api/v1/auth/login
GET /api/v1/auth/me


### Customer


GET /api/v1/customers/me


### Accounts


POST /api/v1/accounts
GET /api/v1/accounts
GET /api/v1/accounts/{accountId}
GET /api/v1/accounts/{accountId}/balance
POST /api/v1/accounts/{accountId}/deposit
POST /api/v1/accounts/{accountId}/withdraw
GET /api/v1/accounts/{accountId}/transactions
```

### Transfers


POST /api/v1/transfers
GET /api/v1/transfers
```
Database Setup
Create the database in MySQL:
CREATE DATABASE banking_api_db;
Copy the example configuration:
src/main/resources/application-dev.example.properties
Create your local configuration:
src/main/resources/application-dev.properties
Set your local MySQL password using an environment variable:
spring.datasource.password=${DB_PASSWORD}

In Eclipse, add environment variable:
DB_PASSWORD=your_mysql_password

Run Locally
Start the application from Eclipse:
Run As > Java Application
The app runs on:
http://localhost:8080
Swagger UI:
http://localhost:8080/swagger-ui.html
Health API:
http://localhost:8080/api/v1/health


# Migration Notes
Flyway migration files are stored in:
src/main/resources/db/migration
Example:
## Migration Notes

Flyway migration files are stored in:

src/main/resources/db/migration


Current migrations:
V1__create_app_info_table.sql
V2__create_users_table.sql
V3__create_customers_table.sql
V4__create_accounts_table.sql
V5__create_account_transactions_table.sql
V6__create_transfers_table.sql

Do not modify already-applied migration files. Create a new versioned migration instead.

# Upcoming Features
- Get transfer by ID
- Account freeze, unfreeze, and close APIs
- Admin and bank staff role-based APIs
- Audit logging
- Refresh token flow
- Unit and integration tests
- Docker support