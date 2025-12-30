# Accounting Service - AGENTS Documentation

## Overview

The **Accounting Service** is responsible for managing all accounting-related business logic in the COMPTA ERP system. It handles company management, chart of accounts, journal entries, and financial transaction records.

**Service Name:** accounting-service  
**Port:** 8084  
**Context Path:** / (root)  
**Database Schema:** `accounting`

---

## Purpose and Responsibilities

The Accounting Service is the core financial engine of the system, providing:

- **Company Management**: Store and manage company information (SIRET, address, contact details)
- **Chart of Accounts**: Maintain hierarchical account structures with account numbers, types, and balances
- **Journal Entries**: Record financial transactions with proper double-entry bookkeeping
- **Account Balances**: Track and update account balances in real-time
- **Financial Reporting**: Provide data for generating financial statements

---

## Technologies and Frameworks

### Core Framework
- **Spring Boot 3.x** - Application framework
- **Java 21** - Programming language

### Database & ORM
- **PostgreSQL** - Primary database
- **Flyway** - Database migration management
- **jOOQ** - Type-safe SQL query builder (code generation enabled)

### Build Tools
- **Maven** - Build and dependency management

### Monitoring & Observability
- **Sentry** - Error tracking and performance monitoring
- **Spring Boot Actuator** - Health checks and metrics (via compta-commons)

### Testing
- **Spring Boot Test** - Testing framework
- **Testcontainers** - Integration testing with PostgreSQL
- **JUnit 5** - Unit testing

---

## API Endpoints

> **Note:** This service is currently in early development. API endpoints are being implemented.

### Planned Endpoints

| Method | Path | Description | Authentication |
|--------|------|-------------|----------------|
| GET | `/api/companies` | List all companies | Required |
| POST | `/api/companies` | Create a new company | Required |
| GET | `/api/companies/{id}` | Get company details | Required |
| PUT | `/api/companies/{id}` | Update company information | Required |
| DELETE | `/api/companies/{id}` | Delete a company | Required |
| GET | `/api/accounts` | List chart of accounts | Required |
| POST | `/api/accounts` | Create a new account | Required |
| GET | `/api/accounts/{id}` | Get account details | Required |
| PUT | `/api/accounts/{id}` | Update account | Required |
| GET | `/api/journal-entries` | List journal entries | Required |
| POST | `/api/journal-entries` | Create journal entry | Required |
| GET | `/api/journal-entries/{id}` | Get journal entry details | Required |

---

## Dependencies on Other Services

### External Dependencies
- **PostgreSQL Database** - Stores accounting data in the `accounting` schema
- **Auth Service** - Validates JWT tokens for user authentication (via compta-commons security module)

### Internal Dependencies
- **compta-commons** - Shared utilities, security, and common configurations

---

## Configuration Details

### Application Configuration (`application.yml`)

```yaml
spring:
  application:
    name: accounting-service
  datasource:
    url: jdbc:postgresql://localhost:5432/compta
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    schemas: accounting
    default-schema: accounting

server:
  port: 8084

sentry:
  dsn: ${SENTRY_DSN:}
  environment: ${SENTRY_ENVIRONMENT:development}
  traces-sample-rate: 1.0
  send-default-pii: false
  enable-tracing: true
  tags:
    service: accounting-service
    module: accounting
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SENTRY_DSN` | Sentry DSN for error tracking | (empty) |
| `SENTRY_ENVIRONMENT` | Sentry environment name | `development` |

---

## Database Schema

### Schema: `accounting`

#### Tables

##### `companies`
Stores company information for accounting purposes.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `name` | VARCHAR(255) | Company name (NOT NULL) |
| `siret` | VARCHAR(14) | French company identifier |
| `address` | TEXT | Company address |
| `phone` | VARCHAR(20) | Contact phone number |
| `email` | VARCHAR(255) | Contact email |
| `created_at` | TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMP | Last update timestamp |

**Indexes:**
- `idx_companies_name` on `name`

##### `accounts`
Chart of accounts with hierarchical structure.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `account_number` | VARCHAR(20) | Account code (NOT NULL) |
| `account_name` | VARCHAR(255) | Account name (NOT NULL) |
| `account_type` | VARCHAR(50) | Account type (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE) |
| `parent_account_id` | BIGINT | Parent account for hierarchy |
| `company_id` | BIGINT | Foreign key to companies (NOT NULL) |
| `balance` | DECIMAL(15,2) | Current account balance |
| `is_active` | BOOLEAN | Active status |
| `created_at` | TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMP | Last update timestamp |

