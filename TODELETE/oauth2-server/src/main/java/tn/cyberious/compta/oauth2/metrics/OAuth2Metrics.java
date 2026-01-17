package tn.cyberious.compta.oauth2.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for OAuth2 operations. Tracks token issuance, revocation, authentication events,
 * and other security-related metrics.
 */
@Component
public class OAuth2Metrics implements MeterBinder {

  // Counters for token operations
  private Counter tokenIssuedCounter;
  private Counter tokenRefreshedCounter;
  private Counter tokenRevokedCounter;
  private Counter tokenIntrospectedCounter;
  private Counter authorizationCodeIssuedCounter;
  private Counter authorizationGrantedCounter;
  private Counter authorizationDeniedCounter;

  // Counters for authentication
  private Counter loginSuccessCounter;
  private Counter loginFailureCounter;
  private Counter logoutCounter;

  // Counters by grant type
  private Counter authorizationCodeGrantCounter;
  private Counter refreshTokenGrantCounter;
  private Counter clientCredentialsGrantCounter;

  // Dynamic counters by client (supports any client, not just hardcoded ones)
  private final Map<String, Counter> clientCounters = new ConcurrentHashMap<>();
  private MeterRegistry meterRegistry;

  // Counters for errors
  private Counter invalidGrantCounter;
  private Counter invalidClientCounter;
  private Counter invalidScopeCounter;
  private Counter unauthorizedClientCounter;

  // Counters for security events
  private Counter rateLimitExceededCounter;
  private Counter csrfValidationFailedCounter;

  // Counters for user management
  private Counter userCreatedCounter;
  private Counter userUpdatedCounter;
  private Counter userDeletedCounter;
  private Counter roleAssignedCounter;
  private Counter roleRemovedCounter;
  private Counter passwordChangedCounter;

  // Counters for password reset
  private Counter passwordResetRequestedCounter;
  private Counter passwordResetCompletedCounter;

  // Counters for email verification
  private Counter emailVerificationRequestedCounter;
  private Counter emailVerificationCompletedCounter;

  // Timers for performance
  private Timer tokenIssuanceTimer;
  private Timer tokenRefreshTimer;
  private Timer authenticationTimer;
  private Timer authorizationTimer;
  private Timer tokenRevocationTimer;
  private Timer tokenIntrospectionTimer;
  private Timer userManagementTimer;
  private Timer passwordResetTimer;
  private Timer emailVerificationTimer;

  // Counters for active tokens
  private Counter activeAccessTokensCounter;
  private Counter activeRefreshTokensCounter;

