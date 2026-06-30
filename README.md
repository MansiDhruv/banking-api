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

- Project foundation with layered package structure
- MySQL database connection
- Flyway database migration setup
- Swagger/OpenAPI documentation
- Basic Spring Security configuration
- Health check API
- Standard API response wrapper
- Global exception handling
- Validation error handling
- User entity and users table migration
- User registration API
- BCrypt password hashing
- Duplicate email validation

## APIs Implemented

### Health

```http
GET /api/v1/health
GET /api/v1/health/error
POST /api/v1/health/validate

Authentication
POST /api/v1/auth/register

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
Migration Notes
Flyway migration files are stored in:
src/main/resources/db/migration
Example:
V1__create_app_info_table.sql
V2__create_users_table.sql
Do not modify already-applied migration files. Create a new versioned migration instead.

Upcoming Features
Login API
JWT token generation and validation
Customer profile management
Account creation and balance inquiry
Deposit and withdrawal
Internal transfers
Transaction history
Admin APIs
Audit logging
Unit and integration tests