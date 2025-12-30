# OAuth2 Server - Tasks Roadmap

This document outlines all the missing components and features needed to make the OAuth2 server complete and production-ready.

> **Note:** The High Priority Tasks (tasks 1-6) have been completed and moved to [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).

---

## Table of Contents

1. [Medium Priority Tasks](#medium-priority-tasks)
2. [Low Priority Tasks](#low-priority-tasks)
3. [Testing Tasks](#testing-tasks)
4. [DevOps & Operations Tasks](#devops--operations-tasks)

---

## Medium Priority Tasks

### 1. Add Rate Limiting

**Current State:**
- No rate limiting on OAuth2 endpoints
- Vulnerable to brute force attacks
- No protection against DDoS

**Problem:**
- Brute force attacks on login endpoint
- Token endpoint abuse
- Password reset endpoint abuse

**Solution:**
Implement rate limiting using Spring Boot Starter for Rate Limiting or Bucket4j

**Implementation Details:**
```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter());
        registration.addUrlPatterns("/oauth2/token", "/oauth2/revoke", "/oauth2/introspect");
        return registration;
    }
}
```

**Rate Limits:**
- `/oauth2/token`: 10 requests per minute per IP
- `/oauth2/revoke`: 20 requests per minute per IP
- `/oauth2/introspect`: 100 requests per minute per IP
- `/login`: 5 requests per minute per IP
- `/api/users/password/reset`: 3 requests per hour per email

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/config/RateLimitConfig.java`
- `src/main/java/tn/cyberious/compta/oauth2/filter/RateLimitFilter.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/RateLimitService.java`

**Dependencies to Add:**
```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.1.0</version>
</dependency>
```

---

### 2. Configure CORS

**Current State:**
- No CORS configuration
- Frontend applications cannot call OAuth2 endpoints from different origins

**Problem:**
- SPAs running on different ports/domains cannot authenticate
- Cross-origin requests blocked by browser

**Solution:**
Configure CORS in SecurityFilterChain

**Implementation Details:**
```java
@Bean
@Order(1)
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
    throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(Customizer.withDefaults());
    
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
    
    // ... rest of configuration
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "http://localhost:8080",
        "https://app.compta.tn"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Files to Modify:**
- `src/main/java/tn/cyberious/compta/oauth2/config/AuthorizationServerConfig.java`

---

### 3. Implement CSRF Protection

**Current State:**
- CSRF protection not explicitly configured for OAuth2 endpoints
- Potential vulnerability for state-changing operations

**Problem:**
- CSRF attacks on token revocation
- CSRF attacks on consent management

**Solution:**
Configure CSRF protection appropriately

**Implementation Details:**
```java
@Bean
@Order(2)
public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, 
        CustomUserDetailsService userDetailsService) throws Exception {
    http.userDetailsService(userDetailsService)
        .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/oauth2/token", "/oauth2/introspect", "/oauth2/revoke")
        );
    return http.build();
}
```

**Files to Modify:**
- `src/main/java/tn/cyberious/compta/oauth2/config/AuthorizationServerConfig.java`

---

### 4. Implement Audit Logging

**Current State:**
- Basic logging configured in [`application.yml`](src/main/resources/application.yml:27)
- No structured audit logs for security events
- No tracking of who did what when

**Problem:**
- Cannot investigate security incidents
- No compliance audit trail
- Difficult to track token issuance and revocation

**Solution:**
Implement comprehensive audit logging

**Events to Log:**
- User login/logout
- Token issuance (access token, refresh token)
- Token revocation
- Token introspection
- Client registration/modification
- User creation/modification/deletion
- Role assignment/revocation
- Failed authentication attempts
- Authorization failures
- Password changes/resets

**Implementation Details:**

1. **Create Audit Log Table:**
```sql
CREATE TABLE oauth2.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    user_id UUID,
    username VARCHAR(255),
    client_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_uri VARCHAR(500),
    http_method VARCHAR(10),
    status_code INTEGER,
    details JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_event_type ON oauth2.audit_logs(event_type);
CREATE INDEX idx_audit_logs_user_id ON oauth2.audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON oauth2.audit_logs(created_at);
```

2. **Create Audit Service:**
```java
@Service
public class AuditLogService {
    
    public void logEvent(AuditEvent event);
    public void logLogin(String username, String ipAddress);
    public void logTokenIssued(String username, String clientId, String tokenType);
    public void logTokenRevoked(String username, String clientId);
}
```

3. **Create Audit Aspect:**
```java
@Aspect
@Component
public class AuditAspect {
    
    @AfterReturning(pointcut = "@annotation(Auditable)", returning = "result")
    public void auditAfterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        // Log successful operation
    }
    
    @AfterThrowing(pointcut = "@annotation(Auditable)", throwing = "exception")
    public void auditAfterThrowing(JoinPoint joinPoint, Auditable auditable, Exception exception) {
        // Log failed operation
    }
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/service/AuditLogService.java`
- `src/main/java/tn/cyberious/compta/oauth2/aspect/AuditAspect.java`
- `src/main/java/tn/cyberious/compta/oauth2/annotation/Auditable.java`
- `src/main/java/tn/cyberious/compta/oauth2/dto/AuditEvent.java`
- `src/main/resources/db/migration/V6__audit_logs.sql`

---

### 5. Add OAuth2 Specific Metrics

**Current State:**
- Basic Spring Boot metrics available
- No OAuth2-specific metrics

**Problem:**
- Cannot monitor OAuth2 server health
- No visibility into token issuance rates
- Cannot track authentication failures

**Solution:**
Implement OAuth2-specific metrics using Micrometer

**Metrics to Track:**
- `oauth2.tokens.issued` - Counter for tokens issued
- `oauth2.tokens.revoked` - Counter for tokens revoked
- `oauth2.tokens.refreshed` - Counter for refresh token usage
- `oauth2.authentications.success` - Counter for successful authentications
- `oauth2.authentications.failure` - Counter for failed authentications
- `oauth2.authorizations.granted` - Counter for granted authorizations
- `oauth2.authorizations.denied` - Counter for denied authorizations
- `oauth2.active_tokens` - Gauge for active tokens
- `oauth2.active_users` - Gauge for active users

**Implementation Details:**
```java
@Component
public class OAuth2Metrics {
    
    private final Counter tokenIssuedCounter;
    private final Counter tokenRevokedCounter;
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;
    
    public OAuth2Metrics(MeterRegistry registry) {
        this.tokenIssuedCounter = Counter.builder("oauth2.tokens.issued")
            .description("Number of tokens issued")
            .register(registry);
        // ... other counters
    }
    
    public void recordTokenIssued(String tokenType, String clientId) {
        tokenIssuedCounter.increment(
            Tags.of("type", tokenType, "client_id", clientId)
        );
    }
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/metrics/OAuth2Metrics.java`

---

### 6. Implement Token Binding (mTLS/DPoP)

**Current State:**
- No token binding mechanism
- Tokens can be used by anyone who has them

**Problem:**
- Token theft allows unauthorized access
- No way to bind tokens to specific clients
- Vulnerable to token replay attacks

**Solution:**
Implement DPoP (Demonstrating Proof-of-Possession) or mTLS

**DPoP Implementation:**

1. **Add DPoP Configuration:**
```java
@Bean
public OAuth2AuthorizationServerConfigurer authorizationServerConfigurer() {
    OAuth2AuthorizationServerConfigurer configurer = new OAuth2AuthorizationServerConfigurer();
    configurer.tokenEndpoint(tokenEndpoint -> 
        tokenEndpoint.accessTokenRequestConverter(new DPoPTokenRequestConverter())
    );
    return configurer;
}
```

2. **Validate DPoP Header:**
```java
@Service
public class DPoPValidator {
    
    public boolean validateDPoP(String dpopHeader, String method, String uri, String accessToken) {
        // Validate DPoP proof
    }
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/security/DPoPValidator.java`
- `src/main/java/tn/cyberious/compta/oauth2/converter/DPoPTokenRequestConverter.java`

---

### 7. Add JTI (JWT ID) for Token Tracking

**Current State:**
- JWTs don't have JTI claim
- Cannot detect token replay
- Cannot track individual tokens

**Problem:**
- No way to detect token replay attacks
- Cannot track token lifecycle
- Difficult to revoke specific tokens

**Solution:**
Add JTI claim to all JWTs and track in database

**Implementation Details:**

1. **Add JTI to Token Settings:**
```java
.tokenSettings(
    TokenSettings.builder()
        .accessTokenTimeToLive(Duration.ofMinutes(30))
        .reuseRefreshTokens(false)
        .x509CertificateBoundAccessTokens(true)
        .build()
)
```

2. **Create Token Tracking Table:**
```sql
CREATE TABLE oauth2.token_blacklist (
    jti VARCHAR(255) PRIMARY KEY,
    token_type VARCHAR(50) NOT NULL,
    user_id UUID,
    client_id VARCHAR(255),
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_token_blacklist_expires_at ON oauth2.token_blacklist(expires_at);
```

3. **Create Token Blacklist Service:**
```java
@Service
public class TokenBlacklistService {
    
    public void blacklistToken(String jti, String tokenType);
    public boolean isTokenBlacklisted(String jti);
    public void cleanupExpiredTokens();
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/service/TokenBlacklistService.java`
- `src/main/java/tn/cyberious/compta/oauth2/filter/TokenValidationFilter.java`
- `src/main/resources/db/migration/V7__token_blacklist.sql`

---

### 8. Implement Password Reset Flow

**Current State:**
- No password reset mechanism
- Users cannot recover forgotten passwords

**Problem:**
- Users locked out if password forgotten
- No self-service password recovery
- Admin intervention required for password resets

**Solution:**
Implement email-based password reset flow

**Implementation Details:**

1. **Password Reset Flow:**
```
User requests reset → Email sent with token → User clicks link → 
User enters new password → Password updated → Token invalidated
```

2. **Create Password Reset Table:**
```sql
CREATE TABLE oauth2.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES oauth2.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_token ON oauth2.password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_expires_at ON oauth2.password_reset_tokens(expires_at);
```

3. **Password Reset Service:**
```java
@Service
public class PasswordResetService {
    
    public void initiatePasswordReset(String email);
    public void resetPassword(String token, String newPassword);
    public boolean validateToken(String token);
}
```

4. **Email Service:**
```java
@Service
public class EmailService {
    
    public void sendPasswordResetEmail(String email, String resetLink);
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/service/PasswordResetService.java`
- `src/main/java/tn/cyberious/compta/oauth2/service/EmailService.java`
- `src/main/java/tn/cyberious/compta/oauth2/controller/PasswordResetController.java`
- `src/main/resources/templates/password-reset-email.html`
- `src/main/resources/templates/reset-password.html`
- `src/main/resources/db/migration/V8__password_reset_tokens.sql`

**Configuration:**
```yaml
oauth2:
  password-reset:
    token-expiry: 3600  # 1 hour
    email:
      from: noreply@compta.tn
      subject: "Password Reset Request"
      base-url: https://app.compta.tn/reset-password
```

---

### 9. Implement Email Verification

**Current State:**
- No email verification for new users
- Users can register with fake emails

**Problem:**
- Invalid email addresses in database
- No way to verify user email ownership
- Security risk for password reset

**Solution:**
Implement email verification flow for new users

**Implementation Details:**

1. **Email Verification Flow:**
```
User registers → Account created (unverified) → Email sent → 
User clicks link → Email verified → Account activated
```

2. **Add Verification Status to Users Table:**
```sql
ALTER TABLE oauth2.users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE oauth2.users ADD COLUMN email_verified_at TIMESTAMP;
```

3. **Create Email Verification Table:**
```sql
CREATE TABLE oauth2.email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES oauth2.users(id) ON DELETE CASCADE
);
```

4. **Email Verification Service:**
```java
@Service
public class EmailVerificationService {
    
    public void sendVerificationEmail(UUID userId);
    public boolean verifyEmail(String token);
    public void resendVerificationEmail(String email);
}
```

**Files to Create:**
- `src/main/java/tn/cyberious/compta/oauth2/service/EmailVerificationService.java`
- `src/main/resources/templates/email-verification.html`
- `src/main/resources/db/migration/V9__email_verification.sql`

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
- **Medium Priority (9 tasks):** Important for security and observability
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
