package tn.compta.commons.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tn.compta.commons.security.annotation.Public;
import tn.compta.commons.security.annotation.RequirePermission;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.exception.ForbiddenException;
import tn.compta.commons.security.exception.UnauthorizedException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

/**
 * Interceptor that checks if the authenticated user has the required permission(s).
 *
 * <p>This interceptor processes {@link RequirePermission} annotations on controller methods and
 * classes.
 */
@Slf4j
public class PermissionCheckInterceptor implements HandlerInterceptor {

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

    // Get the RequirePermission annotation (method-level takes precedence)
    RequirePermission requirePermission =
        handlerMethod.getMethodAnnotation(RequirePermission.class);
    if (requirePermission == null) {
      requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
    }

    // If no RequirePermission annotation, allow access
    if (requirePermission == null) {
      return true;
    }

    // Check authentication
    AuthenticatedUserDetails user = SecurityContextHolder.getUser();
    if (user == null) {
      throw new UnauthorizedException("User not authenticated");
    }

    // Check permissions
    String[] requiredPermissions = requirePermission.value();
    boolean requireAll = requirePermission.requireAll();

    if (requireAll) {
      if (!user.hasAllPermissions(requiredPermissions)) {
        log.warn(
            "User {} does not have all required permissions: {}",
            user.getUsername(),
            String.join(", ", requiredPermissions));
        throw new ForbiddenException(
            "User does not have all required permissions: "
                + String.join(", ", requiredPermissions));
      }
    } else {
      if (!user.hasAnyPermission(requiredPermissions)) {
        log.warn(
            "User {} does not have any required permission: {}",
            user.getUsername(),
            String.join(", ", requiredPermissions));
        throw new ForbiddenException(
            "User does not have any required permission: "
                + String.join(", ", requiredPermissions));
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
