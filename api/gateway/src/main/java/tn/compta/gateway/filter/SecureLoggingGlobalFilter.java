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

    logRequest(request);

    return chain.filter(exchange).doFinally(signal -> {
      long duration = System.currentTimeMillis() - startTime;
      logResponse(request, exchange, duration);
    });
  }

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

      if (duration > 5000) {
        log.warn("⚠️ Slow request detected: {} {} took {}ms",
            request.getMethod(),
            request.getURI(),
            duration);
      }
    }
  }

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

  private boolean isSensitiveHeader(String headerName) {
    return SENSITIVE_HEADERS.stream()
        .anyMatch(sensitive -> sensitive.equalsIgnoreCase(headerName));
  }

  private String getStatusEmoji(int statusCode) {
    if (statusCode >= 200 && statusCode < 300) {
      return "✅";
    } else if (statusCode >= 300 && statusCode < 400) {
      return "↩️";
    } else if (statusCode >= 400 && statusCode < 500) {
      return "⚠️";
    } else {
      return "❌";
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
