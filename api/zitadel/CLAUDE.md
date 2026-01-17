# CLAUDE.md - Zitadel Identity Provider

## Overview

Zitadel is the identity provider (IdP) for COMPTA, handling authentication (OIDC/OAuth2) and user management. It replaces the previous internal oauth2-server.

## Quick Start (Development)

```bash
# 1. Ensure PostgreSQL is running (from project root)
docker-compose up -d postgres

# 2. Create the zitadel database and user
psql -h localhost -U postgres -f init-db.sql
# OR with Docker:
docker exec -i compta-postgres psql -U postgres < init-db.sql

# 3. Create data directory for PAT files
mkdir -p data

# 4. Start Zitadel
docker-compose -f docker-compose.dev.yml up -d

# 5. Access Zitadel Console
# URL: http://localhost:8085/ui/console
# Admin: admin@compta.localhost / Admin123!
```

## Configuration

### Database Connection

Zitadel uses a dedicated PostgreSQL database:
- Host: `host.docker.internal` (dev) or your PostgreSQL host (prod)
- Port: `5432`
- Database: `zitadel` (dedicated database)
- User: `postgres`
- Password: `password` (dev only)
- Schemas: Managed internally by Zitadel

### Ports

| Environment | Port | Purpose |
|-------------|------|---------|
| Development | 8085 | Zitadel API & Console |
| Development | 3001 | Login V2 UI |
| Production | 8443 | Zitadel API & Console (HTTPS) |

## Files

| File | Purpose |
|------|---------|
| `docker-compose.dev.yml` | Development configuration (no TLS) |
| `docker-compose.prod.yml` | Production configuration (TLS, secure defaults) |
| `init-db.sql` | Database creation script |
| `.env.example` | Environment variables template |

## Production Deployment

1. **Generate a secure master key:**
   ```bash
   openssl rand -base64 32 | head -c 32
   ```

2. **Create production environment file:**
   ```bash
   cp .env.example .env.prod
   ```

3. **Configure required variables in `.env.prod`:**
   - `ZITADEL_MASTERKEY` - 32-character encryption key
   - `ZITADEL_DOMAIN` - Your public domain (e.g., `auth.compta.tn`)
   - `ZITADEL_ADMIN_USERNAME` - Initial admin email
   - `ZITADEL_ADMIN_PASSWORD` - Secure admin password
   - `POSTGRES_HOST` - PostgreSQL host
   - `POSTGRES_PASSWORD` - PostgreSQL password
   - `POSTGRES_SSL_MODE` - Set to `require` for production

4. **Prepare TLS certificates:**
   ```bash
   mkdir certs
   # Place cert.pem and key.pem in certs/
   ```

5. **Start Zitadel:**
   ```bash
   docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
   ```

## Integration with COMPTA Services

### Gateway Configuration

Update the gateway to validate JWTs from Zitadel:

```yaml
# application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8085  # dev
          # issuer-uri: https://auth.compta.tn  # prod
```

### Frontend Integration

Configure the frontend to use Zitadel for authentication:

```typescript
// auth.config.ts
export const authConfig = {
  authority: 'http://localhost:8085',  // dev
  // authority: 'https://auth.compta.tn',  // prod
  client_id: 'your-client-id',
  redirect_uri: 'http://localhost:5173/callback',  // Vite dev server
  scope: 'openid profile email',
};
```

## Useful Commands

```bash
# View logs
docker-compose -f docker-compose.dev.yml logs -f zitadel
docker-compose -f docker-compose.dev.yml logs -f login

# Restart Zitadel
docker-compose -f docker-compose.dev.yml restart

# Stop and remove
docker-compose -f docker-compose.dev.yml down

# Check health
curl http://localhost:8085/debug/healthz
```

## Troubleshooting

### Zitadel won't start

1. Check if PostgreSQL is running and accessible
2. Verify the `zitadel` database exists
3. Check logs: `docker-compose -f docker-compose.dev.yml logs zitadel`

### Database connection issues

1. Ensure `host.docker.internal` resolves correctly (Windows/Mac)
2. For Linux, use the actual PostgreSQL IP or add `extra_hosts` mapping

### Reset Zitadel (Development only)

```bash
# Stop Zitadel
docker-compose -f docker-compose.dev.yml down

# Drop and recreate database
psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS zitadel;"
psql -h localhost -U postgres -c "DROP ROLE IF EXISTS zitadel;"
psql -h localhost -U postgres -f init-db.sql

# Clean PAT files
rm -rf data/*

# Restart
docker-compose -f docker-compose.dev.yml up -d
```
