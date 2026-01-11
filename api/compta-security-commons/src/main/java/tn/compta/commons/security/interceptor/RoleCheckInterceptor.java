package tn.compta.commons.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tn.compta.commons.security.annotation.Public;
import tn.compta.commons.security.annotation.RequireRole;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.exception.ForbiddenException;
import tn.compta.commons.security.exception.UnauthorizedException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

/**
 * Interceptor that checks if the authenticated user has the required role(s).
 *
 * <p>This interceptor processes {@link RequireRole} annotations on controller methods and classes.
 */
@Slf4j
public class RoleCheckInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    // Check if endpoint is public
    if (isPublic(handlerMethod)) {
      return true;
    }

    // Get the RequireRole annotation (method-level takes precedence)
    RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
    if (requireRole == null) {
      requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
    }

    // If no RequireRole annotation, allow access
    if (requireRole == null) {
      return true;
    }

    // Check authentication
    AuthenticatedUserDetails user = SecurityContextHolder.getUser();
    if (user == null) {
      throw new UnauthorizedException("User not authenticated");
    }

    // Check roles
    String[] requiredRoles = requireRole.value();
    boolean requireAll = requireRole.requireAll();

    if (requireAll) {
      if (!user.hasAllRoles(requiredRoles)) {
        log.warn(
            "User {} does not have all required roles: {}",
            user.getUsername(),
            String.join(", ", requiredRoles));
        throw new ForbiddenException(
            "User does not have all required roles: " + String.join(", ", requiredRoles));
      }
    } else {
      if (!user.hasAnyRole(requiredRoles)) {
        log.warn(
            "User {} does not have any required role: {}",
            user.getUsername(),
            String.join(", ", requiredRoles));
        throw new ForbiddenException(
            "User does not have any required role: " + String.join(", ", requiredRoles));
      }
    }

    return true;
  }

  /**
   * Check if the endpoint is marked as public.
   *
   * @param handlerMethod the handler method
   * @return true if public
   */
  private boolean isPublic(HandlerMethod handlerMethod) {
    // Check method-level annotation
    if (handlerMethod.getMethodAnnotation(Public.class) != null) {
      return true;
    }
    // Check class-level annotation
    return handlerMethod.getBeanType().getAnnotation(Public.class) != null;
  }
}
