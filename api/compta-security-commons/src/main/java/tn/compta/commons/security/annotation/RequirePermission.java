package tn.compta.commons.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check if the authenticated user has one of the required permissions.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;PostMapping("/societes")
 * &#64;RequirePermission({"societe:create", "societe:manage"})
 * public Societe createSociete(&#64;RequestBody CreateSocieteRequest request) {
 *     return societeService.create(request);
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

  /**
   * The required permissions. User must have at least one of these permissions.
   *
   * @return array of permission names
   */
  String[] value();

  /**
   * Whether all permissions are required (AND logic) instead of any permission (OR logic).
   *
   * @return true if all permissions are required, false otherwise (default)
   */
  boolean requireAll() default false;
}
