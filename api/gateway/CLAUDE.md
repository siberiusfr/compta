# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **COMPTA Gateway Service** - a Spring Cloud Gateway that handles authentication, routing, resilience, and observability for the COMPTA ERP microservices architecture.

**Tech Stack:** Java 21, Spring Boot 3.2.1, Spring Cloud 2023.0.0, Reactive (WebFlux)

## Build Commands

```bash
# Build with tests
mvn clean install

# Build without tests
mvn clean package -DskipTests

# Run tests
mvn test

# Run integration tests
mvn verify

# Run application (dev profile)
./start.sh
# OR
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run production
java -jar target/gateway-service-1.0.0.jar --spring.profiles.active=prod
```

## Architecture

### Reactive Non-Blocking Design
Built on Project Reactor with Spring Cloud Gateway. All I/O operations (Redis, downstream calls, health checks) use reactive clients.

### JWT Authentication
- OAuth2 Resource Server with HMAC-SHA256 (HS256)
- Issuer: `compta-auth`
- Public endpoints: `/auth/**`, `/actuator/health`, `/swagger-ui/**`, `/fallback/**`
- JWT claims propagated to downstream services via `JwtToHeadersGatewayFilter`

### Resilience Patterns
- **Circuit Breakers:** Resilience4j with per-service thresholds (Auth: 30%, Invoice: 60%, default: 50%)
- **Rate Limiting:** Redis-backed token bucket per authenticated user
- **Timeouts:** Per-service configurable (Auth: 10s, Invoice: 60s, default: 30s)
- **Fallback Controller:** Graceful degradation at `/fallback/**`

### Service Routing
| Path | Service | Port |
|------|---------|------|
| `/auth/**` | Auth Service | 8081 |
| `/api/permissions/**` | Authorization Service | 8084 |
| `/api/invoices/**` | Invoice Service | 8082 |
| `/api/employees/**` | Employee Service | 8083 |

### Key Source Locations
- `config/SecurityConfig.java` - JWT OAuth2 security configuration
- `config/CircuitBreakerConfiguration.java` - Resilience4j per-service settings
- `config/RateLimitConfig.java` - Redis rate limiting with user key resolver
- `filter/JwtToHeadersGatewayFilter.java` - JWT propagation to downstream
- `filter/SecureLoggingGlobalFilter.java` - Request logging with sensitive data masking
- `exception/GlobalErrorWebExceptionHandler.java` - Global error handling
- `health/DownstreamServicesHealthIndicator.java` - Aggregated service health

## Configuration Profiles

- **dev:** Permissive CORS (localhost ports), DEBUG logging, all actuator endpoints, Swagger enabled
- **prod:** Strict CORS (compta.tn domains only), INFO logging, limited actuator, Swagger disabled

## Required Environment Variables (Production)

```
JWT_SECRET          # Minimum 64 characters
AUTH_SERVICE_URL    # HTTPS URL
AUTHZ_SERVICE_URL   # HTTPS URL
INVOICE_SERVICE_URL # HTTPS URL
EMPLOYEE_SERVICE_URL# HTTPS URL
REDIS_HOST          # Redis instance
```

## Local Development

Requires Redis running. The `docker-compose.yml` sets up both gateway and Redis:
```bash
docker-compose up -d
```

## Monitoring

```bash
# Health check
curl http://localhost:8080/actuator/health

# Circuit breaker status
curl http://localhost:8080/actuator/circuitbreakers

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Swagger UI (dev only)
http://localhost:8080/swagger-ui.html
```

## Security Notes

See `SECURITY_CHECKLIST.md` for production deployment validation. Key points:
- Never use the default JWT secret in production
- Ensure all service URLs use HTTPS
- Disable Swagger UI in production
- Restrict actuator endpoints
