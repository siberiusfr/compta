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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SecureLoggingGlobalFilter implements GlobalFilter, Ordered {

  private static final Set<String> SENSITIVE_HEADERS = Set.of(
      "authorization",
      "cookie",
      "set-cookie",
      "x-csrf-token",
      "x-api-key",
      "x-auth-token",
      "proxy-authorization"
  );

  private static final Set<String> SENSITIVE_QUERY_PARAMS = Set.of(
      "token",
      "access_token",
      "refresh_token",
      "api_key",
      "apikey",
      "password",
      "secret",
      "key"
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
    String safePath = maskQueryParams(request.getURI());
    if (log.isDebugEnabled()) {
      String maskedHeaders = maskSensitiveHeaders(request.getHeaders());
      log.debug("Incoming request: {} {} | Headers: {}",
          request.getMethod(),
          safePath,
          maskedHeaders);
    } else {
      log.info("Request: {} {}", request.getMethod(), safePath);
    }
  }

  private void logResponse(ServerHttpRequest request, ServerWebExchange exchange, long duration) {
    var statusCode = exchange.getResponse().getStatusCode();

    if (statusCode != null) {
      String safePath = maskQueryParams(request.getURI());
      String statusLabel = getStatusLabel(statusCode.value());

      log.info("{} response: {} {} | Status: {} | Duration: {}ms",
          statusLabel,
          request.getMethod(),
          safePath,
          statusCode.value(),
          duration);

      if (duration > 5000) {
        log.warn("Slow request detected: {} {} took {}ms",
            request.getMethod(),
            safePath,
            duration);
      }
    }
  }

  /**
   * Masks sensitive query parameters in the URI.
   */
  private String maskQueryParams(URI uri) {
    String query = uri.getQuery();
    if (query == null || query.isEmpty()) {
      return uri.getPath();
    }

    String maskedQuery = java.util.Arrays.stream(query.split("&"))
        .map(param -> {
          String[] parts = param.split("=", 2);
          if (parts.length == 2 && isSensitiveParam(parts[0])) {
            return parts[0] + "=***";
          }
          return param;
        })
        .collect(Collectors.joining("&"));

    return uri.getPath() + "?" + maskedQuery;
  }

  private boolean isSensitiveParam(String paramName) {
    return SENSITIVE_QUERY_PARAMS.stream()
        .anyMatch(sensitive -> paramName.toLowerCase().contains(sensitive));
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

  private String getStatusLabel(int statusCode) {
    if (statusCode >= 200 && statusCode < 300) {
      return "OK";
    } else if (statusCode >= 300 && statusCode < 400) {
      return "REDIRECT";
    } else if (statusCode >= 400 && statusCode < 500) {
      return "CLIENT_ERROR";
    } else {
      return "SERVER_ERROR";
    }
  }

  @Override
  public int getOrder() {
    // Run after StripUserHeadersFilter (HIGHEST_PRECEDENCE)
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }
}
