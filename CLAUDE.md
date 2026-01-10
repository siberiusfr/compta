# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

COMPTA is a French accounting ERP application built on a microservices architecture. It manages accounting, HR, document management, and notifications for multi-tenant companies.

**Tech Stack:**
- Backend: Java 21, Spring Boot 3.5.9, jOOQ, PostgreSQL 16
- Notification Service: NestJS, Prisma, BullMQ
- Frontend: Vue 3, TypeScript, Vite, TailwindCSS, Pinia
- Infrastructure: Redis 8.4, RabbitMQ 4.2, MinIO (S3-compatible storage)

## Build Commands

### Backend (from `api/` directory)

```bash
# Build all services
mvn clean install

# Build single service
mvn clean install -pl auth-service -am

# Run single service in dev mode
cd auth-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Run single test class
mvn test -Dtest=AuthServiceTest

# Format code (Spotless + Google Java Format)
mvn spotless:apply

# Check formatting
mvn spotless:check

# Regenerate jOOQ classes after DB schema changes
mvn clean generate-sources
```

### Frontend (from `frontend/` directory)

```bash
pnpm install
pnpm run dev          # Development server
pnpm run build        # Production build (vue-tsc + vite)
pnpm run api:generate # Generate API client from OpenAPI (Orval)
```

### Notification Service (from `api/notification-service/` directory)

```bash
pnpm install
pnpm run start:dev    # Development with hot reload
pnpm run build
pnpm run test
pnpm run test:e2e
npx prisma generate   # Regenerate Prisma client
npx prisma migrate dev # Create new migration
```

### Docker

```bash
docker-compose up -d              # Start all services
docker-compose logs -f auth-service  # View service logs
docker-compose down               # Stop all services
```

## Architecture

### Service Layout

All backend services are in `api/`:

| Service | Port | Purpose |
|---------|------|---------|
| gateway | 8080 | Spring Cloud Gateway, JWT validation, rate limiting |
| auth-service | 8081 | Authentication, users, companies, roles |
| accounting-service | 8082 | Chart of accounts, journal entries, tax |
| document-service | 8083 | Document storage (MinIO), versioning, OCR |
| hr-service | 8084 | Employees, payroll, leave management |
| notification-service | 3001 | Email/SMS/Push via BullMQ queues (NestJS) |
| migration-service | - | Flyway migrations (runs once on startup) |

### Shared Modules

- **compta-commons**: Base Spring Boot configuration, JPA, OpenAPI, test utilities
- **compta-security-commons**: JWT utilities, encryption helpers
- **notification-contracts**: DTOs shared between Java services and notification-service

### Database Architecture

Single PostgreSQL database with **schema-based isolation**:
- Each service owns its schema (`auth`, `accounting`, `hr`, `document`, `notification`)
- Migrations centralized in `migration-service/src/main/resources/db/migration/`
- jOOQ generates type-safe query classes per service (in `src/generated/jooq/`)

### Multi-Tenancy

- Header `X-Company-Id` required on authenticated requests
- Data isolated by `company_id` column
- Users can belong to multiple companies with different roles

### Authentication Flow

1. `POST /api/auth/login` returns JWT access + refresh tokens
2. Gateway validates JWT on all requests to downstream services
3. Roles: ADMIN, COMPTABLE (accountant), SOCIETE (company owner), EMPLOYEE

## Key Patterns

### jOOQ Repositories (Java services)

```java
@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final DSLContext dsl;

    public Optional<Users> findByUsername(String username) {
        return dsl.selectFrom(USERS)
            .where(USERS.USERNAME.eq(username))
            .fetchOptional()
            .map(r -> r.into(Users.class));
    }
}
```

### Adding Database Migrations

1. Create `VX__description.sql` in `api/migration-service/src/main/resources/db/migration/`
2. Always prefix tables with schema: `CREATE TABLE auth.new_table (...)`
3. Restart migration-service or full stack
4. Run `mvn clean generate-sources` in affected service to regenerate jOOQ classes

### API Documentation

All services expose OpenAPI at `/swagger-ui.html` (gateway proxies all at `:8080`)

### Frontend API Client Generation

The frontend uses Orval to generate typed API clients from OpenAPI specs:
```bash
cd frontend && pnpm run api:generate
```

## Service-Specific Documentation

Each service has its own CLAUDE.md with detailed guidance:
- `api/gateway/CLAUDE.md` - Routing, security filters, resilience patterns
- `api/auth-service/CLAUDE.md` - Auth schema, endpoints, jOOQ patterns
- `api/notification-service/CLAUDE.md` - NestJS patterns, Prisma, BullMQ jobs
- `api/authz-service/CLAUDE.md` - Permission management

## Access Points

| Interface | URL |
|-----------|-----|
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| RabbitMQ UI | http://localhost:15672 (guest/guest) |
| MinIO Console | http://localhost:9001 (minioadmin/minioadmin) |
