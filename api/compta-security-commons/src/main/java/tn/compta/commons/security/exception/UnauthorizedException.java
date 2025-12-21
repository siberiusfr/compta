package tn.compta.commons.security.exception;

/**
 * Exception thrown when a user is not authenticated.
 *
 * <p>This exception should be thrown when:
 *
 * <ul>
 *   <li>No authentication token is provided
 *   <li>Authentication token is invalid
 *   <li>Authentication token has expired
 * </ul>
 *
 * <p>HTTP Status: 401 Unauthorized
 */
public class UnauthorizedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
