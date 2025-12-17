# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **auth-service** microservice, part of a larger multi-service accounting application (Compta API). It handles authentication, authorization, and user management for the entire system.

**Technology Stack:**
- Spring Boot 3.5.8 (Java 21)
- PostgreSQL with dedicated `auth` schema
- jOOQ for type-safe SQL queries
- JWT (via jjwt) for authentication
- Flyway for database migrations
- Spring Security for authentication/authorization

**Port:** 8083

## Commands

### Build and Run
```bash
# Build the service (from auth-service directory)
mvn clean install

# Run the service
mvn spring-boot:run

# Build entire project (from parent api directory)
cd .. && mvn clean install
```

### Database

**Database migrations are automatic** - Flyway runs on startup and applies migrations from `src/main/resources/db/migration/`.

```bash
# Regenerate jOOQ classes after schema changes
mvn clean generate-sources

# This runs the jooq-codegen-maven plugin which:
# - Connects to the PostgreSQL database
# - Reads the 'auth' schema structure
# - Generates Java classes in src/main/java/tn/cyberious/compta/auth/generated/
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=AuthServiceApplicationTests

# Run with coverage report (JaCoCo)
mvn test jacoco:report
# Report available at: target/site/jacoco/index.html
```

## Architecture

### Multi-Service Context

This service is part of a **microservices architecture** with schema-based isolation:

| Service | Schema | Port | Purpose |
|---------|--------|------|---------|
| auth-service | `auth` | 8083 | Authentication & user management |
| accounting-service | `accounting` | 8082 | Accounting operations |
| hr-service | `hr` | 8084 | Human resources |
| document-service | `document` | 8085 | Document management |
| notification-service | `notification` | 8086 | Notifications |
| migration-service | - | 8081 | Migration status (read-only) |

**All services share the same PostgreSQL database but use different schemas for data isolation.**

### Dependency Architecture

```
auth-service
  └── compta-commons (shared module)
       ├── Spring Boot starters (web, jooq, validation, actuator)
       ├── PostgreSQL + Flyway
       ├── Jackson configuration
       ├── MapStruct
       ├── OpenAPI/Swagger
       └── Testcontainers for integration tests
```

The `compta-commons` module provides shared configurations and dependencies. Any service-specific configuration overrides the commons defaults.

### Database Schema: `auth`

The service owns the `auth` PostgreSQL schema with these core tables:

**User Management:**
- `users` - User accounts (username, email, password, status)
- `roles` - System roles (ADMIN, COMPTABLE, SOCIETE, EMPLOYEE)
- `user_roles` - Many-to-many relationship between users and roles

**Company Management:**
- `societes` - Companies (raison_sociale, matricule_fiscale, etc.)
- `comptable_societes` - Comptable-to-company assignments (one comptable manages many companies)
- `user_societes` - User-to-company assignments (SOCIETE role users can own multiple companies)
- `employees` - Employee-to-company assignments (one employee belongs to ONE company)

**Security & Audit:**
- `refresh_tokens` - JWT refresh tokens
- `auth_logs` - Authentication audit trail (login attempts, success/failure)

**Key Design Principles:**
- Role-based access control with 4 roles: ADMIN, COMPTABLE, SOCIETE, EMPLOYEE
- Each user can have multiple roles
- COMPTABLEs can manage multiple companies
- SOCIETE users can own multiple companies
- EMPLOYEEs belong to exactly one company
- Account locking after 5 failed login attempts

### jOOQ Generated Code

The `src/main/java/tn/cyberious/compta/auth/generated/` directory contains **generated code** from the database schema:

```
auth/generated/
├── Tables.java              # Table constants
├── Keys.java                # Foreign key definitions
├── Indexes.java             # Index definitions
├── tables/
│   ├── Users.java           # Table class
│   ├── Roles.java
│   ├── UserRoles.java
│   ├── Societes.java
│   ├── Employees.java
│   └── ...
├── tables/records/          # Record classes (database rows)
│   ├── UsersRecord.java
│   └── ...
├── tables/pojos/            # POJO classes (data transfer)
│   ├── Users.java
│   └── ...
└── tables/daos/             # DAO classes (basic CRUD)
    ├── UsersDao.java
    └── ...
```

