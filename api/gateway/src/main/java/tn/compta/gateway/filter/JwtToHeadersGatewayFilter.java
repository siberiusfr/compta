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
 *
 * This filter runs AFTER Spring Security OAuth2 Resource Server has validated the JWT.
 * It extracts user information from authentication context and adds headers
 * that downstream services can use via compta-security-commons.
 *
 * Headers added:
 * - X-User-Id: User's unique identifier
 * - X-User-Email: User's email address
 * - X-User-Roles: Comma-separated list of roles
 * - X-Tenant-Id: Tenant/Company ID (multi-tenancy support)
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

        // Don't add headers for public endpoints (auth service)
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // Get authentication from security context (set by OAuth2 Resource Server)
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> {
                Authentication authentication = securityContext.getAuthentication();

                if (authentication != null && authentication.isAuthenticated()) {
                    return extractHeadersFromAuthentication(authentication, exchange.getRequest());
                }

                return exchange.getRequest(); // No authentication, return original request
            })
            .defaultIfEmpty(exchange.getRequest()) // No security context
            .map(modifiedRequest -> exchange.mutate().request(modifiedRequest).build())
            .flatMap(chain::filter);
    }

    /**
     * Extract user information from authentication and add headers.
     *
     * @param authentication Spring Security authentication object
     * @param request original HTTP request
     * @return modified request with user headers
     */
    private ServerHttpRequest extractHeadersFromAuthentication(Authentication authentication, ServerHttpRequest request) {
        // Extract JWT from authentication principal
        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String userId = jwt.getSubject(); // Standard JWT "sub" claim
            String username = jwt.getClaimAsString("username");
            String email = jwt.getClaimAsString("email");
            List<String> roles = jwt.getClaimAsStringList("roles");
            String tenantId = jwt.getClaimAsString("tenantId");

            // Log for debugging
            log.debug("Adding user headers: userId={}, username={}, email={}, tenantId={}",
                userId, username, email, tenantId);

            // Build modified request with headers
            return request.mutate()
                .header(HEADER_USER_ID, userId != null ? userId : "")
                .header(HEADER_USERNAME, username != null ? username : "")
                .header(HEADER_EMAIL, email != null ? email : "")
                .header(HEADER_ROLES, roles != null ? String.join(",", roles) : "")
                .header(HEADER_TENANT_ID, tenantId != null ? tenantId : "")
                .build();
        }

        // No JWT claims found, return original request
        log.warn("Authentication found but no JWT principal available");
        return request;
    }

    @Override
    public int getOrder() {
        return -1; // Execute after Spring Security filter
    }
}
