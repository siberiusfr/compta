package tn.cyberious.compta.oauth2.aspect;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tn.cyberious.compta.oauth2.dto.AuditLog;
import tn.cyberious.compta.oauth2.service.AuditLogService;

/**
 * Aspect for automatic audit logging of security events. Intercepts method calls and logs them to
 * the audit log table.
 *
 * <p>Note: We cannot intercept Spring Security filter classes (like OAuth2TokenEndpointFilter,
 * OAuth2AuthorizationEndpointFilter, UsernamePasswordAuthenticationFilter, LogoutFilter) using AOP
 * because these are final classes that cannot be proxied by CGLIB. Instead, audit logging for
 * OAuth2 operations should be done via custom filters or service methods.
 */
@Aspect
@Component
public class AuditLogAspect {

  private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

  private final AuditLogService auditLogService;

  public AuditLogAspect(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  /** Pointcut for token revocation. */
  @Pointcut(
      "execution(* tn.cyberious.compta.oauth2.controller.TokenRevocationController.revokeToken(..))")
  public void tokenRevocation() {}

  /** Pointcut for token introspection. */
  @Pointcut(
      "execution(* tn.cyberious.compta.oauth2.controller.TokenIntrospectionController.introspectToken(..))")
  public void tokenIntrospection() {}

  /** Pointcut for user management operations. */
  @Pointcut("execution(* tn.cyberious.compta.oauth2.controller.UserManagementController.*(..))")
  public void userManagement() {}

  /** Pointcut for password reset operations. */
  @Pointcut("execution(* tn.cyberious.compta.oauth2.controller.PasswordResetController.*(..))")
  public void passwordReset() {}

  /** Pointcut for email verification operations. */
  @Pointcut("execution(* tn.cyberious.compta.oauth2.controller.EmailVerificationController.*(..))")
  public void emailVerification() {}

  /** Around advice for logging token revocation events. */
  @Around("tokenRevocation()")
  public Object logTokenRevocation(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = getCurrentRequest();

    try {
      Object result = joinPoint.proceed();

      String clientId = getClientIdFromRequest(request);

      AuditLog auditLog =
          AuditLog.builder()
              .eventType(AuditLog.EventTypes.TOKEN_REVOKED)
              .eventCategory(AuditLog.EventCategories.TOKEN)
              .clientId(clientId)
              .ipAddress(getClientIpAddress(request))
              .userAgent(request.getHeader("User-Agent"))
              .requestUri(request.getRequestURI())
              .requestMethod(request.getMethod())
              .status(AuditLog.Status.SUCCESS)
              .build();

      auditLogService.logAsync(auditLog);

      return result;
    } catch (Exception e) {
      log.error("Error during token revocation", e);
      throw e;
    }
  }

  /** Around advice for logging token introspection events. */
  @Around("tokenIntrospection()")
  public Object logTokenIntrospection(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = getCurrentRequest();

    try {
      Object result = joinPoint.proceed();

      String clientId = getClientIdFromRequest(request);

      AuditLog auditLog =
          AuditLog.builder()
              .eventType(AuditLog.EventTypes.TOKEN_INTROSPECTED)
              .eventCategory(AuditLog.EventCategories.TOKEN)
              .clientId(clientId)
              .ipAddress(getClientIpAddress(request))
              .userAgent(request.getHeader("User-Agent"))
              .requestUri(request.getRequestURI())
              .requestMethod(request.getMethod())
              .status(AuditLog.Status.SUCCESS)
              .build();

      auditLogService.logAsync(auditLog);

      return result;
    } catch (Exception e) {
      log.error("Error during token introspection", e);
      throw e;
    }
  }

  /** Around advice for logging user management events. */
  @Around("userManagement()")
  public Object logUserManagement(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = getCurrentRequest();
    String methodName = joinPoint.getSignature().getName();
    long startTime = System.currentTimeMillis();

    try {
      Object result = joinPoint.proceed();
      long duration = System.currentTimeMillis() - startTime;

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String username = auth != null ? auth.getName() : null;

      AuditLog auditLog =
          AuditLog.builder()
              .eventType(getUserManagementEventType(methodName))
              .eventCategory(AuditLog.EventCategories.USER_MANAGEMENT)
              .username(username)
              .ipAddress(getClientIpAddress(request))
              .userAgent(request.getHeader("User-Agent"))
              .requestUri(request.getRequestURI())
              .requestMethod(request.getMethod())
              .status(AuditLog.Status.SUCCESS)
              .details(createUserManagementDetails(methodName, duration))
              .build();

      auditLogService.logAsync(auditLog);

      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      logUserManagementError(e, request, methodName, duration);
      throw e;
    }
  }

  /** Around advice for logging password reset events. */
  @Around("passwordReset()")
  public Object logPasswordReset(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = getCurrentRequest();
    String methodName = joinPoint.getSignature().getName();

    try {
      Object result = joinPoint.proceed();

      AuditLog auditLog =
          AuditLog.builder()
              .eventType(getPasswordResetEventType(methodName))
              .eventCategory(AuditLog.EventCategories.AUTHENTICATION)
              .ipAddress(getClientIpAddress(request))
              .userAgent(request.getHeader("User-Agent"))
              .requestUri(request.getRequestURI())
              .requestMethod(request.getMethod())
              .status(AuditLog.Status.SUCCESS)
              .build();

      auditLogService.logAsync(auditLog);

      return result;
    } catch (Exception e) {
      logPasswordResetError(e, request, methodName);
      throw e;
    }
  }

  /** Around advice for logging email verification events. */
  @Around("emailVerification()")
  public Object logEmailVerification(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = getCurrentRequest();
    String methodName = joinPoint.getSignature().getName();

    try {
      Object result = joinPoint.proceed();

      AuditLog auditLog =
          AuditLog.builder()
              .eventType(getEmailVerificationEventType(methodName))
              .eventCategory(AuditLog.EventCategories.AUTHENTICATION)
              .ipAddress(getClientIpAddress(request))
              .userAgent(request.getHeader("User-Agent"))
              .requestUri(request.getRequestURI())
              .requestMethod(request.getMethod())
              .status(AuditLog.Status.SUCCESS)
              .build();

      auditLogService.logAsync(auditLog);

      return result;
    } catch (Exception e) {
      logEmailVerificationError(e, request, methodName);
      throw e;
    }
  }

  /** Get the current HTTP request. */
  private HttpServletRequest getCurrentRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attributes != null ? attributes.getRequest() : null;
  }

