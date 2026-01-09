package tn.compta.gateway.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import tn.compta.gateway.config.OAuth2TokenValidator;
import tn.compta.gateway.config.PublicEndpoints;

/**
 * Filtre pour la validation des tokens JWT RSA issus par le serveur OAuth2. Ce filtre valide les
 * tokens avant de les transmettre aux services backend.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2TokenValidationFilter implements GatewayFilter, Ordered {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final OAuth2TokenValidator tokenValidator;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    // Skip validation for public endpoints and health checks
    if (shouldSkipValidation(path)) {
      log.debug("Skipping OAuth2 token validation for path: {}", path);
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      log.warn("Missing or invalid Authorization header for path: {}", path);
      return sendErrorResponse(
          exchange,
          HttpStatus.UNAUTHORIZED,
          "{\"error\":\"missing_or_invalid_authorization_header\",\"message\":\"Authorization header is required\"}");
    }

    String token = authHeader.substring(BEARER_PREFIX.length()).trim();

    if (token.isEmpty()) {
      log.warn("Empty token in Authorization header for path: {}", path);
      return sendErrorResponse(
          exchange,
          HttpStatus.UNAUTHORIZED,
          "{\"error\":\"empty_token\",\"message\":\"Token cannot be empty\"}");
    }

    // Validate the JWT token using OAuth2TokenValidator
    boolean isValid = tokenValidator.validateTokenSilent(token);

    if (!isValid) {
      log.warn("Invalid OAuth2 token for path: {}", path);
      return sendErrorResponse(
          exchange,
          HttpStatus.UNAUTHORIZED,
          "{\"error\":\"invalid_token\",\"message\":\"The provided token is invalid or expired\"}");
    }

    log.debug("OAuth2 token validated successfully for path: {}", path);

    // Add validation flag to exchange attributes for downstream filters
    exchange.getAttributes().put("oauth2.token.validated", true);

    return chain.filter(exchange);
  }

  /** Send an error response */
  private Mono<Void> sendErrorResponse(
      ServerWebExchange exchange, HttpStatus status, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    DataBufferFactory bufferFactory = response.bufferFactory();
    DataBuffer buffer = bufferFactory.wrap(message.getBytes(StandardCharsets.UTF_8));

    return response.writeWith(Mono.just(buffer));
  }

  /** Détérmine si la validation doit être sautée pour certains chemins */
  private boolean shouldSkipValidation(String path) {
    return PublicEndpoints.isPublic(path) || path.equals("/favicon.ico");
  }

  @Override
  public int getOrder() {
    return -100; // Execute before JwtToHeadersGatewayFilter
  }
}
