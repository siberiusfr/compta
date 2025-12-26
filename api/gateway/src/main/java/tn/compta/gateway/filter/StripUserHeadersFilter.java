package tn.compta.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Security filter that strips user-related headers from incoming requests.
 *
 * This prevents attackers from injecting forged X-User-* headers
 * to impersonate users on downstream services.
 * These headers are only set by JwtToHeadersGatewayFilter after authentication.
 */
@Slf4j
@Component
public class StripUserHeadersFilter implements GlobalFilter, Ordered {

  private static final List<String> HEADERS_TO_STRIP = List.of(
      "X-User-Id",
      "X-User-Username",
      "X-User-Email",
      "X-User-Roles",
      "X-Tenant-Id"
  );

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

    boolean headersStripped = false;
    for (String header : HEADERS_TO_STRIP) {
      if (exchange.getRequest().getHeaders().containsKey(header)) {
        requestBuilder.headers(h -> h.remove(header));
        headersStripped = true;
      }
    }

    if (headersStripped) {
      log.warn("Stripped forged user headers from incoming request: {}",
          exchange.getRequest().getPath());
    }

    ServerHttpRequest cleanRequest = requestBuilder.build();
    return chain.filter(exchange.mutate().request(cleanRequest).build());
  }

  @Override
  public int getOrder() {
    // Run before everything else to strip headers before authentication
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
