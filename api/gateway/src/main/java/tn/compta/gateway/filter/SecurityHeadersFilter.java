package tn.compta.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import tn.compta.gateway.config.ProfileHelper;

/**
 * Global filter to add security headers to all responses.
 *
 * <p>Uses ServerHttpResponseDecorator to ensure headers are added before the response is committed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

  private final ProfileHelper profileHelper;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    ServerHttpResponse originalResponse = exchange.getResponse();
    ServerHttpResponseDecorator decoratedResponse =
        new ServerHttpResponseDecorator(originalResponse) {
          @Override
          public HttpHeaders getHeaders() {
            HttpHeaders headers = super.getHeaders();
            addSecurityHeaders(headers, path);
            return headers;
          }
        };

    return chain.filter(exchange.mutate().response(decoratedResponse).build());
  }

  private void addSecurityHeaders(HttpHeaders headers, String path) {
    // Prevent MIME type sniffing
    headers.addIfAbsent("X-Content-Type-Options", "nosniff");

    // Prevent clickjacking
    headers.addIfAbsent("X-Frame-Options", "DENY");

    // Prevent caching of API responses
    headers.addIfAbsent("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
    headers.addIfAbsent("Pragma", "no-cache");

    // HSTS - Only in production
    if (profileHelper.isProduction()) {
      headers.addIfAbsent(
          "Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
    }

    // Content Security Policy
    if (path.startsWith("/swagger-ui") || path.startsWith("/webjars")) {
      // CSP permissive for Swagger UI only
      headers.addIfAbsent(
          "Content-Security-Policy",
          "default-src 'self'; "
              + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
              + "style-src 'self' 'unsafe-inline'; "
              + "img-src 'self' data: https:; "
              + "font-src 'self' data:;");
    } else {
      // Strict CSP for API endpoints
      headers.addIfAbsent("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none';");
    }

    // Referrer policy
    headers.addIfAbsent("Referrer-Policy", "strict-origin-when-cross-origin");

    // Permissions policy
    headers.addIfAbsent(
        "Permissions-Policy",
        "geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=()");

    // Remove server information
    headers.remove("Server");
    headers.remove("X-Powered-By");
  }

  @Override
  public int getOrder() {
    // Run late to ensure headers are set after other filters
    return Ordered.LOWEST_PRECEDENCE - 1;
  }
}