**IMPORTANT:** Never edit generated code manually. To modify the schema:
1. Create a new Flyway migration (e.g., `V2__add_new_column.sql`)
2. Run `mvn clean generate-sources` to regenerate jOOQ classes

### Code Structure

```
src/main/java/tn/cyberious/compta/
├── AuthServiceApplication.java         # Main entry point
├── config/
│   ├── SecurityConfig.java            # Spring Security configuration
│   └── JwtProperties.java             # JWT settings (@ConfigurationProperties)
├── controller/
│   ├── AuthController.java            # Login/refresh endpoints
│   └── UserManagementController.java  # User/company creation
├── service/
│   ├── AuthService.java               # Login, token refresh, audit logging
│   └── UserManagementService.java     # Create users, assign roles
├── repository/                         # jOOQ-based data access
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── UserRoleRepository.java
│   ├── SocieteRepository.java
│   ├── EmployeeRepository.java
│   ├── RefreshTokenRepository.java
│   └── AuthLogRepository.java
├── security/
│   ├── CustomUserDetails.java         # UserDetails implementation
│   ├── CustomUserDetailsService.java  # Loads users from database
│   ├── JwtAuthenticationFilter.java   # JWT filter for requests
│   └── JwtAuthenticationEntryPoint.java
├── util/
│   └── JwtTokenUtil.java              # JWT generation/validation
├── dto/                               # Request/response objects
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── CreateUserRequest.java
│   ├── CreateSocieteRequest.java
│   └── CreateEmployeeRequest.java
└── enums/
    └── Role.java                      # Role enum with string mapping
```

### Repository Pattern

Repositories use jOOQ's DSLContext for type-safe SQL queries. Example pattern from UserRepository.java:

```java
@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final DSLContext dsl;

    public Optional<Users> findByUsername(String username) {
        return dsl.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOptional()
                .map(record -> record.into(Users.class));
    }
}
```

- Use static imports from `tn.cyberious.compta.auth.generated.Tables.*`
- Repositories return POJOs (from `tables.pojos` package), not Records
- Follow existing patterns for consistency

### Security Configuration

**Public endpoints (no auth required):**
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `/actuator/**`
- `/v3/api-docs/**`, `/swagger-ui/**`

**Protected endpoints (role-based):**
- `POST /api/users/comptable` - ADMIN only
- `POST /api/users/societe` - ADMIN or COMPTABLE
- `POST /api/users/employee` - ADMIN, COMPTABLE, or SOCIETE
- `POST /api/societes` - ADMIN or COMPTABLE

**JWT Configuration:**
- Access token expiry: 24 hours (configurable via `JWT_EXPIRATION`)
- Refresh token expiry: 7 days (configurable via `JWT_REFRESH_EXPIRATION`)
- Secret key: Set via `JWT_SECRET` environment variable (default provided for dev)
- Header format: `Authorization: Bearer <token>`

### Authentication Flow

1. **Login** (`POST /api/auth/login`):
   - Validates username/password via Spring Security AuthenticationManager
   - Generates access token + refresh token
   - Saves refresh token to database
   - Logs auth event to `auth_logs`
   - Resets failed login attempts on success
   - Increments failed attempts and locks account after 5 failures

2. **Token Refresh** (`POST /api/auth/refresh`):
   - Validates refresh token exists in database and not expired
   - Generates new access token (refresh token remains same)
   - Returns updated AuthResponse

3. **Protected Requests**:
   - JwtAuthenticationFilter extracts JWT from Authorization header
   - Validates token signature and expiration
   - Loads user details and sets SecurityContext
   - Spring Security enforces role-based access per SecurityConfig

### Parent POM Configuration

