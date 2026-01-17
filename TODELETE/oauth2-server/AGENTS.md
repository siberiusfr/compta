# OAuth2 Server - Agents

> **Code Review Date**: 2026-01-17
> **Reviewed By**: Claude Code (Opus 4.5)
> **Status**: Production Ready with Recommendations

---

## Executive Summary

Le serveur OAuth2 est une implémentation complète et bien architecturée utilisant Spring Authorization Server 1.3+. Il fournit des tokens JWT et gère l'authentification centralisée pour tous les microservices COMPTA.

### Health Score: 8.2/10

| Catégorie | Score | Status |
|-----------|-------|--------|
| Architecture | 9/10 | Excellent |
| Sécurité | 8/10 | Bon |
| Code Quality | 8/10 | Bon |
| Tests | 5/10 | Insuffisant |
| Documentation | 9/10 | Excellent |

---

## Architecture Overview

### Technology Stack
- **Spring Boot 3.5.9** avec Spring Security 6
- **Spring Authorization Server 1.3+** (production-grade)
- **jOOQ** pour l'accès type-safe à la base de données
- **PostgreSQL 16** avec schéma `oauth2`
- **Redis** pour rate limiting et cache
- **Micrometer** pour les métriques

### Service Layout

```
oauth2-server/
├── config/                 # Configuration Spring Security & OAuth2
│   ├── AuthorizationServerConfig.java    # Core OAuth2 config
│   ├── JWKSourceConfig.java              # Key management
│   ├── JwtTokenCustomizer.java           # Custom JWT claims
│   ├── RateLimitConfig.java              # Rate limiting rules
│   └── CorsProperties.java               # CORS configuration
├── controller/             # REST API endpoints
│   ├── UserManagementController.java     # User CRUD
│   ├── ClientManagementController.java   # OAuth2 client CRUD
│   ├── PasswordResetController.java      # Password reset flow
│   ├── EmailVerificationController.java  # Email verification
│   ├── IntrospectionController.java      # Token introspection
│   └── RevocationController.java         # Token revocation
├── service/                # Business logic
│   ├── UserManagementService.java
│   ├── ClientManagementService.java
│   ├── KeyManagementService.java
│   ├── TokenRevocationService.java
│   └── AuditLogService.java
├── repository/             # jOOQ data access
├── jti/                    # Token blacklisting
├── filter/                 # Security filters
├── metrics/                # Micrometer metrics
└── aspect/                 # AOP audit logging
```

---

## Key Components

### 1. AuthorizationServerConfig (Core)
**Location**: `config/AuthorizationServerConfig.java`

Configuration centrale avec deux chaînes de filtres:
1. **OAuth2 Filter Chain** (Order 1): Endpoints OAuth2 avec OIDC
2. **Default Filter Chain** (Order 2): Form login, API authentication

**Points forts**:
- CORS configurable via properties
- CSRF protection appropriée
- JWT authentication converter pour roles/scopes
- Initialisation automatique des clients par défaut

### 2. Token Blacklist Service
**Location**: `jti/TokenBlacklistService.java`

Implémentation deux-tiers pour la révocation de tokens:
- **Cache in-memory** (ConcurrentHashMap) pour performances
- **Persistance PostgreSQL** pour durabilité
- Nettoyage automatique des entrées expirées

### 3. Rate Limiting
**Location**: `filter/RateLimitFilter.java`

| Endpoint | Limite | Fenêtre |
|----------|--------|---------|
| `/oauth2/token` | 10 | par minute |
| `/oauth2/revoke` | 20 | par minute |
| `/oauth2/introspect` | 100 | par minute |
| `/login` | 5 | par minute |
| `/api/users/password/reset` | 3 | par heure |

### 4. Audit Logging
**Location**: `aspect/AuditLogAspect.java`

Logging complet via AOP:
- Toutes les opérations sensibles tracées
- IP address et User-Agent capturés
- Stockage JSON extensible
- Nettoyage automatique après 90 jours

---

## OAuth2 Flows Implemented

### 1. Authorization Code Flow with PKCE
```
Client → /oauth2/authorize (with code_challenge)
User → Login form
User → Consent (optional)
Server → Redirect with code
Client → /oauth2/token (with code_verifier)
Server → Access token + Refresh token
```

### 2. Client Credentials Flow
```
Gateway → /oauth2/token (Basic auth)
Server → Access token (no refresh)
```

### 3. Refresh Token Flow
```
Client → /oauth2/token (refresh_token grant)
Server → New access token + New refresh token
```

---

## Database Schema

### Core Tables (oauth2 schema)

| Table | Purpose |
|-------|---------|
| `oauth2_registered_client` | OAuth2 clients |
| `oauth2_authorization` | Active authorizations |
| `oauth2_authorization_consent` | User consents |
| `users` | User accounts |
| `roles` | Role definitions |
| `user_roles` | User-role mapping |
| `oauth2_keys` | RSA key pairs |
| `password_reset_tokens` | Password reset |
| `email_verification_tokens` | Email verification |
| `audit_logs` | Security audit trail |
| `token_blacklist` | Revoked token JTIs |

