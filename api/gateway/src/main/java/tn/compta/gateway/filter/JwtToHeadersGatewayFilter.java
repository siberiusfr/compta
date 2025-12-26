package tn.compta.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tn.compta.gateway.config.PublicEndpoints;

import java.util.List;

/**
 * Gateway filter that extracts JWT claims and adds them as HTTP headers.
 */
@Slf4j
@Component
public class JwtToHeadersGatewayFilter implements GlobalFilter, Ordered {

  private static final String HEADER_USER_ID = "X-User-Id";
  private static final String HEADER_USERNAME = "X-User-Username";
  private static final String HEADER_EMAIL = "X-User-Email";
  private static final String HEADER_ROLES = "X-User-Roles";
  private static final String HEADER_TENANT_ID = "X-Tenant-Id";

  private static final int MAX_ROLES_COUNT = 50;
  private static final int MAX_HEADER_VALUE_LENGTH = 1024;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().toString();

    // Don't add headers for public endpoints
    if (isPublicEndpoint(path)) {
      return chain.filter(exchange);
    }

    return ReactiveSecurityContextHolder.getContext()
        .map(securityContext -> {
          Authentication authentication = securityContext.getAuthentication();

          if (authentication != null && authentication.isAuthenticated()) {
            return extractHeadersFromAuthentication(authentication, exchange.getRequest());
          }

          return exchange.getRequest();
        })
        .defaultIfEmpty(exchange.getRequest())
        .map(modifiedRequest -> exchange.mutate().request(modifiedRequest).build())
        .flatMap(chain::filter);
  }

  /**
   * Check if endpoint is public (doesn't need user headers).
   */
  private boolean isPublicEndpoint(String path) {
    return PublicEndpoints.isPublic(path);
  }

  /**
   * Extract user information from JWT and add headers.
   * ✅ Only add headers if values are present (no empty strings).
   */
  private ServerHttpRequest extractHeadersFromAuthentication(Authentication authentication, ServerHttpRequest request) {
    Object principal = authentication.getPrincipal();

    if (!(principal instanceof Jwt jwt)) {
      log.warn("Authentication found but no JWT principal available");
      return request;
    }

    // Extract and validate claims
    String userId = truncateIfNeeded(jwt.getSubject());
    String username = truncateIfNeeded(jwt.getClaimAsString("username"));
    String email = truncateIfNeeded(jwt.getClaimAsString("email"));
    List<String> roles = validateRoles(jwt.getClaimAsStringList("roles"));
    String tenantId = truncateIfNeeded(jwt.getClaimAsString("tenantId"));

    // Build request with headers (only if values exist)
    ServerHttpRequest.Builder builder = request.mutate();

    if (isValidHeaderValue(userId)) {
      builder.header(HEADER_USER_ID, userId);
    }

    if (isValidHeaderValue(username)) {
      builder.header(HEADER_USERNAME, username);
    }

    if (isValidHeaderValue(email)) {
      builder.header(HEADER_EMAIL, email);
    }

    if (roles != null && !roles.isEmpty()) {
      String rolesValue = String.join(",", roles);
      if (rolesValue.length() <= MAX_HEADER_VALUE_LENGTH) {
        builder.header(HEADER_ROLES, rolesValue);
      } else {
        log.warn("Roles header value too long, truncating");
        builder.header(HEADER_ROLES, rolesValue.substring(0, MAX_HEADER_VALUE_LENGTH));
      }
    }

    if (isValidHeaderValue(tenantId)) {
      builder.header(HEADER_TENANT_ID, tenantId);
    }

    // ✅ Log avec masquage de l'email pour GDPR
    log.debug("Added user headers: userId={}, username={}, email={}, tenantId={}, roles={}",
        userId, username, maskEmail(email), tenantId, roles);

    return builder.build();
  }

  /**
   * Masque partiellement l'email pour la protection des données personnelles (GDPR).
   * Exemple: john.doe@example.com -> j***@example.com
   */
  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }

    String[] parts = email.split("@", 2);
    if (parts.length != 2 || parts[1].isEmpty()) {
      return "***";
    }

    String localPart = parts[0];
    if (localPart.isEmpty()) {
      return "***@" + parts[1];
    }

    return localPart.charAt(0) + "***@" + parts[1];
  }

  private String truncateIfNeeded(String value) {
    if (value == null) {
      return null;
    }
    if (value.length() > MAX_HEADER_VALUE_LENGTH) {
      log.warn("Claim value too long, truncating from {} to {} chars", value.length(), MAX_HEADER_VALUE_LENGTH);
      return value.substring(0, MAX_HEADER_VALUE_LENGTH);
    }
    return value;
  }

  private boolean isValidHeaderValue(String value) {
    return value != null && !value.isEmpty();
  }

  private List<String> validateRoles(List<String> roles) {
    if (roles == null) {
      return null;
    }
    if (roles.size() > MAX_ROLES_COUNT) {
      log.warn("Too many roles ({}), limiting to {}", roles.size(), MAX_ROLES_COUNT);
      return roles.subList(0, MAX_ROLES_COUNT);
    }
    return roles;
  }

  @Override
  public int getOrder() {
    return 0; // After security context is established
  }
}
