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
 * - Strict-Transport-Security: Forces HTTPS (production only)
 * - Content-Security-Policy: Restricts resource loading (strict for API)
 * - Cache-Control: Prevents caching of sensitive responses
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

      // ✅ Prevent caching of API responses
      headers.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
      headers.add("Pragma", "no-cache");

      // ✅ HSTS - Only in production
      if (isProduction()) {
        headers.add("Strict-Transport-Security",
            "max-age=31536000; includeSubDomains; preload");
        log.debug("Added HSTS header for production environment");
      }

      // ✅ Content Security Policy - Strict pour une API Gateway pure
      // Si vous servez aussi du contenu web (Swagger UI), utilisez la version commentée ci-dessous
      String path = exchange.getRequest().getPath().value();
      
      if (path.startsWith("/swagger-ui") || path.startsWith("/webjars")) {
        // CSP permissive pour Swagger UI uniquement
        headers.add("Content-Security-Policy",
            "default-src 'self'; "
                + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
                + "style-src 'self' 'unsafe-inline'; "
                + "img-src 'self' data: https:; "
                + "font-src 'self' data:;");
      } else {
        // CSP strict pour les endpoints API
        headers.add("Content-Security-Policy",
            "default-src 'none'; "
                + "frame-ancestors 'none';");
      }

      // ✅ Referrer policy
      headers.add("Referrer-Policy", "strict-origin-when-cross-origin");

      // ✅ Permissions policy (Feature Policy replacement)
      headers.add("Permissions-Policy",
          "geolocation=(), "
              + "microphone=(), "
              + "camera=(), "
              + "payment=(), "
              + "usb=(), "
              + "magnetometer=()");

      // ✅ Remove server information
      headers.remove("Server");
      headers.remove("X-Powered-By");
    }));
  }

  /**
   * Check if running in production environment.
   * Handles multiple profiles (e.g., "prod,monitoring").
   */
  private boolean isProduction() {
    if (activeProfile == null) {
      return false;
    }
    String lowerProfile = activeProfile.toLowerCase();
    return lowerProfile.contains("prod");
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
