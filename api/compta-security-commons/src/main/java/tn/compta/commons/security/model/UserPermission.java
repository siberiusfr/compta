package tn.compta.commons.security.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user permission.
 *
 * <p>Permissions follow the pattern: {@code resource:action} or {@code resource:*} for wildcard
 * permissions.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>user:create - Can create users
 *   <li>user:read - Can read user data
 *   <li>user:* - Can perform any action on users
 *   <li>societe:manage - Can manage companies
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** The permission string (e.g., "user:create", "societe:read") */
  private String permission;

  /** Optional: The resource this permission applies to */
  private String resource;

  /** Optional: The action this permission allows */
  private String action;

  /**
   * Create a permission from a string.
   *
   * @param permission the permission string (e.g., "user:create")
   * @return the UserPermission object
   */
  public static UserPermission of(String permission) {
    String[] parts = permission.split(":", 2);
    return UserPermission.builder()
        .permission(permission)
        .resource(parts.length > 0 ? parts[0] : null)
        .action(parts.length > 1 ? parts[1] : null)
        .build();
  }
}
