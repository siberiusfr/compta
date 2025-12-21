package tn.compta.commons.security.exception;

import java.io.Serial;

/**
 * Exception thrown when an authenticated user does not have sufficient permissions.
 *
 * <p>This exception should be thrown when:
 *
 * <ul>
 *   <li>User is authenticated but lacks required role(s)
 *   <li>User is authenticated but lacks required permission(s)
 *   <li>User is authenticated but not authorized to access specific resource
 * </ul>
 *
 * <p>HTTP Status: 403 Forbidden
 */
public class ForbiddenException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }
}
