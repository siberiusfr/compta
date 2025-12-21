package tn.compta.commons.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark an endpoint as publicly accessible without authentication.
 *
 * <p>This annotation can be applied at the method or class level. When applied at the class level,
 * all methods in the class are considered public unless overridden at the method level.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;PostMapping("/auth/login")
 * &#64;Public
 * public AuthResponse login(&#64;RequestBody LoginRequest request) {
 *     return authService.login(request);
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Public {}
