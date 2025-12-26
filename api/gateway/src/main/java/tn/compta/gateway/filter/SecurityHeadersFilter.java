package tn.compta.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter to add security headers to all responses.
 *
 * Security headers:
 * - X-Content-Type-Options: Prevents MIME sniffing
 * - X-Frame-Options: Prevents clickjacking
 * - X-XSS-Protection: XSS filter for older browsers
 * - Strict-Transport-Security: Forces HTTPS (production only)
 * - Content-Security-Policy: Restricts resource loading
 * - Referrer-Policy: Controls referrer information
 * - Permissions-Policy: Feature policy
 */
@Slf4j
@Component
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return chain.filter(exchange).then(Mono.fromRunnable(() -> {
      HttpHeaders headers = exchange.getResponse().getHeaders();

      // ✅ Prevent MIME type sniffing
      headers.add("X-Content-Type-Options", "nosniff");

      // ✅ Prevent clickjacking
      headers.add("X-Frame-Options", "DENY");

      // ✅ XSS protection for older browsers
      headers.add("X-XSS-Protection", "1; mode=block");

      // ✅ HSTS - Only in production
      if (isProduction()) {
        headers.add("Strict-Transport-Security",
            "max-age=31536000; includeSubDomains; preload");
        log.debug("Added HSTS header for production environment");
      }

      // ✅ Content Security Policy
      headers.add("Content-Security-Policy",
          "default-src 'self'; " +
              "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
              "style-src 'self' 'unsafe-inline'; " +
              "img-src 'self' data: https:; " +
              "font-src 'self' data:; " +
              "connect-src 'self'; " +
              "frame-ancestors 'none';");

      // ✅ Referrer policy
      headers.add("Referrer-Policy", "strict-origin-when-cross-origin");

      // ✅ Permissions policy (Feature Policy replacement)
      headers.add("Permissions-Policy",
          "geolocation=(), " +
              "microphone=(), " +
              "camera=(), " +
              "payment=(), " +
              "usb=(), " +
              "magnetometer=()");

      // ✅ Remove server information
      headers.remove("Server");
      headers.remove("X-Powered-By");
    }));
  }

  /**
   * Check if running in production environment.
   */
  private boolean isProduction() {
    return "prod".equalsIgnoreCase(activeProfile) ||
        "production".equalsIgnoreCase(activeProfile);
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