  /** Get the client IP address from the request. Handles proxies and load balancers. */
  private String getClientIpAddress(HttpServletRequest request) {
    if (request == null) {
      return "unknown";
    }

    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }

    // Handle multiple IPs in X-Forwarded-For
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }

    return ip;
  }

  /** Get the client ID from the request. */
  private String getClientIdFromRequest(HttpServletRequest request) {
    // Try to get from Basic Auth header
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Basic ")) {
      try {
        String decoded = new String(java.util.Base64.getDecoder().decode(authHeader.substring(6)));
        String[] parts = decoded.split(":", 2);
        if (parts.length > 0) {
          return parts[0];
        }
      } catch (Exception e) {
        // Ignore parsing errors
      }
    }

    // Try to get from parameter
    return request.getParameter("client_id");
  }

  /** Get event type from user management method name. */
  private String getUserManagementEventType(String methodName) {
    return switch (methodName) {
      case "createUser" -> AuditLog.EventTypes.USER_CREATED;
      case "updateUser" -> AuditLog.EventTypes.USER_UPDATED;
      case "deleteUser" -> AuditLog.EventTypes.USER_DELETED;
      case "assignRole" -> AuditLog.EventTypes.ROLE_ASSIGNED;
      case "removeRole" -> AuditLog.EventTypes.ROLE_REMOVED;
      case "changePassword" -> AuditLog.EventTypes.PASSWORD_CHANGED;
      default -> AuditLog.EventTypes.USER_MANAGEMENT;
    };
  }

  /** Get event type from password reset method name. */
  private String getPasswordResetEventType(String methodName) {
    return switch (methodName) {
      case "initiatePasswordReset" -> AuditLog.EventTypes.PASSWORD_RESET_REQUESTED;
      case "confirmPasswordReset" -> AuditLog.EventTypes.PASSWORD_RESET_COMPLETED;
      default -> AuditLog.EventTypes.PASSWORD_RESET;
    };
  }

  /** Get event type from email verification method name. */
  private String getEmailVerificationEventType(String methodName) {
    return switch (methodName) {
      case "initiateEmailVerification" -> AuditLog.EventTypes.EMAIL_VERIFICATION_REQUESTED;
      case "confirmEmailVerification" -> AuditLog.EventTypes.EMAIL_VERIFICATION_COMPLETED;
      default -> AuditLog.EventTypes.EMAIL_VERIFICATION;
    };
  }

  /** Create user management details map. */
  private Map<String, Object> createUserManagementDetails(String methodName, long duration) {
    Map<String, Object> details = new HashMap<>();
    details.put("method", methodName);
    details.put("duration_ms", duration);
    return details;
  }

  /** Log user management error. */
  private void logUserManagementError(
      Exception e, HttpServletRequest request, String methodName, long duration) {
    AuditLog auditLog =
        AuditLog.builder()
            .eventType(getUserManagementEventType(methodName))
            .eventCategory(AuditLog.EventCategories.USER_MANAGEMENT)
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .requestUri(request.getRequestURI())
            .requestMethod(request.getMethod())
            .status(AuditLog.Status.FAILURE)
            .errorMessage(e.getMessage())
            .details(createUserManagementDetails(methodName, duration))
            .build();

    auditLogService.logAsync(auditLog);
  }

  /** Log password reset error. */
  private void logPasswordResetError(Exception e, HttpServletRequest request, String methodName) {
    AuditLog auditLog =
        AuditLog.builder()
            .eventType(getPasswordResetEventType(methodName))
            .eventCategory(AuditLog.EventCategories.AUTHENTICATION)
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .requestUri(request.getRequestURI())
            .requestMethod(request.getMethod())
            .status(AuditLog.Status.FAILURE)
            .errorMessage(e.getMessage())
            .build();

    auditLogService.logAsync(auditLog);
  }

  /** Log email verification error. */
  private void logEmailVerificationError(
      Exception e, HttpServletRequest request, String methodName) {
    AuditLog auditLog =
        AuditLog.builder()
            .eventType(getEmailVerificationEventType(methodName))
            .eventCategory(AuditLog.EventCategories.AUTHENTICATION)
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .requestUri(request.getRequestURI())
            .requestMethod(request.getMethod())
            .status(AuditLog.Status.FAILURE)
            .errorMessage(e.getMessage())
            .build();

    auditLogService.logAsync(auditLog);
  }
}
