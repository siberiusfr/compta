package tn.compta.commons.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Compta Security Commons.
 *
 * <p>These properties can be configured in application.yml:
 *
 * <pre>
 * compta:
 *   security:
 *     enabled: true
 *     public-paths: /actuator/**,/v3/api-docs/**,/swagger-ui/**
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "compta.security")
public class SecurityProperties {

  /**
   * Enable or disable the Compta Security Commons auto-configuration.
   *
   * <p>When enabled, the module will automatically register:
   *
   * <ul>
   *   <li>GatewayAuthenticationFilter - Reads X-User-* headers from API Gateway
   *   <li>RoleCheckInterceptor - Verifies @RequireRole annotations
   *   <li>PermissionCheckInterceptor - Verifies @RequirePermission annotations
   *   <li>SecurityAspect - AOP security for service layer
   *   <li>AuthenticatedUserArgumentResolver - Injects @AuthenticatedUser
   *   <li>SecurityContextArgumentResolver - Injects SecurityContext
   * </ul>
   *
   * <p>Default: true
   */
  private boolean enabled = true;

  /**
   * Comma-separated list of URL patterns that do not require authentication.
   *
   * <p>Supports wildcard patterns (e.g., /actuator/**, /public/**).
   *
   * <p>These paths will bypass the GatewayAuthenticationFilter and can be accessed without
   * authentication headers.
   *
   * <p>Default: "/actuator/**,/v3/api-docs/**,/swagger-ui/**"
   */
  private String publicPaths = "/actuator/**,/v3/api-docs/**,/swagger-ui/**";
}
