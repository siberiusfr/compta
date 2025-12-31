package tn.compta.gateway.config;

import java.util.List;

/**
 * Shared public endpoints configuration. Used by SecurityConfig and filters to maintain
 * consistency.
 */
public final class PublicEndpoints {

  private PublicEndpoints() {}

  /** Ant-style patterns for Spring Security. */
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

  /** Exact paths that are public (no wildcard matching needed). */
  public static final List<String> EXACT_PATHS =
      List.of("/actuator/health", "/actuator/info", "/swagger-ui.html");

  /** Path prefixes for filter checks (startsWith matching). */
  public static final List<String> PREFIXES =
      List.of("/auth/", "/swagger-ui/", "/v3/api-docs", "/webjars/", "/fallback/");

  /**
   * Check if a path is a public endpoint. Uses exact match for specific paths, prefix match for
   * others.
   */
  public static boolean isPublic(String path) {
    return EXACT_PATHS.contains(path) || PREFIXES.stream().anyMatch(path::startsWith);
  }
}
