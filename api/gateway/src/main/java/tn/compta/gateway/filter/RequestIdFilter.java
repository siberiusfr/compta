package tn.compta.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter pour ajouter un Request ID à chaque requête.
 * 
 * Permet de tracer les requêtes à travers tous les microservices.
 * Le Request ID est :
 * - Ajouté dans les headers de la requête (pour les services downstream)
 * - Ajouté dans les headers de la réponse (pour le client)
 * - Ajouté dans le MDC pour les logs
 */
@Slf4j
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

  private static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final String MDC_REQUEST_ID_KEY = "requestId";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // Récupérer le Request ID existant ou en créer un nouveau
    String requestId = exchange.getRequest()
        .getHeaders()
        .getFirst(REQUEST_ID_HEADER);

    if (requestId == null || requestId.isEmpty()) {
      requestId = UUID.randomUUID().toString();
    }

    final String finalRequestId = requestId;

    // Ajouter le Request ID dans les headers de la requête
    ServerHttpRequest modifiedRequest = exchange.getRequest()
        .mutate()
        .header(REQUEST_ID_HEADER, finalRequestId)
        .build();

    // Ajouter le Request ID dans les headers de la réponse
    exchange.getResponse()
        .getHeaders()
        .add(REQUEST_ID_HEADER, finalRequestId);

    // Ajouter le Request ID dans le MDC pour les logs
    return chain.filter(exchange.mutate().request(modifiedRequest).build())
        .contextWrite(ctx -> ctx.put(MDC_REQUEST_ID_KEY, finalRequestId))
        .doFirst(() -> {
          MDC.put(MDC_REQUEST_ID_KEY, finalRequestId);
          log.debug("Request ID added: {}", finalRequestId);
        })
        .doFinally(signal -> {
          MDC.remove(MDC_REQUEST_ID_KEY);
        });
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1; // Juste après le logging filter
  }
}
