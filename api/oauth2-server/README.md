# OAuth2 Server Module

> **Code Review**: 2026-01-17 | **Health Score**: 8.2/10 | **Status**: Production Ready

## Overview

This module provides a complete OAuth2 Authorization Server implementation using Spring Authorization Server 1.3+ with Spring Security 6. It supports both public clients (with PKCE) and confidential clients, and uses jOOQ for database access.

## Quick Start

```bash
# Build
mvn clean install -pl oauth2-server -am

# Run (dev profile)
cd oauth2-server && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Access points
# - OAuth2 Server: http://localhost:9000
# - Swagger UI: http://localhost:9000/swagger-ui.html
# - JWKS: http://localhost:9000/.well-known/jwks.json
```

## Architecture

### Technology Stack
- **Spring Boot 3.5.9** with Spring Security 6
- **Spring Authorization Server 1.3+**
- **jOOQ** for type-safe database access
- **Flyway** for database migrations
- **PostgreSQL 16** with `oauth2` schema
- **Redis** for rate limiting cache
- **RSA 2048-bit** keys for JWT signing (with rotation)
- **Micrometer** for metrics

### Features Implemented

| Feature | Status | RFC |
|---------|--------|-----|
| Authorization Code Flow | Implemented | RFC 6749 |
| PKCE | Implemented | RFC 7636 |
| Client Credentials | Implemented | RFC 6749 |
| Refresh Token | Implemented | RFC 6749 |
| Token Introspection | Implemented | RFC 7662 |
| Token Revocation | Implemented | RFC 7009 |
| OIDC UserInfo | Implemented | OpenID Connect |
| JWKS | Implemented | RFC 7517 |
| Rate Limiting | Implemented | - |
| Audit Logging | Implemented | - |
| Key Rotation | Implemented | - |

### Registered Clients

| Client ID | Type | Authentication | Grant Types | PKCE | Description |
|-----------|------|----------------|--------------|------|-------------|
| `public-client` | Public | None | Authorization Code, Refresh Token | Yes | SPAs, mobile apps |
| `gateway` | Confidential | Client Secret Basic | Client Credentials | No | Gateway service (service-to-service) |

### Client Secrets
- `public-client`: No secret (public client)
- `gateway`: `${GATEWAY_SECRET}` (configure via environment)

### Architecture Note

Services behind the gateway (accounting-service, authz-service, hr-service, etc.) do NOT need to be registered as OAuth2 clients. They receive user information via HTTP headers added by the gateway after token validation:

- `X-User-Id` - User ID
- `X-User-Username` - Username
- `X-User-Email` - User email
- `X-User-Roles` - Comma-separated roles
- `X-Tenant-Id` - Tenant/Company ID

### Default Users
| Username | Password | Roles |
|----------|----------|-------|
| `admin` | `admin123` | ROLE_ADMIN |
| `user` | `user123` | ROLE_USER |

## PKCE (Proof Key for Code Exchange)

PKCE is enabled for all clients except the gateway (which uses Client Credentials flow). PKCE provides additional security for public clients and is recommended for confidential clients as well.

### PKCE Flow
1. Client generates a code verifier (random string)
2. Client creates a code challenge (SHA256 hash of verifier)
3. Client includes code challenge in authorization request
4. Authorization server stores the challenge
5. Client sends code verifier in token request
6. Authorization server verifies the challenge

## Database Schema

All tables are in the `oauth2` schema:

### OAuth2 Tables
- `oauth2.oauth2_registered_client` - Registered OAuth2 clients
- `oauth2.oauth2_authorization` - OAuth2 authorizations
- `oauth2.oauth2_authorization_consent` - User consent records

### User Authentication Tables
- `oauth2.users` - User accounts
- `oauth2.roles` - User roles
- `oauth2.user_roles` - User-role associations

## Endpoints

