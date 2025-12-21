package tn.compta.commons.security.context;

import tn.compta.commons.security.exception.UnauthorizedException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

/**
 * Holder for the security context using ThreadLocal storage.
 *
 * <p>This class provides static methods to access the current security context throughout the
 * request lifecycle.
 *
 * <p>Usage example:
 *
 * <pre>
 * SecurityContext context = SecurityContextHolder.getContext();
 * AuthenticatedUserDetails user = SecurityContextHolder.getUser();
 * Long userId = SecurityContextHolder.getUserId();
 * </pre>
 */
public class SecurityContextHolder {

  private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();

  private SecurityContextHolder() {
    // Utility class
  }

  /**
   * Set the security context for the current thread.
   *
   * @param context the security context
   */
  public static void setContext(SecurityContext context) {
    contextHolder.set(context);
  }

  /**
   * Get the security context for the current thread.
   *
   * @return the security context, or null if not set
   */
  public static SecurityContext getContext() {
    return contextHolder.get();
  }

  /**
   * Get the authenticated user from the current context.
   *
   * @return the authenticated user, or null if not authenticated
   */
  public static AuthenticatedUserDetails getUser() {
    SecurityContext context = getContext();
    return context != null ? context.getUser() : null;
  }

  /**
   * Get the authenticated user from the current context, throwing exception if not authenticated.
   *
   * @return the authenticated user
   * @throws UnauthorizedException if not authenticated
   */
  public static AuthenticatedUserDetails getRequiredUser() {
    AuthenticatedUserDetails user = getUser();
    if (user == null) {
      throw new UnauthorizedException("User not authenticated");
    }
    return user;
  }

  /**
   * Get the user ID from the current context.
   *
   * @return the user ID, or null if not authenticated
   */
  public static Long getUserId() {
    AuthenticatedUserDetails user = getUser();
    return user != null ? user.getUserId() : null;
  }

  /**
   * Get the user ID from the current context, throwing exception if not authenticated.
   *
   * @return the user ID
   * @throws UnauthorizedException if not authenticated
   */
  public static Long getRequiredUserId() {
    Long userId = getUserId();
    if (userId == null) {
      throw new UnauthorizedException("User not authenticated");
    }
    return userId;
  }

  /**
   * Get the username from the current context.
   *
   * @return the username, or null if not authenticated
   */
  public static String getUsername() {
    AuthenticatedUserDetails user = getUser();
    return user != null ? user.getUsername() : null;
  }

  /**
   * Check if the current context is authenticated.
   *
   * @return true if authenticated
   */
  public static boolean isAuthenticated() {
    SecurityContext context = getContext();
    return context != null && context.isAuthenticated();
  }

  /**
   * Clear the security context for the current thread.
   *
   * <p>This should be called at the end of each request to prevent memory leaks.
   */
  public static void clear() {
    contextHolder.remove();
  }
}
