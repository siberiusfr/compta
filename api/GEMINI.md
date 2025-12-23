# Project Overview

This is a multi-module Java Spring Boot project with a microservices architecture. It uses Maven as the build tool and primarily targets Java 21. The project leverages PostgreSQL for its database, with schema isolation per service managed by Flyway for database migrations. Sentry is integrated across all services for error tracking and performance monitoring. Additionally, Node.js with pnpm and Prettier is used for code formatting of various non-Java files (XML, SQL, YAML, JSON).

## Core Technologies:
*   **Backend:** Java 21, Spring Boot 3.5.x
*   **Build Tool:** Apache Maven
*   **Database:** PostgreSQL
*   **Database Migrations:** Flyway (decentralized with schema isolation)
*   **ORM/DAO:** JOOQ
*   **API Documentation:** Springdoc OpenAPI
*   **Error Tracking & Performance Monitoring:** Sentry
*   **Code Quality:** Checkstyle, JaCoCo (test coverage), fmt-maven-plugin (Google Java Format)
*   **Code Formatting:** Node.js, pnpm, Prettier (for XML, SQL, YAML, JSON)
*   **Mapping:** MapStruct
*   **Boilerplate reduction:** Lombok
*   **Testing:** Testcontainers

## Architecture:
The project follows a microservices architecture, with each service running as a separate Spring Boot application. Key services include:
*   `accounting-service`
*   `auth-service`
*   `authz-service`
*   `document-service`
*   `hr-service`
*   `migration-service` (read-only for migration status)
*   `notification-service`

Database migrations are decentralized, with each service managing its own schema in PostgreSQL using Flyway. The `compta-commons` module provides shared configurations and utilities across services.

# Building and Running

## Prerequisites
*   Java 21 JDK
*   Maven (or use `mvnw` wrapper)
*   pnpm (for code formatting tasks)
*   PostgreSQL database (configured per `MIGRATION_ARCHITECTURE.md`)

## Build the entire project
```bash
./mvnw clean install
```

## Run a specific service
Navigate to the service directory and run the Spring Boot application:
```bash
cd accounting-service
../mvnw spring-boot:run
```
*(Replace `accounting-service` with the desired service directory)*

## Run all services (TODO: needs a `docker-compose.yml` or similar for easier orchestration)
Currently, each service needs to be run individually. For local development, it's recommended to start the necessary PostgreSQL instances and then run the desired services.

## Database Migrations
Migrations are applied automatically by each service on startup. To consult the migration status, run the `migration-service`:
```bash
cd migration-service
../mvnw spring-boot:run
# Then query its endpoints, e.g.:
curl http://localhost:8081/api/migrations/info
```

## Code Formatting
To format non-Java files (XML, SQL, YAML, JSON) using Prettier:
```bash
pnpm format
```
To check formatting without applying changes:
```bash
pnpm format:check
```

# Development Conventions

## Code Style
*   Java code adheres to Google Java Format, enforced by `fmt-maven-plugin`.
*   Checkstyle is used for static analysis, configured via `checkstyle.xml`.
*   Non-Java files (XML, SQL, YAML, JSON) are formatted using Prettier.

## Database Migrations
*   Follows Flyway naming conventions: `V{version}__{description}.sql`.
*   Migrations are idempotent, using `IF NOT EXISTS` and `IF EXISTS`.
*   Each service operates within its dedicated PostgreSQL schema (e.g., `accounting.table_name`).
*   Direct foreign key references between service schemas are discouraged.

## Error Tracking
*   Sentry is used for error and performance monitoring.
*   Environment variables `SENTRY_DSN` and `SENTRY_ENVIRONMENT` are required for Sentry activation.
*   PII filtering is enabled by default (`send-default-pii: false`).
*   Recommended `traces-sample-rate`: 1.0 (dev), 0.5 (staging), 0.1 (production).