### OAuth2 Endpoints
- **Authorization Endpoint**: `GET /oauth2/authorize`
- **Token Endpoint**: `POST /oauth2/token`
- **JWKS Endpoint**: `GET /.well-known/jwks.json`
- **Token Revocation**: `POST /oauth2/revoke`
- **Token Introspection**: `POST /oauth2/introspect`

### User Authentication
- **Login**: `POST /login` (form-based)
- **Logout**: `POST /logout`

## Configuration

### Application Properties

```yaml
server:
  port: 9000

spring:
  application:
    name: oauth2-server
  datasource:
    url: jdbc:postgresql://localhost:5432/compta
    username: postgres
    password: postgres
  flyway:
    enabled: true
    schemas: oauth2
    locations: classpath:db/migration
```

### JOOQ Configuration

```yaml
jooq:
  generator:
    db:
      schema: oauth2
    target:
      package: tn.cyberious.compta.oauth2.generated
```

## Usage Examples

### Authorization Code Flow with PKCE (Public Client)

```bash
# 1. Generate code verifier and challenge
CODE_VERIFIER=$(openssl rand -base64 64 | tr -d '/+=' | cut -c1-128)
CODE_CHALLENGE=$(echo -n $CODE_VERIFIER | openssl dgst -sha256 -binary | openssl base64 -urlsafe | tr -d '=')

# 2. Request authorization
curl -X GET "http://localhost:9000/oauth2/authorize?client_id=public-client&response_type=code&scope=openid%20read%20write&redirect_uri=http://localhost:3000/authorized&code_challenge=$CODE_CHALLENGE&code_challenge_method=S256"

# 3. Exchange code for token
curl -X POST "http://localhost:9000/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=AUTHORIZATION_CODE&redirect_uri=http://localhost:3000/authorized&client_id=public-client&code_verifier=$CODE_VERIFIER"
```

### Client Credentials Flow (Gateway)

```bash
curl -X POST "http://localhost:9000/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic $(echo -n 'gateway:gateway-secret' | base64)" \
  -d "grant_type=client_credentials&scope=read%20write"
```

### Authorization Code Flow with PKCE (Confidential Client)

```bash
# 1. Generate code verifier and challenge
CODE_VERIFIER=$(openssl rand -base64 64 | tr -d '/+=' | cut -c1-128)
CODE_CHALLENGE=$(echo -n $CODE_VERIFIER | openssl dgst -sha256 -binary | openssl base64 -urlsafe | tr -d '=')

# 2. Request authorization
curl -X GET "http://localhost:9000/oauth2/authorize?client_id=accounting-service&response_type=code&scope=openid%20read%20write&redirect_uri=http://127.0.0.1:8080/authorized&code_challenge=$CODE_CHALLENGE&code_challenge_method=S256"

# 3. Exchange code for token
curl -X POST "http://localhost:9000/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic $(echo -n 'accounting-service:accounting-secret' | base64)" \
  -d "grant_type=authorization_code&code=AUTHORIZATION_CODE&redirect_uri=http://127.0.0.1:8080/authorized&code_verifier=$CODE_VERIFIER"
```

### Refresh Token Flow

```bash
curl -X POST "http://localhost:9000/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic $(echo -n 'public-client: ' | base64)" \
  -d "grant_type=refresh_token&refresh_token=REFRESH_TOKEN"
```

## Token Validation

The gateway validates JWT tokens using the JWKS endpoint:

```bash
# Get JWKS (public keys)
curl http://localhost:9000/.well-known/jwks.json

# Decode JWT (for inspection)
echo "JWT_TOKEN" | jq -R 'split(".") | .[1] | @base64d | fromjson'
```

## Security Features

1. **PKCE Support**: Enabled for all clients except gateway
2. **RSA 2048-bit Keys**: Strong cryptographic signing
3. **Authorization Code Expiry**: 5 minutes
4. **Access Token Expiry**: 30 minutes (60 minutes for gateway)
5. **Refresh Tokens**: Non-reusable for public clients
6. **Consent Management**: Required for all authorization requests
7. **JWKS Endpoint**: Automatic public key distribution

