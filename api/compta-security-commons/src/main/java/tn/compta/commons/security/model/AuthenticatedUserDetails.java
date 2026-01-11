package tn.compta.commons.security.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the authenticated user details extracted from JWT or gateway headers.
 *
 * <p>This class contains the user information that is passed through the authentication chain,
 * typically from the API Gateway to downstream microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUserDetails implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** User's unique identifier */
  private Long userId;

  /** User's username */
  private String username;

  /** User's email address */
  private String email;

  /** User's roles (e.g., ADMIN, COMPTABLE, SOCIETE, EMPLOYEE) */
  private List<String> roles;

  /** User's permissions (e.g., user:create, societe:read) */
  private List<UserPermission> permissions;

  /** Company IDs the user is associated with */
  private List<Long> societeIds;

  /** ID of the primary company (for employees) */
  private Long primarySocieteId;

  /** Whether the user account is active */
  private boolean active;

  /** Whether the user account is locked */
  private boolean locked;

  /**
   * Check if the user has a specific role.
   *
   * @param role the role to check
   * @return true if the user has the role
   */
  public boolean hasRole(String role) {
    return roles != null && roles.contains(role);
  }

  /**
   * Check if the user has any of the specified roles.
   *
   * @param roles the roles to check
   * @return true if the user has at least one of the roles
   */
  public boolean hasAnyRole(String... roles) {
    if (this.roles == null || roles == null) {
      return false;
    }
    for (String role : roles) {
      if (this.roles.contains(role)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if the user has all of the specified roles.
   *
   * @param roles the roles to check
   * @return true if the user has all of the roles
   */
  public boolean hasAllRoles(String... roles) {
    if (this.roles == null || roles == null) {
      return false;
    }
    for (String role : roles) {
      if (!this.roles.contains(role)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if the user has a specific permission.
   *
   * @param permission the permission to check (e.g., "user:create")
   * @return true if the user has the permission
   */
  public boolean hasPermission(String permission) {
    if (permissions == null) {
      return false;
    }
    return permissions.stream()
        .anyMatch(p -> p.getPermission().equals(permission) || matchesWildcard(p, permission));
  }

  /**
   * Check if the user has any of the specified permissions.
   *
   * @param permissions the permissions to check
   * @return true if the user has at least one of the permissions
   */
  public boolean hasAnyPermission(String... permissions) {
    if (permissions == null) {
      return false;
    }
    for (String permission : permissions) {
      if (hasPermission(permission)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if the user has all of the specified permissions.
   *
   * @param permissions the permissions to check
   * @return true if the user has all of the permissions
   */
  public boolean hasAllPermissions(String... permissions) {
    if (permissions == null) {
      return false;
    }
    for (String permission : permissions) {
      if (!hasPermission(permission)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if a permission matches using wildcard logic.
   *
   * @param userPermission the user's permission
   * @param requestedPermission the requested permission
   * @return true if the permission matches
   */
  private boolean matchesWildcard(UserPermission userPermission, String requestedPermission) {
    String perm = userPermission.getPermission();
    if (perm.endsWith("*")) {
      String prefix = perm.substring(0, perm.length() - 1);
      return requestedPermission.startsWith(prefix);
    }
    return false;
  }
}
