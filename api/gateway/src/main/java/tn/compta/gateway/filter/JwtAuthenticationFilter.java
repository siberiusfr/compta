package tn.compta.gateway.filter;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tn.compta.gateway.util.JwtUtils;

import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip authentication for health checks and public endpoints
            String path = request.getURI().getPath();
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate token
                if (!jwtUtils.validateToken(token)) {
                    log.warn("Invalid JWT token for path: {}", path);
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract claims from token
                Claims claims = jwtUtils.extractClaims(token);

                // Extract user information
                String userId = claims.get("userId", String.class);
                String email = claims.get("email", String.class);
                String companyId = claims.get("companyId", String.class);

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                @SuppressWarnings("unchecked")
                List<String> permissions = claims.get("permissions", List.class);

                // Build modified request with injected headers
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId != null ? userId : "")
                        .header("X-User-Email", email != null ? email : "")
                        .header("X-Company-Id", companyId != null ? companyId : "")
                        .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                        .header("X-User-Permissions", permissions != null ? String.join(",", permissions) : "")
                        .build();

                log.debug("JWT validated for user: {} (company: {})", email, companyId);

                // Continue with modified request
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("Error processing JWT token: {}", e.getMessage());
                return onError(exchange, "Error processing token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path) {
        return path.equals("/actuator/health") ||
               path.equals("/actuator/info") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/auth/login") ||
               path.startsWith("/auth/register") ||
               path.equals("/login") ||
               path.equals("/register");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorResponse = "{\"error\":\"%s\",\"status\":%d}".formatted(message, httpStatus.value());

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    public static class Config {
        // Configuration properties if needed
    }
}
