package tn.compta.commons.security.exception;

/**
 * Exception thrown when a JWT token is invalid.
 *
 * <p>This exception should be thrown when:
 *
 * <ul>
 *   <li>Token signature is invalid
 *   <li>Token is malformed
 *   <li>Token has expired
 *   <li>Token claims are invalid
 * </ul>
 *
 * <p>HTTP Status: 401 Unauthorized
 */
public class InvalidTokenException extends UnauthorizedException {

  private static final long serialVersionUID = 1L;

  public InvalidTokenException(String message) {
    super(message);
  }

  public InvalidTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
