package tn.compta.commons.security.context;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

/**
 * Security context that holds the authenticated user details and request metadata.
 *
 * <p>This context is stored in a thread-local variable and is accessible throughout the request
 * lifecycle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityContext implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** The authenticated user details */
  private AuthenticatedUserDetails user;

  /** Request ID for tracing */
  private String requestId;

  /** Whether the request is authenticated */
  private boolean authenticated;

  /**
   * Check if the context has an authenticated user.
   *
   * @return true if authenticated
   */
  public boolean isAuthenticated() {
    return authenticated && user != null;
  }

  /**
   * Get the user ID from the context.
   *
   * @return the user ID, or null if not authenticated
   */
  public Long getUserId() {
    return user != null ? user.getUserId() : null;
  }

  /**
   * Get the username from the context.
   *
   * @return the username, or null if not authenticated
   */
  public String getUsername() {
    return user != null ? user.getUsername() : null;
  }
}
