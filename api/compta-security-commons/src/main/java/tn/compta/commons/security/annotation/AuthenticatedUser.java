package tn.compta.commons.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the authenticated user details into controller method parameters.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;GetMapping("/profile")
 * public UserProfile getProfile(&#64;AuthenticatedUser AuthenticatedUserDetails user) {
 *     return userService.getProfile(user.getUserId());
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthenticatedUser {}
