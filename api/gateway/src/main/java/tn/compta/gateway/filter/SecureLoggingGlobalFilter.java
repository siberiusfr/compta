package tn.compta.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global logging filter for Gateway requests and responses.
 *
 * Security features:
 * - Masks sensitive headers (Authorization, Cookie, etc.)
 * - Logs request/response details
 * - Performance monitoring
 */
@Slf4j
@Component
public class SecureLoggingGlobalFilter implements GlobalFilter, Ordered {

  private static final List<String> SENSITIVE_HEADERS = List.of(
      "authorization",
      "cookie",
      "set-cookie",
      "x-csrf-token",
      "x-api-key"
  );

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    long startTime = System.currentTimeMillis();

    // Log request
    logRequest(request);

    return chain.filter(exchange).doFinally(signal -> {
      // Log response
      long duration = System.currentTimeMillis() - startTime;
      logResponse(request, exchange, duration);
    });
  }

  /**
   * Log incoming request with masked sensitive headers.
   */
  private void logRequest(ServerHttpRequest request) {
    if (log.isDebugEnabled()) {
      String maskedHeaders = maskSensitiveHeaders(request.getHeaders());

      log.debug("🔵 Incoming Request: {} {} | Headers: {}",
          request.getMethod(),
          request.getURI(),
          maskedHeaders);
    } else {
      log.info("🔵 Request: {} {}", request.getMethod(), request.getURI());
    }
  }

  /**
   * Log response with status and duration.
   */
  private void logResponse(ServerHttpRequest request, ServerWebExchange exchange, long duration) {
    var statusCode = exchange.getResponse().getStatusCode();

    if (statusCode != null) {
      String statusEmoji = getStatusEmoji(statusCode.value());

      log.info("{} Response: {} {} | Status: {} | Duration: {}ms",
          statusEmoji,
          request.getMethod(),
          request.getURI(),
          statusCode.value(),
          duration);

      // Warn on slow requests
      if (duration > 5000) {
        log.warn("⚠️ Slow request detected: {} {} took {}ms",
            request.getMethod(),
            request.getURI(),
            duration);
      }
    }
  }

  /**
   * Mask sensitive headers for security.
   */
  private String maskSensitiveHeaders(HttpHeaders headers) {
    return headers.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> isSensitiveHeader(entry.getKey())
                ? List.of("***MASKED***")
                : entry.getValue()
        ))
        .toString();
  }

  /**
   * Check if header is sensitive.
   */
  private boolean isSensitiveHeader(String headerName) {
    return SENSITIVE_HEADERS.stream()
        .anyMatch(sensitive -> sensitive.equalsIgnoreCase(headerName));
  }

  /**
   * Get emoji based on HTTP status code.
   */
  private String getStatusEmoji(int statusCode) {
    if (statusCode >= 200 && statusCode < 300) {
      return "✅"; // Success
    } else if (statusCode >= 300 && statusCode < 400) {
      return "↩️"; // Redirect
    } else if (statusCode >= 400 && statusCode < 500) {
      return "⚠️"; // Client error
    } else {
      return "❌"; // Server error
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
