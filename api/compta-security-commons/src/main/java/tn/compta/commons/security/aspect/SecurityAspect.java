package tn.compta.commons.security.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tn.compta.commons.security.annotation.Public;
import tn.compta.commons.security.annotation.RequirePermission;
import tn.compta.commons.security.annotation.RequireRole;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.exception.ForbiddenException;
import tn.compta.commons.security.exception.UnauthorizedException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

/**
 * Aspect that provides additional security checks for service layer methods.
 *
 * <p>This aspect can be used in addition to the interceptors to provide security checks at the
 * service layer.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;Service
 * public class UserService {
 *
 *   &#64;RequireRole("ADMIN")
 *   public void deleteUser(Long userId) {
 *     // Only admins can delete users
 *   }
 * }
 * </pre>
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class SecurityAspect {

  /**
   * Around advice for methods annotated with @RequireRole.
   *
   * @param joinPoint the join point
   * @param requireRole the annotation
   * @return the method result
   * @throws Throwable if an error occurs
   */
  @Around("@annotation(requireRole)")
  public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();

    // Check if method is also marked as @Public
    if (signature.getMethod().isAnnotationPresent(Public.class)) {
      return joinPoint.proceed();
    }

    // Check authentication
    AuthenticatedUserDetails user = SecurityContextHolder.getUser();
    if (user == null) {
      log.warn("Unauthorized access attempt to method: {}", signature.getName());
      throw new UnauthorizedException("User not authenticated");
    }

    // Check roles
    String[] requiredRoles = requireRole.value();
    boolean requireAll = requireRole.requireAll();

    if (requireAll) {
      if (!user.hasAllRoles(requiredRoles)) {
        log.warn(
            "User {} does not have all required roles {} for method: {}",
            user.getUsername(),
            String.join(", ", requiredRoles),
            signature.getName());
        throw new ForbiddenException(
            "User does not have all required roles: " + String.join(", ", requiredRoles));
      }
    } else {
      if (!user.hasAnyRole(requiredRoles)) {
        log.warn(
            "User {} does not have any required role {} for method: {}",
            user.getUsername(),
            String.join(", ", requiredRoles),
            signature.getName());
        throw new ForbiddenException(
            "User does not have any required role: " + String.join(", ", requiredRoles));
      }
    }

    return joinPoint.proceed();
  }

  /**
   * Around advice for methods annotated with @RequirePermission.
   *
   * @param joinPoint the join point
   * @param requirePermission the annotation
   * @return the method result
   * @throws Throwable if an error occurs
   */
  @Around("@annotation(requirePermission)")
  public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission)
      throws Throwable {

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();

    // Check if method is also marked as @Public
    if (signature.getMethod().isAnnotationPresent(Public.class)) {
      return joinPoint.proceed();
    }

    // Check authentication
    AuthenticatedUserDetails user = SecurityContextHolder.getUser();
    if (user == null) {
      log.warn("Unauthorized access attempt to method: {}", signature.getName());
      throw new UnauthorizedException("User not authenticated");
    }

    // Check permissions
    String[] requiredPermissions = requirePermission.value();
    boolean requireAll = requirePermission.requireAll();

    if (requireAll) {
      if (!user.hasAllPermissions(requiredPermissions)) {
        log.warn(
            "User {} does not have all required permissions {} for method: {}",
            user.getUsername(),
            String.join(", ", requiredPermissions),
            signature.getName());
        throw new ForbiddenException(
            "User does not have all required permissions: "
                + String.join(", ", requiredPermissions));
      }
    } else {
      if (!user.hasAnyPermission(requiredPermissions)) {
        log.warn(
            "User {} does not have any required permission {} for method: {}",
            user.getUsername(),
            String.join(", ", requiredPermissions),
            signature.getName());
        throw new ForbiddenException(
            "User does not have any required permission: "
                + String.join(", ", requiredPermissions));
      }
    }

    return joinPoint.proceed();
  }
}
