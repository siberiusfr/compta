package tn.compta.commons.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check if the authenticated user has one of the required roles.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;GetMapping("/admin/users")
 * &#64;RequireRole({"ADMIN", "COMPTABLE"})
 * public List&lt;User&gt; listUsers() {
 *     return userService.findAll();
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

  /**
   * The required roles. User must have at least one of these roles.
   *
   * @return array of role names
   */
  String[] value();

  /**
   * Whether all roles are required (AND logic) instead of any role (OR logic).
   *
   * @return true if all roles are required, false otherwise (default)
   */
  boolean requireAll() default false;
}