**Constraints:**
- UNIQUE on `(company_id, account_number)`
- Foreign key to `accounts(id)` for parent-child relationship
- Foreign key to `companies(id)`

**Indexes:**
- `idx_accounts_company` on `company_id`
- `idx_accounts_type` on `account_type`
- `idx_accounts_parent` on `parent_account_id`
- `idx_accounts_number` on `account_number`

##### `journal_entries`
Header records for financial transactions.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `entry_date` | DATE | Transaction date (NOT NULL) |
| `description` | TEXT | Transaction description |
| `reference` | VARCHAR(100) | Reference number |
| `company_id` | BIGINT | Foreign key to companies (NOT NULL) |
| `created_by` | VARCHAR(100) | User who created the entry |
| `created_at` | TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMP | Last update timestamp |

**Foreign key to `companies(id)`**

**Indexes:**
- `idx_journal_entries_company` on `company_id`
- `idx_journal_entries_date` on `entry_date`

##### `journal_entry_lines`
Line items for journal entries (double-entry bookkeeping).

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `journal_entry_id` | BIGINT | Foreign key to journal_entries (NOT NULL) |
| `account_id` | BIGINT | Foreign key to accounts (NOT NULL) |
| `debit` | DECIMAL(15,2) | Debit amount |
| `credit` | DECIMAL(15,2) | Credit amount |
| `description` | TEXT | Line description |
| `created_at` | TIMESTAMP | Creation timestamp |

**Constraints:**
- Foreign key to `journal_entries(id)` with CASCADE DELETE
- Foreign key to `accounts(id)`

**Indexes:**
- `idx_journal_entry_lines_entry` on `journal_entry_id`
- `idx_journal_entry_lines_account` on `account_id`

### Database Triggers

Automatic `updated_at` timestamp updates for:
- `companies` table
- `accounts` table
- `journal_entries` table

---

## Key Classes and Responsibilities

### Main Application Class
- **`AccountingServiceApplication`** - Spring Boot application entry point

### Package Structure
```
tn.cyberious.compta
├── AccountingServiceApplication.java
├── auth/           # Authentication-related code
└── authz/          # Authorization-related code
```

> **Note:** The service is in early development. Controller, Service, and Repository classes are yet to be implemented.

### Planned Architecture

#### Controllers
- `CompanyController` - Company CRUD operations
- `AccountController` - Chart of accounts management
- `JournalEntryController` - Journal entry operations

#### Services
- `CompanyService` - Company business logic
- `AccountService` - Account management logic
- `JournalEntryService` - Journal entry processing with double-entry validation

#### Repositories (jOOQ-generated)
- `CompaniesDao` - Company data access
- `AccountsDao` - Account data access
- `JournalEntriesDao` - Journal entry data access
- `JournalEntryLinesDao` - Journal entry line data access

---

## Development Notes

### Running the Service

```bash
# Build the service
mvn clean install

# Run the service
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Database Migration

Flyway migrations are located in `src/main/resources/db/migration/`

To add a new migration:
1. Create a new SQL file with version prefix (e.g., `V2__add_feature.sql`)
2. Flyway will automatically apply it on startup

### jOOQ Code Generation

The service uses jOOQ for type-safe SQL queries. Code is generated from the database schema:

```xml
<properties>
  <jooq.generator.db.schema>accounting</jooq.generator.db.schema>
  <jooq.generator.target.package>tn.cyberious.compta.accounting.generated</jooq.generator.target.package>
</properties>
```

Generated classes are placed in `src/generated/jooq/`

### Testing

Integration tests use Testcontainers with PostgreSQL:

```bash
# Run tests
mvn test
```

---

## Security

The service uses the shared security module from `compta-commons`:

- JWT-based authentication
- Role-based access control (RBAC)
- Public endpoints for health checks
- Protected endpoints for all business operations

---

## Monitoring

### Sentry Integration
- Error tracking enabled
- Distributed tracing with 100% sampling in development
- Service tags: `service=accounting-service`, `module=accounting`

### Health Checks
Available via Spring Boot Actuator (configured in compta-commons)

---

## Future Enhancements

- Implement REST API controllers
- Add business logic services
- Create DTOs for request/response
- Implement double-entry validation
- Add financial reporting endpoints
- Implement account balance calculations
- Add transaction rollback capabilities
