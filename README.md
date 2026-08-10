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
- Docker
- Docker Compose

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
- Admin customer search and detail APIs
- KYC status update by admin/bank staff
- Admin account search with filters
- Account freeze and unfreeze by admin/bank staff
- Audit log APIs with pagination and filtering
- Transaction history with pagination and filtering
- Transfer history with pagination and filtering
- Docker support with MySQL and Spring Boot using Docker Compose

## APIs Implemented

### Health

```http
GET /api/v1/health
GET /api/v1/health/error
POST /api/v1/health/validate
```

### Authentication

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
GET /api/v1/auth/me
```

### Customer

```http
GET /api/v1/customers/me
```

### Accounts

```http
POST /api/v1/accounts
GET /api/v1/accounts
GET /api/v1/accounts/{accountId}
GET /api/v1/accounts/{accountId}/balance
POST /api/v1/accounts/{accountId}/deposit
POST /api/v1/accounts/{accountId}/withdraw
GET /api/v1/accounts/{accountId}/transactions
PATCH /api/v1/accounts/{accountId}/close
GET /api/v1/accounts/{accountId}/transactions
```

### Transfers

```http
POST /api/v1/transfers
GET /api/v1/transfers
GET /api/v1/transfers/{transferId}
```

### Admin Customers

```http
GET /api/v1/admin/customers
GET /api/v1/admin/customers/{customerId}
PATCH /api/v1/admin/customers/{customerId}/kyc-status
```


### Admin Accounts

```http
GET /api/v1/admin/accounts
PATCH /api/v1/admin/accounts/{accountId}/freeze
PATCH /api/v1/admin/accounts/{accountId}/unfreeze

GET /api/v1/admin/audit-logs
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
V7__create_audit_logs_table.sql


## Run With Docker

This project includes Docker support for running the Banking API with a MySQL database.

### Prerequisites

- Docker Desktop
- Maven

### Build Application Jar

```bash
mvn clean package -DskipTests
```

### Start Application With Docker Compose

```bash
docker compose up --build
```

This starts:

```text
banking-api
banking-mysql
```

### Access Application

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health API:

```text
http://localhost:8080/api/v1/health
```

### MySQL Docker Details

Docker MySQL runs inside the container on port `3306`.

It is exposed on the local machine as:

```text
localhost:3307
```

Database name:

```text
banking_api_db
```

Default Docker database credentials used in `docker-compose.yml`:

```text
Username: root
Password: root
```

The Spring Boot app connects to MySQL inside Docker using the Docker service name:

```text
jdbc:mysql://mysql:3306/banking_api_db
```

### Stop Containers

```bash
docker compose down
```

### Remove Containers And Database Volume

Use this only if you want to delete Docker MySQL data and start fresh:

```bash
docker compose down -v
```

After running with `-v`, Flyway migrations will run again and recreate the schema.

## Postman Collection

Postman collection and local environment files are available in:

```text
postman/
Import both files into Postman:
Banking-API.postman_collection.json
Banking-API-Local.postman_environment.json

Select the Banking API Local environment before running requests.
The collection includes flows for:
Health check
Registration and login
Customer profile
Account creation
Deposit and withdrawal
Transaction history
Transfers
Admin customer/account APIs
Audit logs
```

## Upcoming Features

- Refresh token flow
- Unit tests
- Integration tests
- Final Postman collection
- Architecture and database diagrams