  @Override
  public void bindTo(MeterRegistry registry) {
    // Token operation counters
    tokenIssuedCounter =
        Counter.builder("oauth2.token.issued")
            .description("Number of access tokens issued")
            .tag("type", "access")
            .register(registry);

    tokenRefreshedCounter =
        Counter.builder("oauth2.token.refreshed")
            .description("Number of tokens refreshed")
            .register(registry);

    tokenRevokedCounter =
        Counter.builder("oauth2.token.revoked")
            .description("Number of tokens revoked")
            .register(registry);

    tokenIntrospectedCounter =
        Counter.builder("oauth2.token.introspected")
            .description("Number of tokens introspected")
            .register(registry);

    authorizationCodeIssuedCounter =
        Counter.builder("oauth2.authorization_code.issued")
            .description("Number of authorization codes issued")
            .register(registry);

    authorizationGrantedCounter =
        Counter.builder("oauth2.authorization.granted")
            .description("Number of authorizations granted")
            .register(registry);

    authorizationDeniedCounter =
        Counter.builder("oauth2.authorization.denied")
            .description("Number of authorizations denied")
            .register(registry);

    // Authentication counters
    loginSuccessCounter =
        Counter.builder("oauth2.login")
            .description("Number of login attempts")
            .tag("status", "success")
            .register(registry);

    loginFailureCounter =
        Counter.builder("oauth2.login")
            .description("Number of login attempts")
            .tag("status", "failure")
            .register(registry);

    logoutCounter =
        Counter.builder("oauth2.logout").description("Number of logout events").register(registry);

    // Grant type counters
    authorizationCodeGrantCounter =
        Counter.builder("oauth2.grant_type")
            .description("Number of token requests by grant type")
            .tag("type", "authorization_code")
            .register(registry);

    refreshTokenGrantCounter =
        Counter.builder("oauth2.grant_type")
            .description("Number of token requests by grant type")
            .tag("type", "refresh_token")
            .register(registry);

    clientCredentialsGrantCounter =
        Counter.builder("oauth2.grant_type")
            .description("Number of token requests by grant type")
            .tag("type", "client_credentials")
            .register(registry);

    // Store registry for dynamic client counter creation
    this.meterRegistry = registry;

    // Error counters
    invalidGrantCounter =
        Counter.builder("oauth2.error")
            .description("Number of OAuth2 errors")
            .tag("error", "invalid_grant")
            .register(registry);

    invalidClientCounter =
        Counter.builder("oauth2.error")
            .description("Number of OAuth2 errors")
            .tag("error", "invalid_client")
            .register(registry);

    invalidScopeCounter =
        Counter.builder("oauth2.error")
            .description("Number of OAuth2 errors")
            .tag("error", "invalid_scope")
            .register(registry);

    unauthorizedClientCounter =
        Counter.builder("oauth2.error")
            .description("Number of OAuth2 errors")
            .tag("error", "unauthorized_client")
            .register(registry);

    // Security event counters
    rateLimitExceededCounter =
        Counter.builder("oauth2.security.rate_limit_exceeded")
            .description("Number of times rate limit was exceeded")
            .register(registry);

    csrfValidationFailedCounter =
        Counter.builder("oauth2.security.csrf_validation_failed")
            .description("Number of CSRF validation failures")
            .register(registry);

    // User management counters
    userCreatedCounter =
        Counter.builder("oauth2.user.created")
            .description("Number of users created")
            .register(registry);

    userUpdatedCounter =
        Counter.builder("oauth2.user.updated")
            .description("Number of users updated")
            .register(registry);

    userDeletedCounter =
        Counter.builder("oauth2.user.deleted")
            .description("Number of users deleted")
            .register(registry);

    roleAssignedCounter =
        Counter.builder("oauth2.user.role_assigned")
            .description("Number of roles assigned")
            .register(registry);

    roleRemovedCounter =
        Counter.builder("oauth2.user.role_removed")
            .description("Number of roles removed")
            .register(registry);

    passwordChangedCounter =
        Counter.builder("oauth2.user.password_changed")
            .description("Number of passwords changed")
            .register(registry);

    // Password reset counters
    passwordResetRequestedCounter =
        Counter.builder("oauth2.password_reset.requested")
            .description("Number of password reset requests")
            .register(registry);

    passwordResetCompletedCounter =
        Counter.builder("oauth2.password_reset.completed")
            .description("Number of password resets completed")
            .register(registry);

    // Email verification counters
    emailVerificationRequestedCounter =
        Counter.builder("oauth2.email_verification.requested")
            .description("Number of email verification requests")
            .register(registry);

    emailVerificationCompletedCounter =
        Counter.builder("oauth2.email_verification.completed")
            .description("Number of email verifications completed")
            .register(registry);

    // Performance timers
    tokenIssuanceTimer =
        Timer.builder("oauth2.token.issuance.duration")
            .description("Time taken to issue tokens")
            .register(registry);

    tokenRefreshTimer =
        Timer.builder("oauth2.token.refresh.duration")
            .description("Time taken to refresh tokens")
            .register(registry);

    authenticationTimer =
        Timer.builder("oauth2.authentication.duration")
            .description("Time taken to authenticate")
            .register(registry);

    authorizationTimer =
        Timer.builder("oauth2.authorization.duration")
            .description("Time taken to authorize")
            .register(registry);

    tokenRevocationTimer =
        Timer.builder("oauth2.token.revocation.duration")
            .description("Time taken to revoke tokens")
            .register(registry);

    tokenIntrospectionTimer =
        Timer.builder("oauth2.token.introspection.duration")
            .description("Time taken to introspect tokens")
            .register(registry);

    userManagementTimer =
        Timer.builder("oauth2.user.management.duration")
            .description("Time taken for user management operations")
            .register(registry);

    passwordResetTimer =
        Timer.builder("oauth2.password_reset.duration")
            .description("Time taken for password reset operations")
            .register(registry);

    emailVerificationTimer =
        Timer.builder("oauth2.email_verification.duration")
            .description("Time taken for email verification operations")
            .register(registry);

    // Active token counters
    activeAccessTokensCounter =
        Counter.builder("oauth2.token.active")
            .description("Number of active tokens")
            .tag("type", "access")
            .register(registry);

    activeRefreshTokensCounter =
        Counter.builder("oauth2.token.active")
            .description("Number of active tokens")
            .tag("type", "refresh")
            .register(registry);
  }

  // Token operation methods
  public void recordTokenIssued(String grantType, String clientId) {
    tokenIssuedCounter.increment();
    recordGrantType(grantType);
    recordClientRequest(clientId);
  }

  public void recordTokenRefreshed(String clientId) {
    tokenRefreshedCounter.increment();
    recordClientRequest(clientId);
  }

  public void recordTokenRevoked() {
    tokenRevokedCounter.increment();
  }

  public void recordTokenIntrospected() {
    tokenIntrospectedCounter.increment();
  }

  public void recordAuthorizationCodeIssued() {
    authorizationCodeIssuedCounter.increment();
  }

