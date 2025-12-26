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
    return path.startsWith("/auth/") ||
        path.startsWith("/actuator/") ||
        path.startsWith("/swagger-ui") ||
        path.startsWith("/v3/api-docs");
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

    // Extract claims
    String userId = jwt.getSubject();
    String username = jwt.getClaimAsString("username");
    String email = jwt.getClaimAsString("email");
    List<String> roles = jwt.getClaimAsStringList("roles");
    String tenantId = jwt.getClaimAsString("tenantId");

    // Build request with headers (only if values exist)
    ServerHttpRequest.Builder builder = request.mutate();

    if (userId != null && !userId.isEmpty()) {
      builder.header(HEADER_USER_ID, userId);
    }

    if (username != null && !username.isEmpty()) {
      builder.header(HEADER_USERNAME, username);
    }

    if (email != null && !email.isEmpty()) {
      builder.header(HEADER_EMAIL, email);
    }

    if (roles != null && !roles.isEmpty()) {
      builder.header(HEADER_ROLES, String.join(",", roles));
    }

    if (tenantId != null && !tenantId.isEmpty()) {
      builder.header(HEADER_TENANT_ID, tenantId);
    }

    log.debug("Added user headers: userId={}, username={}, email={}, tenantId={}, roles={}",
        userId, username, email, tenantId, roles);

    return builder.build();
  }

  @Override
  public int getOrder() {
    return 0; // After security context is established
  }
}