The parent `pom.xml` (at `../pom.xml`) defines:
- **Java 21** requirement
- Centralized dependency versions (MapStruct, Lombok, jOOQ, etc.)
- **jOOQ code generation plugin** configuration - each service overrides:
  - `jooq.generator.db.schema` - schema name (e.g., "auth")
  - `jooq.generator.target.package` - generated code package
- **JaCoCo** for test coverage
- **OpenRewrite** for Spring Boot upgrades

### Database Migrations with Flyway

**Migration files location:** `src/main/resources/db/migration/`

**Naming convention:** `V{version}__{description}.sql`
- Example: `V1__init_auth_schema.sql` (initial schema)
- Example: `V2__add_password_expiry.sql` (new feature)

**Key principles:**
- Each migration must start with `CREATE SCHEMA IF NOT EXISTS auth;`
- Always prefix table names with schema: `auth.users`, `auth.roles`, etc.
- Use idempotent statements: `IF NOT EXISTS`, `IF EXISTS`
- Migrations run automatically on service startup
- **Never modify an applied migration** - create a new one to fix issues
- Avoid cross-schema foreign keys for service independence

**Migration workflow:**
1. Create `VX__description.sql` in `src/main/resources/db/migration/`
2. Restart service (migration applies automatically)
3. Run `mvn clean generate-sources` to regenerate jOOQ classes
4. Use new schema elements in code

## Configuration

### Environment Variables

```bash
# Database (defaults for local development)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/compta
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# JWT Configuration
JWT_SECRET=your-secret-key-here  # Use strong key in production
JWT_EXPIRATION=86400000          # 24 hours in milliseconds
JWT_REFRESH_EXPIRATION=604800000 # 7 days in milliseconds

# Server
SERVER_PORT=8083
```

### Application Properties

Primary configuration: `src/main/resources/application.yml`

Inherits base configuration from `compta-commons/src/main/resources/application.yml` and overrides:
- `spring.flyway.enabled=true`
- `spring.flyway.schemas=auth`
- `spring.flyway.default-schema=auth`
- `server.port=8083`

## Development Guidelines

### Adding a New Feature

1. **Database changes**: Create new Flyway migration
2. **Regenerate jOOQ**: `mvn clean generate-sources`
3. **Add/modify repositories**: Use jOOQ DSLContext for queries
4. **Add/modify services**: Business logic layer
5. **Add/modify controllers**: REST endpoints
6. **Update SecurityConfig**: If new endpoints need authorization rules
7. **Test**: Write integration tests extending `AbstractIntegrationTest` from compta-commons

### Working with jOOQ

```java
// Import generated table constants
import static tn.cyberious.compta.auth.generated.Tables.*;

// Use DSLContext for queries
@RequiredArgsConstructor
public class ExampleRepository {
    private final DSLContext dsl;

    public List<Users> findActiveUsers() {
        return dsl.selectFrom(USERS)
                .where(USERS.IS_ACTIVE.eq(true))
                .fetch()
                .into(Users.class);  // Convert to POJO
    }
}
```

### Testing

Tests use **Testcontainers** to spin up a real PostgreSQL database. Base test class provided by `compta-commons`:

```java
@SpringBootTest
@Testcontainers
class MyIntegrationTest extends AbstractIntegrationTest {
    // Test with real database
}
```

## Common Issues

### jOOQ classes not found
**Solution:** Run `mvn clean generate-sources`

### Migration checksum mismatch
**Cause:** Modified an already-applied migration
**Solution (dev):** Delete row from `auth.flyway_schema_history` and restart
**Solution (prod):** Create new corrective migration (V3__fix_issue.sql)

### "Table does not exist" error
**Cause:** Schema not prefixed or Flyway not enabled
**Solution:** Always use `auth.table_name` in migrations, verify `spring.flyway.enabled=true`

### Account locked during testing
**Cause:** 5+ failed login attempts
**Solution:** Reset via SQL:
```sql
UPDATE auth.users SET is_locked = false, failed_login_attempts = 0 WHERE username = 'testuser';
```