## Development

### Running the Server

```bash
# Build the project
mvn clean install

# Run the OAuth2 server
cd oauth2-server
mvn spring-boot:run
```

### Database Migrations

```bash
# Flyway migrations are automatically applied on startup
# To manually run migrations:
mvn flyway:migrate -pl oauth2-server
```

### JOOQ Code Generation

```bash
# Generate JOOQ classes from database schema
mvn jooq-codegen:generate -pl oauth2-server
```

## Testing

### Test with curl

```bash
# Test JWKS endpoint
curl http://localhost:9000/.well-known/jwks.json

# Test authorization endpoint (will redirect to login)
curl -L http://localhost:9000/oauth2/authorize?client_id=public-client&response_type=code&scope=openid&redirect_uri=http://localhost:3000/authorized
```

### Test with Postman

1. Create a new OAuth 2.0 request
2. Configure client credentials
3. Generate PKCE code verifier and challenge
4. Request authorization code
5. Exchange for access token

## Troubleshooting

### Common Issues

1. **Invalid Code Verifier**: Ensure code verifier matches the challenge used in authorization request
2. **Expired Authorization Code**: Authorization codes expire in 5 minutes
3. **Invalid Client Secret**: Verify client credentials match registered client configuration
4. **Missing PKCE Parameters**: Public clients must include `code_challenge` and `code_challenge_method`

### Logs

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
    tn.cyberious.compta.oauth2: DEBUG
```

## API Endpoints

### OAuth2 Standard
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/oauth2/authorize` | GET | Authorization endpoint |
| `/oauth2/token` | POST | Token endpoint |
| `/oauth2/revoke` | POST | Token revocation |
| `/oauth2/introspect` | POST | Token introspection |
| `/.well-known/jwks.json` | GET | JWKS public keys |
| `/userinfo` | GET | OIDC UserInfo |

### Management APIs
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/users` | CRUD | ADMIN | User management |
| `/api/clients` | CRUD | ADMIN | Client management |
| `/api/users/password/reset` | POST | Public | Password reset |
| `/api/users/email/verify` | POST | Public | Email verification |

## Configuration

### Environment Variables

```bash
# Required for production
OAUTH2_ISSUER=https://auth.example.com
GATEWAY_SECRET=your-secure-secret
CORS_ALLOWED_ORIGINS=https://app.example.com
FRONTEND_URL=https://app.example.com

# Database
DATABASE_URL=jdbc:postgresql://host:5432/compta
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=secret

# Redis
REDIS_HOST=redis-host
REDIS_PORT=6379
```

## Documentation

- **Detailed Architecture**: See [`AGENTS.md`](AGENTS.md)
- **Completed Tasks**: See [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md)
- **Remaining Tasks**: See [`TASKS.md`](TASKS.md)

## References

- [Spring Authorization Server Documentation](https://docs.spring.io/spring-authorization-server/reference/)
- [OAuth 2.1 RFC](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-09)
- [PKCE RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636)
- [Token Introspection RFC 7662](https://datatracker.ietf.org/doc/html/rfc7662)
- [Token Revocation RFC 7009](https://datatracker.ietf.org/doc/html/rfc7009)
- [jOOQ Documentation](https://www.jooq.org/)

---

## Code Review Summary (2026-01-17)

### Health Score: 8.2/10

| Category | Score |
|----------|-------|
| Architecture | 9/10 |
| Security | 8/10 |
| Code Quality | 8/10 |
| Tests | 5/10 |
| Documentation | 9/10 |

### What's Working Well
- Spring Authorization Server 1.3+ properly configured
- PKCE, RSA key rotation, token blacklisting
- Rate limiting and audit logging
- Full user and client management APIs

### Areas for Improvement
- Add integration tests
- Implement account lockout
- Consider adding 2FA support
- See [`TASKS.md`](TASKS.md) for full list
