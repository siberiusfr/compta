# OAuth2 Authorization Server

OAuth2 Authorization Server for Compta microservices architecture.

## Overview

This service provides OAuth2 authorization capabilities including:
- Token issuance (access tokens, refresh tokens)
- User authentication with database-backed user details
- Client credentials for service-to-service authentication
- Authorization code flow for user authentication
- OpenID Connect (OIDC) support

## Configuration

### Database

The service uses PostgreSQL with Flyway migrations. Ensure your database is running:

```bash
# Start PostgreSQL (if using Docker)
docker run --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=compta -p 5432:5432 -d postgres:16
```

### Application Properties

The service is configured in `src/main/resources/application.yml`:

- Server port: 9000
- Database: PostgreSQL on localhost:5432
- Flyway migrations: Enabled

## Registered Clients

The following OAuth2 clients are pre-configured:

| Client ID | Client Secret | Grant Types | Scopes |
|-----------|---------------|-------------|--------|
| accounting-service | accounting-secret | authorization_code, refresh_token, client_credentials | openid, read, write |
| authz-service | authz-secret | authorization_code, refresh_token, client_credentials | openid, read, write |
| hr-service | hr-secret | authorization_code, refresh_token, client_credentials | openid, read, write |

## Default Users

The following users are created by Flyway migrations:

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ROLE_ADMIN |
| user | user123 | ROLE_USER |

## API Endpoints

### OAuth2 Endpoints

- **Authorization Endpoint**: `GET /oauth2/authorize`
- **Token Endpoint**: `POST /oauth2/token`
- **JWKS Endpoint**: `GET /.well-known/jwks.json`
- **Token Revocation**: `POST /oauth2/revoke`
- **Token Introspection**: `POST /oauth2/introspect`

### Documentation

- **Swagger UI**: http://localhost:9000/swagger-ui.html
- **OpenAPI JSON**: http://localhost:9000/v3/api-docs

## Running the Service

```bash
# Build the project
mvn clean install

# Run the service
cd oauth2-server
mvn spring-boot:run
```

## Database Schema

All tables are created in the `oauth2` schema by Flyway migrations.

### OAuth2 Tables

- `oauth2.oauth2_registered_client` - Registered OAuth2 clients
- `oauth2.oauth2_authorization` - OAuth2 authorizations and tokens
- `oauth2.oauth2_authorization_consent` - User consent for authorizations

### User Authentication Tables

- `oauth2.users` - User accounts
- `oauth2.roles` - User roles
- `oauth2.user_roles` - User-role relationships

## Service-to-Service Authentication

Services can authenticate using the client credentials grant:

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "accounting-service:accounting-secret" \
  -d "grant_type=client_credentials" \
  -d "scope=read write"
```

## User Authentication (Authorization Code Flow)

1. Redirect user to authorization endpoint:
   ```
   http://localhost:9000/oauth2/authorize?client_id=accounting-service&response_type=code&scope=read write&redirect_uri=http://127.0.0.1:8080/authorized
   ```

2. User logs in with username/password

3. User is redirected with authorization code

4. Exchange code for access token:
   ```bash
   curl -X POST http://localhost:9000/oauth2/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -u "accounting-service:accounting-secret" \
     -d "grant_type=authorization_code" \
     -d "code=YOUR_CODE" \
     -d "redirect_uri=http://127.0.0.1:8080/authorized"
   ```

## Security Considerations

- All client secrets should be stored securely in production
- Use HTTPS in production environments
- Implement proper token expiration policies
- Regularly rotate RSA keys
- Monitor and audit authorization activities
