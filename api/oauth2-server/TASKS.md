# OAuth2 Server - Tasks Roadmap

> **Last Code Review**: 2026-01-17
> **Reviewed By**: Claude Code (Opus 4.5)

This document outlines all the missing components and features needed to make the OAuth2 server complete and production-ready.

> **Note:** The High Priority Tasks (tasks 1-6) and most Medium Priority Tasks have been completed and moved to [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).

---

## Table of Contents

1. [Code Review Findings](#code-review-findings---new) - **FROM 2026-01-17 REVIEW**
2. [Critical Issues to Fix](#critical-issues-to-fix) - **URGENT**
3. [Medium Priority Tasks](#medium-priority-tasks) - Partially Completed
4. [Low Priority Tasks](#low-priority-tasks)
5. [Testing Tasks](#testing-tasks)
6. [DevOps & Operations Tasks](#devops--operations-tasks)

---

## Code Review Findings - NEW

### Summary

| Category | Score | Status |
|----------|-------|--------|
| Architecture | 9/10 | Excellent |
| Security | 8/10 | Good |
| Code Quality | 8/10 | Good |
| Tests | 5/10 | Needs Work |
| Documentation | 9/10 | Excellent |

### What's Working Well

1. **Architecture solide**
   - Spring Authorization Server 1.3+ bien configuré
   - jOOQ avec type-safety
   - Séparation claire des responsabilités (Controller → Service → Repository)

2. **Sécurité**
   - PKCE obligatoire pour clients publics
   - RSA 2048-bit avec rotation automatique
   - Token blacklisting avec double-couche (cache + DB)
   - Rate limiting sur tous les endpoints sensibles
   - Audit logging complet

3. **Features complètes**
   - Token introspection (RFC 7662)
   - Token revocation (RFC 7009)
   - OIDC UserInfo endpoint
   - User/Client management APIs
   - Password reset flow
   - Email verification flow

4. **Configuration**
   - Secrets externalisés via variables d'environnement
   - CORS configurable
   - Profiles dev/prod

### Issues Identified

#### CRITICAL - Security
| # | Issue | Impact | Fix Required |
|---|-------|--------|--------------|
| 1 | **Redirect URIs hardcodées** | Vulnérabilité en production | Externaliser via config |
| 2 | **Pas de account lockout** | Brute force possible | Implémenter lockout progressif |
| 3 | **Pas de 2FA** | Sécurité insuffisante pour comptes sensibles | Implémenter TOTP |

#### HIGH - Code Quality
| # | Issue | Impact | Fix Required |
|---|-------|--------|--------------|
| 4 | **Tests insuffisants** | Régressions possibles | Ajouter tests d'intégration |
| 5 | **RequestCounter bug potentiel** | Race condition | Refactorer la logique |
| 6 | **DPoP non implémenté** | Documenté mais absent | Implémenter ou supprimer doc |

#### MEDIUM - Functionality
| # | Issue | Impact | Fix Required |
|---|-------|--------|--------------|
| 7 | **Pas de session management** | UX limitée | Ajouter gestion sessions |
| 8 | **Pas de social login** | Friction utilisateur | Implémenter OAuth2 federation |
| 9 | **Multi-tenancy limité** | Scalabilité | Améliorer isolation |

---

## Critical Issues to Fix

### Issue 1: Redirect URIs Hardcodées

**Problem**: Les redirect URIs sont hardcodées dans `AuthorizationServerConfig.java`:
```java
.redirectUri("http://localhost:3000/authorized")
.redirectUri("http://localhost:8080/authorized")
```

**Impact**: Ne fonctionne pas en production sans modification du code.

**Solution**:
```yaml
# application.yml
oauth2:
  clients:
    public-client:
      redirect-uris: ${PUBLIC_CLIENT_REDIRECT_URIS:http://localhost:3000/authorized}
    gateway:
      redirect-uris: ${GATEWAY_REDIRECT_URIS:http://localhost:8080/authorized}
```

**Files to Modify**:
- `src/main/java/tn/cyberious/compta/oauth2/config/AuthorizationServerConfig.java`
- `src/main/resources/application.yml`

---

### Issue 2: Account Lockout Missing

**Problem**: Pas de protection contre les attaques brute force sur `/login`.

**Current State**: Rate limiting existe mais n'est pas suffisant.

**Solution**: Implémenter lockout progressif:
- 5 échecs → 15 minutes de blocage
- 10 échecs → 1 heure
- 15 échecs → 24 heures

**Files to Create**:
```
src/main/java/tn/cyberious/compta/oauth2/service/AccountLockoutService.java
src/main/java/tn/cyberious/compta/oauth2/handler/AuthenticationFailureHandler.java
src/main/resources/db/migration/V11__failed_login_attempts.sql
```

**Database Schema**:
```sql
CREATE TABLE oauth2.failed_login_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    attempt_count INTEGER DEFAULT 1,
    first_attempt_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_attempt_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    locked_until TIMESTAMP
);
```

---

### Issue 3: 2FA Missing

**Problem**: Authentification mono-facteur uniquement.

**Solution**: Implémenter TOTP (Time-based One-Time Password)

**Files to Create**:
```
src/main/java/tn/cyberious/compta/oauth2/service/TwoFactorAuthService.java
src/main/java/tn/cyberious/compta/oauth2/controller/TwoFactorAuthController.java
src/main/java/tn/cyberious/compta/oauth2/dto/Enable2FARequest.java
src/main/java/tn/cyberious/compta/oauth2/dto/Verify2FARequest.java
src/main/resources/db/migration/V12__two_factor_auth.sql
```

**Dependencies**:
```xml
<dependency>
    <groupId>dev.samstevens.totp</groupId>
    <artifactId>totp</artifactId>
    <version>1.7.1</version>
</dependency>
```

---

### Issue 4: Tests Insuffisants

**Current State**: Seulement `OAuth2ServerApplicationTests.java` qui teste le chargement du contexte.

**Required Tests**:

| Category | Files to Create |
|----------|-----------------|
| Integration | `AuthorizationCodeFlowTest.java` |
| Integration | `ClientCredentialsFlowTest.java` |
| Integration | `RefreshTokenFlowTest.java` |
| Integration | `TokenRevocationTest.java` |
| Security | `AuthenticationSecurityTest.java` |
| Security | `RateLimitTest.java` |
| Unit | `UserManagementServiceTest.java` |
| Unit | `TokenBlacklistServiceTest.java` |

---

### Issue 5: RequestCounter Race Condition

**Location**: `filter/RateLimitFilter.java:130-153`

**Problem**: La méthode `increment()` fait cleanup après ajout, pouvant causer des incohérences.

**Current Code**:
```java
public int increment() {
    long now = System.currentTimeMillis();
    timestamps.compute(now, (k, v) -> v == null ? new AtomicInteger(1) : v);
    // Cleanup happens AFTER adding
    long cutoff = now - lastWindowSize;
    timestamps.entrySet().removeIf(entry -> entry.getKey() < cutoff);
    return timestamps.values().stream().mapToInt(AtomicInteger::get).sum();
}
```

**Fix**: Faire cleanup AVANT l'ajout ou utiliser une approche sliding window.

---

### Issue 6: DPoP Non Implémenté

**Current State**: Documenté dans COMPLETED_TASKS.md comme "NOT DONE" mais présent dans la documentation.

**Decision Required**:
- Option A: Implémenter DPoP (RFC 9449)
- Option B: Supprimer la documentation DPoP

**If Implementing**:
```
src/main/java/tn/cyberious/compta/oauth2/dpop/DPoPConfig.java
src/main/java/tn/cyberious/compta/oauth2/dpop/DPoPValidator.java
src/main/java/tn/cyberious/compta/oauth2/dpop/DPoPProofGenerator.java
src/main/java/tn/cyberious/compta/oauth2/dpop/DPoPFilter.java
```

---

## Bugs & Issues - ✅ ALL FIXED

All 8 critical issues have been fixed. See [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md#bug-fixes-applied) for details.

---

## Medium Priority Tasks - ✅ MOSTLY COMPLETED

8 of 9 tasks completed. See [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md#medium-priority-tasks) for implementation details.

### Remaining Task: Implement Token Binding (DPoP)

DPoP (Demonstrating Proof-of-Possession) is documented but **not actually implemented**.

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/dpop/DPoPConfig.java`
- `src/main/java/tn/cyberious/compta/oauth2/dpop/DPoPValidator.java`
- `src/main/java/tn/cyberious/compta/oauth2/dpop/DPoPProofGenerator.java`
- `src/main/java/tn/cyberious/compta/oauth2/dpop/DPoPFilter.java`

See RFC 9449 for implementation details.

---

## Detailed Code Analysis (2026-01-17)

### File-by-File Review

#### AuthorizationServerConfig.java (8/10)
**Location**: `config/AuthorizationServerConfig.java`

**Positifs**:
- Deux filter chains bien séparées (OAuth2 vs Default)
- CORS configurable via CorsProperties
- CSRF protection appropriée
- JWT authentication converter injecté

**Problèmes**:
- Lignes 196-197: Redirect URIs hardcodées `http://localhost:3000/authorized`
- Ligne 224: Post-logout redirect URI hardcodée
- Pas de validation des redirect URIs contre une whitelist

---

#### TokenBlacklistService.java (9/10)
**Location**: `jti/TokenBlacklistService.java`

**Positifs**:
- Double-couche cache/DB bien implémentée
- Lazy initialization du cache
- Cleanup automatique des entrées expirées
- Gestion des métadonnées (revoked_by, reason)

**Problèmes mineurs**:
- Ligne 63: `rs.getTimestamp().toInstant()` peut échouer si timestamp est null

---

#### RateLimitFilter.java (7/10)
**Location**: `filter/RateLimitFilter.java`

**Positifs**:
- Support des headers proxy (X-Forwarded-For, X-Real-IP)
- Integration avec les métriques
- Blocage IP supporté

**Problèmes**:
- Lignes 141-152: `RequestCounter.increment()` a une race condition potentielle
- Le cleanup se fait APRÈS l'ajout, ce qui peut fausser le comptage
- `blockIp()` n'est jamais appelé (méthode morte)

**Fix suggéré**:
```java
public int increment() {
    long now = System.currentTimeMillis();
    long cutoff = now - lastWindowSize;
    // Cleanup AVANT l'ajout
    timestamps.entrySet().removeIf(entry -> entry.getKey() < cutoff);
    timestamps.compute(now, (k, v) -> v == null ? new AtomicInteger(1) : v);
    return timestamps.values().stream().mapToInt(AtomicInteger::get).sum();
}
```

---

#### KeyManagementService.java (9/10)
**Location**: `service/KeyManagementService.java`

**Positifs**:
- Rotation automatique via @Scheduled
- Grace period pour transition douce
- Configuration externalisée
- Génération RSA 2048-bit

**Problèmes mineurs**:
- Ligne 181: `queryForObject` peut retourner null et causer NPE
- Pas de backup des clés avant rotation

---

#### UserRepository.java (9/10)
**Location**: `repository/UserRepository.java`

**Positifs**:
- jOOQ type-safe
- Méthodes bien nommées
- Gestion des timestamps

**Problèmes mineurs**:
- Lignes 75-88: `updateFields()` ne vérifie pas si les champs sont non-null avant `.set()`

---

#### OAuth2Metrics.java (9/10)
**Location**: `metrics/OAuth2Metrics.java`

**Positifs**:
- 50+ métriques Micrometer
- Tags pour filtrage (client_id, grant_type, status)
- Timers pour performance
- Bien organisé par catégorie

**Problèmes mineurs**:
- Lignes 387-394: Switch case avec clients hardcodés (public-client, gateway)
- Nouveaux clients ne seront pas trackés

---

#### AuditLogAspect.java (8/10)
**Location**: `aspect/AuditLogAspect.java`

**Positifs**:
- Pointcuts bien définis
- Logging asynchrone
- Capture complète (IP, User-Agent, duration)
- Gestion des erreurs

**Problèmes**:
- Ligne 43: `TokenRevocationController` n'existe pas (c'est `RevocationController`)
- Ligne 47: `TokenIntrospectionController` n'existe pas (c'est `IntrospectionController`)
- Ces pointcuts ne matchent probablement pas

---

#### CustomUserDetailsService.java (8/10)
**Location**: `security/CustomUserDetailsService.java`

**Positifs**:
- Simple et efficace
- Transaction read-only
- Charge les rôles en une requête

**Problèmes**:
- Pas de logging des tentatives de login
- N'intègre pas avec un mécanisme de lockout

---

### Database Migrations Review

| Migration | Status | Notes |
|-----------|--------|-------|
| V1__oauth2_registered_client.sql | OK | Table standard Spring Auth Server |
| V2__oauth2_authorization.sql | OK | Table standard |
| V3__oauth2_authorization_consent.sql | OK | Table standard |
| V4__user_authentication_tables.sql | OK | Users, roles, user_roles |
| V5__oauth2_keys.sql | OK | RSA keys storage |
| V6__create_password_reset_tables.sql | OK | Tokens avec expiration |
| V7__create_email_verification_tables.sql | OK | Tokens avec expiration |
| V8__create_audit_log_table.sql | OK | JSONB pour details |
| V9__create_token_blacklist_table.sql | OK | JTI blacklist |
| V10__fix_default_user_passwords.sql | OK | BCrypt hashes |

**Total migrations**: 10
**Manquantes**: `failed_login_attempts`, `two_factor_auth`, `user_sessions`

---

### Security Checklist

| Check | Status | Notes |
|-------|--------|-------|
| SQL Injection | SAFE | jOOQ parameterized queries |
| XSS | SAFE | No HTML rendering of user input |
| CSRF | PROTECTED | Cookie-based tokens |
| Brute Force | PARTIAL | Rate limiting only, no lockout |
| Password Storage | SAFE | BCrypt |
| Key Management | SAFE | RSA 2048 with rotation |
| Token Revocation | SAFE | JTI blacklist |
| Audit Logging | GOOD | Comprehensive |
| Secrets | GOOD | Externalized via env vars |

---

## Quick Fixes (Copy-Paste Ready)

### Fix 1: AuditLogAspect Pointcuts

**File**: `src/main/java/tn/cyberious/compta/oauth2/aspect/AuditLogAspect.java`

**Current (BROKEN)**:
```java
@Pointcut("execution(* tn.cyberious.compta.oauth2.controller.TokenRevocationController.revokeToken(..))")
public void tokenRevocation() {}

@Pointcut("execution(* tn.cyberious.compta.oauth2.controller.TokenIntrospectionController.introspectToken(..))")
public void tokenIntrospection() {}
```

**Fixed**:
```java
@Pointcut("execution(* tn.cyberious.compta.oauth2.controller.RevocationController.revoke(..))")
public void tokenRevocation() {}

@Pointcut("execution(* tn.cyberious.compta.oauth2.controller.IntrospectionController.introspect(..))")
public void tokenIntrospection() {}
```

---

### Fix 2: RateLimitFilter Race Condition

**File**: `src/main/java/tn/cyberious/compta/oauth2/filter/RateLimitFilter.java`

**Current (lines 141-152)**:
```java
public int increment() {
    long now = System.currentTimeMillis();
    timestamps.compute(now, (k, v) -> v == null ? new AtomicInteger(1) : v);
    long cutoff = now - lastWindowSize;
    timestamps.entrySet().removeIf(entry -> entry.getKey() < cutoff);
    return timestamps.values().stream().mapToInt(AtomicInteger::get).sum();
}
```

**Fixed**:
```java
public int increment() {
    long now = System.currentTimeMillis();
    long cutoff = now - lastWindowSize;
    // Cleanup BEFORE adding to ensure accurate count
    timestamps.entrySet().removeIf(entry -> entry.getKey() < cutoff);
    timestamps.compute(now, (k, v) -> v == null ? new AtomicInteger(1) : v);
    return timestamps.values().stream().mapToInt(AtomicInteger::get).sum();
}
```

---

### Fix 3: Externalize Redirect URIs

**File**: `src/main/resources/application.yml`

**Add**:
```yaml
oauth2:
  clients:
    public-client:
      redirect-uris: ${PUBLIC_CLIENT_REDIRECT_URIS:http://localhost:3000/authorized}
      post-logout-redirect-uris: ${PUBLIC_CLIENT_POST_LOGOUT_URIS:http://localhost:3000}
    gateway:
      redirect-uris: ${GATEWAY_REDIRECT_URIS:http://localhost:8080/authorized}
```

**File**: `src/main/java/tn/cyberious/compta/oauth2/config/AuthorizationServerConfig.java`

**Add fields**:
```java
@Value("${oauth2.clients.public-client.redirect-uris}")
private String publicClientRedirectUris;

@Value("${oauth2.clients.public-client.post-logout-redirect-uris}")
private String publicClientPostLogoutUris;

@Value("${oauth2.clients.gateway.redirect-uris}")
private String gatewayRedirectUris;
```

**Update initializeDefaultClients() lines 196-197, 224**:
```java
// Replace hardcoded URIs with:
.redirectUri(publicClientRedirectUris)
.postLogoutRedirectUri(publicClientPostLogoutUris)
// ...
.redirectUri(gatewayRedirectUris)
```

---

### Fix 4: Remove Dead Code

**File**: `src/main/java/tn/cyberious/compta/oauth2/filter/RateLimitFilter.java`

**Remove unused method** (line 126-128):
```java
// DELETE THIS - never called
private void blockIp(String clientIp, long blockDurationMillis) {
    blockedUntil.put(clientIp, Instant.now().plusMillis(blockDurationMillis));
}
```

---

### Fix 5: KeyManagementService NPE Protection

**File**: `src/main/java/tn/cyberious/compta/oauth2/service/KeyManagementService.java`

**Current (line 181)**:
```java
LocalDateTime expiresAt = jdbcTemplate.queryForObject(sql, LocalDateTime.class, keyId);
if (expiresAt == null) {
    return true;
}
```

**Fixed**:
```java
List<LocalDateTime> results = jdbcTemplate.queryForList(sql, LocalDateTime.class, keyId);
if (results.isEmpty() || results.get(0) == null) {
    return true;
}
LocalDateTime expiresAt = results.get(0);
```

---

## Low Priority Tasks

### 10. Implement Device Code Flow

**Current State:**
- Device Code Flow not implemented
- No support for devices without browsers (IoT, smart TVs, etc.)

**Problem:**
- Cannot authenticate on devices without browsers
- Limited to web and mobile apps

**Solution:**
Implement RFC 8628 Device Code Flow

**Implementation Details:**

1. **Add Device Code Grant Type to Clients:**
```java
.authorizationGrantType(AuthorizationGrantType.DEVICE_CODE)
```

2. **Device Code Flow:**
```
Device requests code → User code displayed → 
User enters code on another device → User authorizes → 
Device polls for token → Token issued
```

3. **Create Device Code Tables:**
```sql
CREATE TABLE oauth2.device_codes (
    id VARCHAR(255) PRIMARY KEY,
    device_code VARCHAR(255) NOT NULL UNIQUE,
    user_code VARCHAR(255) NOT NULL UNIQUE,
    registered_client_id VARCHAR(255) NOT NULL,
    principal_name VARCHAR(256),
    authorized_scopes VARCHAR(1000),
    expires_at TIMESTAMP NOT NULL,
    last_poll_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/controller/DeviceAuthorizationController.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/DeviceCodeService.java`
- `src/main/resources/db/migration/V10__device_codes.sql`

---

### 11. Implement Dynamic Client Registration

**Current State:**
- Clients must be registered manually
- No RFC 7591 Dynamic Client Registration

**Problem:**
- Third-party developers cannot register their own clients
- Requires admin intervention for each new client
- Not suitable for public OAuth2 providers

**Solution:**
Implement RFC 7591 Dynamic Client Registration

**Implementation Details:**

1. **Registration Endpoint:**
```
POST /oauth2/register
Content-Type: application/json

{
  "redirect_uris": ["https://example.com/callback"],
  "grant_types": ["authorization_code", "refresh_token"],
  "response_types": ["code"],
  "client_name": "My App",
  "client_uri": "https://example.com",
  "logo_uri": "https://example.com/logo.png",
  "scopes": ["openid", "profile", "email"],
  "token_endpoint_auth_method": "client_secret_basic"
}
```

2. **Registration Response:**
```json
{
  "client_id": "s6BhdRkqt3",
  "client_secret": "7Fjfp0ZBr1KtDRbnfVdmIw",
  "client_id_issued_at": 1234567890,
  "client_secret_expires_at": 0,
  "registration_access_token": "this.is.a.registration.access.token",
  "registration_client_uri": "https://server.example.com/connect/register/s6BhdRkqt3"
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/controller/DynamicClientRegistrationController.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/DynamicClientRegistrationService.java`
- `src/main/java/tn/cyberious/compta/oauth2/dto/ClientRegistrationRequest.java`
- `src/main/java/tn/cyberious/compta/oauth2/dto/ClientRegistrationResponse.java`

---

### 12. Implement Consent Management UI

**Current State:**
- No UI for managing user consents
- Users cannot revoke app authorizations

**Problem:**
- Users cannot see which apps have access
- No way to revoke app permissions
- Privacy concern

**Solution:**
Create consent management UI

**Features:**
- List all authorized applications
- Show granted scopes per application
- Revoke application authorization
- View authorization history

**Endpoints:**
- `GET /api/user/consents` - List user's consents
- `DELETE /api/user/consents/{id}` - Revoke consent

**Implementation Details:**
```java
@RestController
@RequestMapping("/api/user/consents")
public class ConsentManagementController {
    
    @GetMapping
    public List<ConsentResponse> getUserConsents(Principal principal);
    
    @DeleteMapping("/{id}")
    public void revokeConsent(@PathVariable String id, Principal principal);
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/controller/ConsentManagementController.java`
- `src/main/java/tn/cyberious/compta/oauth2/dto/ConsentResponse.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/ConsentManagementService.java`

---

### 13. Implement Global Logout / Single Sign-Out (SLO)

**Current State:**
- No global logout mechanism
- Revoking one token doesn't invalidate others

**Problem:**
- User logout doesn't revoke all tokens
- Tokens remain valid after logout
- Security risk

**Solution:**
Implement global logout with SLO support

**Implementation Details:**

1. **Global Logout Endpoint:**
```
POST /oauth2/logout
Authorization: Bearer <access_token>
```

2. **Logout Logic:**
- Revoke all tokens for user
- Invalidate refresh tokens
- Notify all clients via back-channel logout
- Clear user sessions

3. **Back-Channel Logout (OIDC):**
```java
@Service
public class BackChannelLogoutService {
    
    public void notifyClients(String userId);
    public void sendLogoutToken(String clientId, String sid);
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/controller/LogoutController.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/BackChannelLogoutService.java`

---

### 14. Implement Multi-Tenancy Support

**Current State:**
- No multi-tenancy support
- Single tenant architecture

**Problem:**
- Cannot support multiple organizations
- No data isolation between tenants
- Not suitable for SaaS model

**Solution:**
Implement multi-tenancy with tenant isolation

**Implementation Details:**

1. **Add Tenant to Users Table:**
```sql
ALTER TABLE oauth2.users ADD COLUMN tenant_id UUID;
ALTER TABLE oauth2.users ADD CONSTRAINT fk_users_tenant 
    FOREIGN KEY (tenant_id) REFERENCES oauth2.tenants(id);
```

2. **Create Tenants Table:**
```sql
CREATE TABLE oauth2.tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    domain VARCHAR(255),
    settings JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

3. **Tenant Context Filter:**
```java
@Component
public class TenantContextFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {
        // Extract tenant from request and set in context
    }
}
```

4. **Tenant-Aware JWT Claims:**
```java
{
  "sub": "user123",
  "tenant_id": "tenant456",
  "tenant_slug": "acme-corp"
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/filter/TenantContextFilter.java`
- `src/main/java/tn/cyberious/compta/oauth2/context/TenantContext.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/TenantService.java`
- `src/main/resources/db/migration/V11__tenants.sql`

---

### 15. Implement FIDO2/WebAuthn Authentication

**Current State:**
- Only username/password authentication
- No passwordless authentication

**Problem:**
- Passwords are vulnerable to phishing
- Users forget passwords
- Security risk

**Solution:**
Implement FIDO2/WebAuthn for passwordless authentication

**Implementation Details:**

1. **WebAuthn Registration Flow:**
```
User registers → Server generates challenge → 
User authenticates with biometrics/security key → 
Credential stored → Registration complete
```

2. **WebAuthn Authentication Flow:**
```
User logs in → Server generates challenge → 
User authenticates with biometrics/security key → 
Authentication successful
```

3. **Create WebAuthn Tables:**
```sql
CREATE TABLE oauth2.webauthn_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    credential_id VARCHAR(255) NOT NULL UNIQUE,
    public_key TEXT NOT NULL,
    counter BIGINT DEFAULT 0,
    transports VARCHAR(100),
    backup_eligible BOOLEAN DEFAULT FALSE,
    backup_status BOOLEAN DEFAULT FALSE,
    user_handle VARCHAR(255) NOT NULL,
    device_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES oauth2.users(id) ON DELETE CASCADE
);
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/controller/WebAuthnController.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/WebAuthnService.java`
- `src/main/java/tn/cyberious/compta/oauth2/security/WebAuthnAuthenticationProvider.java`
- `src/main/resources/db/migration/V12__webauthn_credentials.sql`

**Dependencies to Add:**
```xml
<dependency>
    <groupId>com.yubico</groupId>
    <artifactId>webauthn-server-core</artifactId>
    <version>2.5.2</version>
</dependency>
```

---

### 16. Implement Two-Factor Authentication (2FA)

**Current State:**
- No 2FA support
- Only single-factor authentication

**Problem:**
- Compromised passwords give full access
- Security risk for sensitive operations

**Solution:**
Implement TOTP-based 2FA

**Implementation Details:**

1. **Add 2FA to Users Table:**
```sql
ALTER TABLE oauth2.users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE oauth2.users ADD COLUMN two_factor_secret VARCHAR(255);
ALTER TABLE oauth2.users ADD COLUMN backup_codes TEXT[];
```

2. **2FA Login Flow:**
```
User enters credentials → Check if 2FA enabled → 
If yes, require TOTP code → Validate TOTP → 
Login successful
```

3. **2FA Service:**
```java
@Service
public class TwoFactorAuthService {
    
    public String generateSecret();
    public String generateQRCode(String secret, String username);
    public boolean validateCode(String secret, String code);
    public List<String> generateBackupCodes();
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/service/TwoFactorAuthService.java`
- `src/main/java/tn/cyberious/compta/oauth2/controller/TwoFactorAuthController.java`
- `src/main/java/tn/cyberious/compta/oauth2/dto/Enable2FARequest.java`
- `src/main/java/tn/cyberious/compta/oauth2/dto/Verify2FARequest.java`

**Dependencies to Add:**
```xml
<dependency>
    <groupId>dev.samstevens.totp</groupId>
    <artifactId>totp</artifactId>
    <version>1.7.1</version>
</dependency>
```

---

### 17. Implement Account Lockout Policy

**Current State:**
- No account lockout after failed attempts
- Vulnerable to brute force attacks

**Problem:**
- Brute force attacks can succeed eventually
- No protection against credential stuffing

**Solution:**
Implement progressive account lockout

**Implementation Details:**

1. **Create Failed Login Attempts Table:**
```sql
CREATE TABLE oauth2.failed_login_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    attempt_count INTEGER DEFAULT 1,
    first_attempt_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_attempt_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    locked_until TIMESTAMP
);

CREATE INDEX idx_failed_login_attempts_username ON oauth2.failed_login_attempts(username);
```

2. **Lockout Policy:**
- 5 failed attempts → 15 minute lockout
- 10 failed attempts → 1 hour lockout
- 15 failed attempts → 24 hour lockout
- Reset counter on successful login

3. **Lockout Service:**
```java
@Service
public class AccountLockoutService {
    
    public void recordFailedAttempt(String username, String ipAddress);
    public boolean isAccountLocked(String username);
    public void resetFailedAttempts(String username);
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/service/AccountLockoutService.java`
- `src/main/java/tn/cyberious/compta/oauth2/handler/AuthenticationFailureHandler.java`
- `src/main/resources/db/migration/V13__failed_login_attempts.sql`

**Configuration:**
```yaml
oauth2:
  account-lockout:
    enabled: true
    max-attempts: 5
    lockout-duration: 900  # 15 minutes
    progressive-lockout: true
```

---

### 18. Implement Session Management

**Current State:**
- No session management
- Cannot view active sessions
- Cannot revoke specific sessions

**Problem:**
- Users cannot see where they're logged in
- Cannot revoke specific device sessions
- Security risk

**Solution:**
Implement session management

**Implementation Details:**

1. **Create Sessions Table:**
```sql
CREATE TABLE oauth2.user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_type VARCHAR(50),
    device_name VARCHAR(255),
    location_country VARCHAR(100),
    location_city VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES oauth2.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user_id ON oauth2.user_sessions(user_id);
CREATE INDEX idx_user_sessions_expires_at ON oauth2.user_sessions(expires_at);
```

2. **Session Management Endpoints:**
- `GET /api/user/sessions` - List active sessions
- `DELETE /api/user/sessions/{id}` - Revoke specific session
- `DELETE /api/user/sessions` - Revoke all sessions except current

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/controller/SessionManagementController.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/SessionManagementService.java`
- `src/main/java/tn/cyberious/compta/oauth2/dto/SessionResponse.java`
- `src/main/resources/db/migration/V14__user_sessions.sql`

---

### 19. Implement Social Login (OAuth2/OIDC)

**Current State:**
- No social login integration
- Users cannot login with Google, GitHub, etc.

**Problem:**
- Users prefer social login
- Reduces friction for new users
- Competitive disadvantage

**Solution:**
Integrate OAuth2/OIDC social providers

**Supported Providers:**
- Google
- GitHub
- Microsoft
- Facebook
- Apple

**Implementation Details:**

1. **Create Social Accounts Table:**
```sql
CREATE TABLE oauth2.social_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    access_token TEXT,
    refresh_token TEXT,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_synced_at TIMESTAMP,
    UNIQUE(provider, provider_user_id),
    FOREIGN KEY (user_id) REFERENCES oauth2.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_social_accounts_user_id ON oauth2.social_accounts(user_id);
```

2. **Social Login Flow:**
```
User clicks "Login with Google" → Redirect to Google → 
User authorizes → Redirect back with code → 
Exchange code for user info → Create/link account → Issue tokens
```

3. **Social Login Service:**
```java
@Service
public class SocialLoginService {
    
    public String getAuthorizationUrl(String provider, String redirectUri);
    public OAuth2User authenticate(String provider, String code);
    public void linkSocialAccount(UUID userId, String provider, String providerUserId);
    public void unlinkSocialAccount(UUID userId, String provider);
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/controller/SocialLoginController.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/SocialLoginService.java`
- `src/main/java/tn/cyberious/compta/oauth2/config/SocialLoginConfig.java`
- `src/main/resources/db/migration/V15__social_accounts.sql`

**Configuration:**
```yaml
oauth2:
  social:
    google:
      client-id: ${GOOGLE_CLIENT_ID}
      client-secret: ${GOOGLE_CLIENT_SECRET}
      scope: openid,profile,email
    github:
      client-id: ${GITHUB_CLIENT_ID}
      client-secret: ${GITHUB_CLIENT_SECRET}
      scope: user:email
```

---

## Testing Tasks

### 20. Write Integration Tests

**Current State:**
- Only basic test class exists
- No OAuth2 flow tests

**Tests to Implement:**

**Authorization Code Flow Tests:**
- Test authorization request
- Test token exchange
- Test PKCE validation
- Test redirect URI validation

**Client Credentials Flow Tests:**
- Test token issuance
- Test scope validation
- Test client authentication

**Refresh Token Flow Tests:**
- Test token refresh
- Test refresh token reuse detection
- Test refresh token expiration

**Token Introspection Tests:**
- Test valid token introspection
- Test invalid token introspection
- Test revoked token introspection

**Token Revocation Tests:**
- Test access token revocation
- Test refresh token revocation
- Test token reuse after revocation

**User Management Tests:**
- Test user creation
- Test user update
- Test user deletion
- Test role assignment

**Files to Create:**
- `src/test/java/tn/cyberious/compta/oauth2/integration/AuthorizationCodeFlowTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/integration/ClientCredentialsFlowTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/integration/RefreshTokenFlowTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/integration/TokenIntrospectionTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/integration/TokenRevocationTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/integration/UserManagementTest.java`

---

### 21. Write Security Tests

**Tests to Implement:**

**Authentication Tests:**
- Test invalid credentials
- Test SQL injection attempts
- Test XSS attempts

**Authorization Tests:**
- Test unauthorized access
- Test role-based access control
- Test scope validation

**Token Security Tests:**
- Test JWT signature validation
- Test token expiration
- Test token tampering

**Rate Limiting Tests:**
- Test rate limit enforcement
- Test rate limit bypass attempts

**CSRF Tests:**
- Test CSRF token validation
- Test CSRF protection bypass attempts

**Files to Create:**
- `src/test/java/tn/cyberious/compta/oauth2/security/AuthenticationSecurityTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/security/AuthorizationSecurityTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/security/TokenSecurityTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/security/RateLimitTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/security/CSRFTest.java`

---

### 22. Write Performance Tests

**Tests to Implement:**

**Load Tests:**
- Test concurrent token issuance
- Test concurrent authentication
- Test database performance under load

**Stress Tests:**
- Test maximum concurrent users
- Test maximum requests per second
- Test memory usage under load

**Benchmark Tests:**
- Measure token issuance time
- Measure authentication time
- Measure token validation time

**Files to Create:**
- `src/test/java/tn/cyberious/compta/oauth2/performance/LoadTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/performance/StressTest.java`
- `src/test/java/tn/cyberious/compta/oauth2/performance/BenchmarkTest.java`

**Dependencies to Add:**
```xml
<dependency>
    <groupId>io.gatling.highcharts</groupId>
    <artifactId>gatling-charts-highcharts</artifactId>
    <scope>test</scope>
</dependency>
```

---

## DevOps & Operations Tasks

### 23. Externalize Configuration

**Current State:**
- Configuration in [`application.yml`](src/main/resources/application.yml:1)
- Secrets hardcoded in code (e.g., client secrets)
- No environment-specific configuration

**Solution:**
Externalize configuration using Spring Cloud Config or environment variables

**Implementation Details:**

1. **Environment-Specific Configs:**
- `application-dev.yml`
- `application-staging.yml`
- `application-prod.yml`

2. **External Secrets:**
- Use HashiCorp Vault
- Use AWS Secrets Manager
- Use Azure Key Vault

3. **Configuration Example:**
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

oauth2:
  keys:
    rotation:
      enabled: ${KEY_ROTATION_ENABLED:true}
  clients:
    gateway:
      secret: ${GATEWAY_CLIENT_SECRET}
```

**Files to Create:**
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-staging.yml`
- `src/main/resources/application-prod.yml`

---

### 24. Implement Health Checks

**Current State:**
- Basic Spring Boot health checks
- No OAuth2-specific health indicators

**Solution:**
Implement custom health indicators

**Health Indicators:**
- Database connection health
- Key rotation health
- Active token count
- Failed authentication rate
- Token issuance rate

**Implementation Details:**
```java
@Component
public class OAuth2HealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check OAuth2 server health
    }
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/health/OAuth2HealthIndicator.java`
- `src/main/java/tn/cyberious/compta/oauth2/health/DatabaseHealthIndicator.java`
- `src/main/java/tn/cyberious/compta/oauth2/health/KeyManagementHealthIndicator.java`

---

### 25. Implement Distributed Tracing

**Current State:**
- No distributed tracing
- Difficult to debug requests across services

**Solution:**
Implement distributed tracing with Spring Cloud Sleuth or OpenTelemetry

**Implementation Details:**

1. **Add Dependencies:**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

2. **Configuration:**
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

**Files to Modify:**
- `pom.xml` - Add tracing dependencies
- `src/main/resources/application.yml` - Add tracing configuration

---

### 26. Implement Alerting

**Current State:**
- No alerting
- No notification of critical events

**Solution:**
Implement alerting for critical events

**Alerts to Configure:**
- High authentication failure rate
- High token issuance rate (possible attack)
- Database connection failures
- Key rotation failures
- Token revocation rate anomalies

**Implementation Details:**

1. **Use Prometheus Alertmanager:**
```yaml
groups:
  - name: oauth2_alerts
    rules:
      - alert: HighAuthFailureRate
        expr: rate(oauth2_authentications_failure[5m]) > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: High authentication failure rate
```

**Files to Create:**
- `prometheus-alerts.yml`

---

### 27. Implement Backup and Recovery

**Current State:**
- No backup strategy
- No recovery procedures

**Solution:**
Implement backup and recovery procedures

**Backup Strategy:**
1. **Database Backups:**
   - Daily full backups
   - Hourly incremental backups
   - Point-in-time recovery enabled

2. **Key Backups:**
   - Backup RSA keys to secure storage
   - Encrypt key backups
   - Store in multiple locations

3. **Configuration Backups:**
   - Version control configuration
   - Document all changes

**Files to Create:**
- `docs/backup-strategy.md`
- `docs/recovery-procedures.md`
- `scripts/backup-database.sh`
- `scripts/backup-keys.sh`

---

### 28. Implement Monitoring Dashboard

**Current State:**
- No monitoring dashboard
- No visibility into system health

**Solution:**
Create monitoring dashboard with Grafana

**Metrics to Display:**
- Token issuance rate
- Active token count
- Authentication success/failure rate
- Token revocation rate
- Database connection pool usage
- JVM memory usage
- Request latency
- Error rate

**Files to Create:**
- `grafana/dashboards/oauth2-server.json`

---

### 29. Implement CI/CD Pipeline

**Current State:**
- No CI/CD pipeline
- Manual deployment

**Solution:**
Implement CI/CD with GitHub Actions or GitLab CI

**Pipeline Stages:**
1. **Build**
   - Compile code
   - Run unit tests
   - Generate artifacts

2. **Test**
   - Run integration tests
   - Run security tests
   - Run performance tests

3. **Deploy**
   - Deploy to staging
   - Run smoke tests
   - Deploy to production

**Files to Create:**
- `.github/workflows/ci-cd.yml`
- `docker/Dockerfile`
- `docker/docker-compose.yml`
- `k8s/deployment.yaml`
- `k8s/service.yaml`
- `k8s/ingress.yaml`

---

### 30. Implement Log Aggregation

**Current State:**
- Logs only in console/file
- No centralized logging
- Difficult to troubleshoot issues

**Solution:**
Implement log aggregation with ELK Stack or Loki

**Implementation Details:**

1. **Add Dependencies:**
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
</dependency>
```

2. **Log Configuration:**
```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>localhost:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

**Files to Modify:**
- `pom.xml` - Add logstash dependency
- `src/main/resources/logback-spring.xml` - Add logstash appender

---

### 31. Implement Security Scanning

**Current State:**
- No automated security scanning
- Vulnerabilities may go undetected

**Solution:**
Implement automated security scanning

**Tools to Use:**
- OWASP Dependency-Check
- Snyk
- SonarQube
- Trivy

**Implementation Details:**

1. **Add to CI/CD:**
```yaml
- name: Run OWASP Dependency-Check
  run: mvn org.owasp:dependency-check-maven:check

- name: Run Trivy
  run: trivy image oauth2-server:latest
```

**Files to Create:**
- `.github/workflows/security-scan.yml`

---

### 32. Implement API Documentation

**Current State:**
- Basic OpenAPI configuration exists
- No detailed API documentation
- No usage examples

**Solution:**
Enhance API documentation

**Enhancements:**
- Add detailed endpoint descriptions
- Add request/response examples
- Add error response documentation
- Add authentication/authorization requirements
- Add rate limiting information
- Add usage guides

**Files to Create:**
- `docs/api/authentication.md`
- `docs/api/authorization.md`
- `docs/api/user-management.md`
- `docs/api/client-management.md`
- `docs/api/token-management.md`

---

## Summary

This document outlines 32 tasks needed to make the OAuth2 server complete and production-ready. The tasks are organized by priority:

- **High Priority (6 tasks):** ✅ Completed - See [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md)
- **Medium Priority (9 tasks):** ✅ Completed - See [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md)
- **Low Priority (10 tasks):** Nice-to-have features
- **Testing (3 tasks):** Comprehensive test coverage
- **DevOps & Operations (10 tasks):** Production readiness

### Quick Start

To get started, focus on these medium-priority tasks:

1. Add Rate Limiting
2. Configure CORS
3. Implement CSRF Protection
4. Implement Audit Logging
5. Add OAuth2 Specific Metrics
6. Implement Token Binding (mTLS/DPoP)
7. Add JTI (JWT ID) for Token Tracking
8. Implement Password Reset Flow
9. Implement Email Verification

These tasks will enhance the security and observability of the OAuth2 server.