  public void recordAuthorizationGranted() {
    authorizationGrantedCounter.increment();
  }

  public void recordAuthorizationDenied() {
    authorizationDeniedCounter.increment();
  }

  // Authentication methods
  public void recordLoginSuccess() {
    loginSuccessCounter.increment();
  }

  public void recordLoginFailure() {
    loginFailureCounter.increment();
  }

  public void recordLogout() {
    logoutCounter.increment();
  }

  // Grant type methods
  private void recordGrantType(String grantType) {
    if (grantType == null) {
      return;
    }

    switch (grantType.toLowerCase()) {
      case "authorization_code":
        authorizationCodeGrantCounter.increment();
        break;
      case "refresh_token":
        refreshTokenGrantCounter.increment();
        break;
      case "client_credentials":
        clientCredentialsGrantCounter.increment();
        break;
    }
  }

  // Client methods - dynamically creates counters for any client
  private void recordClientRequest(String clientId) {
    if (clientId == null || clientId.isEmpty() || meterRegistry == null) {
      return;
    }

    // Get or create counter for this client dynamically
    Counter counter =
        clientCounters.computeIfAbsent(
            clientId,
            id ->
                Counter.builder("oauth2.client.requests")
                    .description("Number of requests by client")
                    .tag("client_id", id)
                    .register(meterRegistry));

    counter.increment();
  }

  // Error methods
  public void recordError(String error) {
    if (error == null) {
      return;
    }

    switch (error) {
      case "invalid_grant":
        invalidGrantCounter.increment();
        break;
      case "invalid_client":
        invalidClientCounter.increment();
        break;
      case "invalid_scope":
        invalidScopeCounter.increment();
        break;
      case "unauthorized_client":
        unauthorizedClientCounter.increment();
        break;
    }
  }

  // Security event methods
  public void recordRateLimitExceeded() {
    rateLimitExceededCounter.increment();
  }

  public void recordCsrfValidationFailed() {
    csrfValidationFailedCounter.increment();
  }

  // User management methods
  public void recordUserCreated() {
    userCreatedCounter.increment();
  }

  public void recordUserUpdated() {
    userUpdatedCounter.increment();
  }

  public void recordUserDeleted() {
    userDeletedCounter.increment();
  }

  public void recordRoleAssigned() {
    roleAssignedCounter.increment();
  }

  public void recordRoleRemoved() {
    roleRemovedCounter.increment();
  }

  public void recordPasswordChanged() {
    passwordChangedCounter.increment();
  }

  // Password reset methods
  public void recordPasswordResetRequested() {
    passwordResetRequestedCounter.increment();
  }

  public void recordPasswordResetCompleted() {
    passwordResetCompletedCounter.increment();
  }

  // Email verification methods
  public void recordEmailVerificationRequested() {
    emailVerificationRequestedCounter.increment();
  }

  public void recordEmailVerificationCompleted() {
    emailVerificationCompletedCounter.increment();
  }

  // Timer methods
  public void recordTokenIssuanceDuration(long duration, TimeUnit unit) {
    tokenIssuanceTimer.record(duration, unit);
  }

  public void recordTokenRefreshDuration(long duration, TimeUnit unit) {
    tokenRefreshTimer.record(duration, unit);
  }

  public void recordAuthenticationDuration(long duration, TimeUnit unit) {
    authenticationTimer.record(duration, unit);
  }

  public void recordAuthorizationDuration(long duration, TimeUnit unit) {
    authorizationTimer.record(duration, unit);
  }

  public void recordTokenRevocationDuration(long duration, TimeUnit unit) {
    tokenRevocationTimer.record(duration, unit);
  }

  public void recordTokenIntrospectionDuration(long duration, TimeUnit unit) {
    tokenIntrospectionTimer.record(duration, unit);
  }

  public void recordUserManagementDuration(long duration, TimeUnit unit) {
    userManagementTimer.record(duration, unit);
  }

  public void recordPasswordResetDuration(long duration, TimeUnit unit) {
    passwordResetTimer.record(duration, unit);
  }

  public void recordEmailVerificationDuration(long duration, TimeUnit unit) {
    emailVerificationTimer.record(duration, unit);
  }

  // Active token methods
  public void incrementActiveAccessTokens() {
    activeAccessTokensCounter.increment();
  }

  public void incrementActiveRefreshTokens() {
    activeRefreshTokensCounter.increment();
  }

  // Getters for testing purposes
  public Counter getTokenIssuedCounter() {
    return tokenIssuedCounter;
  }

  public Counter getTokenRefreshedCounter() {
    return tokenRefreshedCounter;
  }

  public Counter getTokenRevokedCounter() {
    return tokenRevokedCounter;
  }

  public Counter getLoginSuccessCounter() {
    return loginSuccessCounter;
  }

  public Counter getLoginFailureCounter() {
    return loginFailureCounter;
  }
}
