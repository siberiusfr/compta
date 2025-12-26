package tn.compta.gateway.config;

import java.util.List;

/**
 * Shared public endpoints configuration.
 * Used by SecurityConfig and filters to maintain consistency.
 */
public final class PublicEndpoints {

  private PublicEndpoints() {
  }

  /**
   * Ant-style patterns for Spring Security.
   */
  public static final String[] PATTERNS = {
      "/auth/**",
      "/actuator/health",
      "/actuator/info",
      "/swagger-ui.html",
      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/webjars/**",
      "/fallback/**"
  };

  /**
   * Path prefixes for filter checks.
   */
  public static final List<String> PREFIXES = List.of(
      "/auth/",
      "/actuator/",
      "/swagger-ui",
      "/v3/api-docs",
      "/webjars/",
      "/fallback/"
  );

  /**
   * Check if a path is a public endpoint.
   */
  public static boolean isPublic(String path) {
    return PREFIXES.stream().anyMatch(path::startsWith);
  }
}