---

## Registered Clients

| Client ID | Type | Auth Method | Grants | PKCE |
|-----------|------|-------------|--------|------|
| `public-client` | Public | None | auth_code, refresh | Required |
| `gateway` | Confidential | client_secret_basic | client_credentials | No |

---

## API Endpoints

### OAuth2 Standard
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/oauth2/authorize` | GET | Authorization endpoint |
| `/oauth2/token` | POST | Token endpoint |
| `/oauth2/revoke` | POST | Token revocation (RFC 7009) |
| `/oauth2/introspect` | POST | Token introspection (RFC 7662) |
| `/.well-known/jwks.json` | GET | JWKS endpoint |
| `/userinfo` | GET | OIDC UserInfo |

### User Management
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/users` | POST | ADMIN | Create user |
| `/api/users` | GET | ADMIN | List users |
| `/api/users/{id}` | GET/PUT/DELETE | ADMIN | User CRUD |
| `/api/users/{id}/disable` | PATCH | ADMIN | Disable account |
| `/api/users/{id}/enable` | PATCH | ADMIN | Enable account |
| `/api/users/{id}/roles` | GET/POST | ADMIN | Role management |
| `/api/users/{id}/password` | POST | AUTH | Change password |

### Client Management
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/clients` | POST | ADMIN | Create client |
| `/api/clients` | GET | ADMIN | List clients |
| `/api/clients/{id}` | GET/PUT/DELETE | ADMIN | Client CRUD |
| `/api/clients/{id}/secret` | POST | ADMIN | Rotate secret |

### Public Endpoints
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users/password/reset` | POST | Request reset |
| `/api/users/password/reset/confirm` | POST | Confirm reset |
| `/api/users/email/verify` | POST | Request verification |
| `/api/users/email/verify/confirm` | POST | Confirm verification |

---

## Security Features Implemented

| Feature | Status | Notes |
|---------|--------|-------|
| PKCE | Implemented | Required for public clients |
| RSA 2048-bit signing | Implemented | With key rotation |
| Token blacklisting | Implemented | JTI-based |
| Rate limiting | Implemented | Per endpoint |
| CSRF protection | Implemented | Cookie-based |
| Audit logging | Implemented | Full trail |
| Password hashing | Implemented | BCrypt |
| CORS | Implemented | Configurable |
| Key rotation | Implemented | Scheduled daily |

---

## Metrics Available

### Token Operations
- `oauth2.token.issued`
- `oauth2.token.refreshed`
- `oauth2.token.revoked`
- `oauth2.token.introspected`

### Authentication
- `oauth2.login{status=success/failure}`
- `oauth2.logout`
- `oauth2.authentication.duration`

### Security Events
- `oauth2.security.rate_limit_exceeded`
- `oauth2.security.csrf_validation_failed`

---

## Configuration

### Key Properties

```yaml
oauth2:
  issuer: ${OAUTH2_ISSUER:http://localhost:9000}
  gateway:
    secret: ${GATEWAY_SECRET:gateway-secret-change-in-production}
  authorization-code:
    access-token-validity: 1800   # 30 minutes
    refresh-token-validity: 86400 # 24 hours
  key:
    rotation:
      enabled: true
      schedule: "0 0 2 * * *"     # Daily at 2 AM
      key-lifetime-days: 90
      grace-period-days: 7

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
  allow-credentials: true

app:
  frontend:
    url: ${FRONTEND_URL:http://localhost:3000}
```

---

## Default Users

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ROLE_ADMIN |
| user | user123 | ROLE_USER |

---

## Integration with Gateway

Services behind the gateway receive user info via headers:
- `X-User-Id` - UUID
- `X-User-Username` - String
- `X-User-Email` - String
- `X-User-Roles` - Comma-separated
- `X-Tenant-Id` - Company ID

---

## Development Commands

```bash
# Build
mvn clean install -pl oauth2-server -am

# Run
cd oauth2-server && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generate jOOQ classes
mvn clean generate-sources -pl oauth2-server

# Run tests
mvn test -pl oauth2-server
```

---

## Documentation Links

- **Swagger UI**: http://localhost:9000/swagger-ui.html
- **OpenAPI Spec**: http://localhost:9000/v3/api-docs
- [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/)
- [RFC 6749 - OAuth 2.0](https://datatracker.ietf.org/doc/html/rfc6749)
- [RFC 7636 - PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
- [RFC 7662 - Token Introspection](https://datatracker.ietf.org/doc/html/rfc7662)
- [RFC 7009 - Token Revocation](https://datatracker.ietf.org/doc/html/rfc7009)
