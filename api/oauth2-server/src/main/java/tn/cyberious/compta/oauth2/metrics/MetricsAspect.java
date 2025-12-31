package tn.cyberious.compta.oauth2.metrics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import tn.cyberious.compta.oauth2.service.AuditLogService;

/**
 * Aspect for automatic metrics collection for OAuth2 operations. Intercepts method calls and
 * records metrics using Micrometer.
 *
 * <p>Note: We cannot intercept Spring Security filter classes (like OAuth2TokenEndpointFilter,
 * OAuth2AuthorizationEndpointFilter, UsernamePasswordAuthenticationFilter) using AOP because these
 * are final classes that cannot be proxied by CGLIB. Instead, metrics for OAuth2 operations should
 * be recorded via custom filters or service methods.
 */
@Aspect
@Component
public class MetricsAspect {

  private static final Logger log = LoggerFactory.getLogger(MetricsAspect.class);

  private final OAuth2Metrics oauth2Metrics;
  private final AuditLogService auditLogService;

  public MetricsAspect(OAuth2Metrics oauth2Metrics, AuditLogService auditLogService) {
    this.oauth2Metrics = oauth2Metrics;
    this.auditLogService = auditLogService;
  }

  /** Around advice for measuring token revocation time and recording metrics. */
  @Around(
      "execution(* tn.cyberious.compta.oauth2.controller.TokenRevocationController.revokeToken(..))")
  public Object measureTokenRevocation(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.nanoTime();

    try {
      Object result = joinPoint.proceed();
      long duration = System.nanoTime() - startTime;

      // Record metrics
      oauth2Metrics.recordTokenRevoked();
      oauth2Metrics.recordTokenRevocationDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);

      return result;
    } catch (Exception e) {
      long duration = System.nanoTime() - startTime;
      oauth2Metrics.recordError("token_revocation_failed");
      oauth2Metrics.recordTokenRevocationDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);
      throw e;
    }
  }

  /** Around advice for measuring token introspection time and recording metrics. */
  @Around(
      "execution(* tn.cyberious.compta.oauth2.controller.TokenIntrospectionController.introspectToken(..))")
  public Object measureTokenIntrospection(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.nanoTime();

    try {
      Object result = joinPoint.proceed();
      long duration = System.nanoTime() - startTime;

      // Record metrics
      oauth2Metrics.recordTokenIntrospected();
      oauth2Metrics.recordTokenIntrospectionDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);

      return result;
    } catch (Exception e) {
      long duration = System.nanoTime() - startTime;
      oauth2Metrics.recordError("token_introspection_failed");
      oauth2Metrics.recordTokenIntrospectionDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);
      throw e;
    }
  }

  /** Around advice for measuring user management operations. */
  @Around("execution(* tn.cyberious.compta.oauth2.controller.UserManagementController.*(..))")
  public Object measureUserManagement(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    long startTime = System.nanoTime();

    try {
      Object result = joinPoint.proceed();
      long duration = System.nanoTime() - startTime;

      // Record metrics
      recordUserManagementMetrics(methodName, true);
      oauth2Metrics.recordUserManagementDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);

      return result;
    } catch (Exception e) {
      long duration = System.nanoTime() - startTime;
      recordUserManagementMetrics(methodName, false);
      oauth2Metrics.recordUserManagementDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);
      throw e;
    }
  }

  /** Around advice for measuring password reset operations. */
  @Around("execution(* tn.cyberious.compta.oauth2.controller.PasswordResetController.*(..))")
  public Object measurePasswordReset(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    long startTime = System.nanoTime();

    try {
      Object result = joinPoint.proceed();
      long duration = System.nanoTime() - startTime;

      // Record metrics
      recordPasswordResetMetrics(methodName, true);
      oauth2Metrics.recordPasswordResetDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);

      return result;
    } catch (Exception e) {
      long duration = System.nanoTime() - startTime;
      recordPasswordResetMetrics(methodName, false);
      oauth2Metrics.recordPasswordResetDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);
      throw e;
    }
  }

  /** Around advice for measuring email verification operations. */
  @Around("execution(* tn.cyberious.compta.oauth2.controller.EmailVerificationController.*(..))")
  public Object measureEmailVerification(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    long startTime = System.nanoTime();

    try {
      Object result = joinPoint.proceed();
      long duration = System.nanoTime() - startTime;

      // Record metrics
      recordEmailVerificationMetrics(methodName, true);
      oauth2Metrics.recordEmailVerificationDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);

      return result;
    } catch (Exception e) {
      long duration = System.nanoTime() - startTime;
      recordEmailVerificationMetrics(methodName, false);
      oauth2Metrics.recordEmailVerificationDuration(
          duration, java.util.concurrent.TimeUnit.NANOSECONDS);
      throw e;
    }
  }

  /** Get the current HTTP request. */
  private HttpServletRequest getCurrentRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attributes != null ? attributes.getRequest() : null;
  }

  /** Record user management metrics. */
  private void recordUserManagementMetrics(String methodName, boolean success) {
    if (!success) {
      oauth2Metrics.recordError("user_management_failed");
      return;
    }

    switch (methodName) {
      case "createUser" -> oauth2Metrics.recordUserCreated();
      case "updateUser" -> oauth2Metrics.recordUserUpdated();
      case "deleteUser" -> oauth2Metrics.recordUserDeleted();
      case "assignRole" -> oauth2Metrics.recordRoleAssigned();
      case "removeRole" -> oauth2Metrics.recordRoleRemoved();
      case "changePassword" -> oauth2Metrics.recordPasswordChanged();
      default -> {}
    }
  }

  /** Record password reset metrics. */
  private void recordPasswordResetMetrics(String methodName, boolean success) {
    if (!success) {
      oauth2Metrics.recordError("password_reset_failed");
      return;
    }

    switch (methodName) {
      case "initiatePasswordReset" -> oauth2Metrics.recordPasswordResetRequested();
      case "confirmPasswordReset" -> oauth2Metrics.recordPasswordResetCompleted();
      default -> {}
    }
  }

  /** Record email verification metrics. */
  private void recordEmailVerificationMetrics(String methodName, boolean success) {
    if (!success) {
      oauth2Metrics.recordError("email_verification_failed");
      return;
    }

    switch (methodName) {
      case "initiateEmailVerification" -> oauth2Metrics.recordEmailVerificationRequested();
      case "confirmEmailVerification" -> oauth2Metrics.recordEmailVerificationCompleted();
      default -> {}
    }
  }
}
