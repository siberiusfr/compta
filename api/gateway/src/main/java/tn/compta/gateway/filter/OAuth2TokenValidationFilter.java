package tn.compta.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tn.compta.gateway.config.OAuth2TokenValidator;

/**
 * Filtre pour la validation des tokens JWT RSA issus par le serveur OAuth2.
 * Ce filtre valide les tokens avant de les transmettre aux services backend.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2TokenValidationFilter implements GatewayFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final OAuth2TokenValidator tokenValidator;

  @Override
  public Mono<Void> filter(
      ServerWebExchange exchange,
      GatewayFilterChain chain
  ) {
    String path = exchange.getRequest().getPath().value();

    // Skip validation for public endpoints and health checks
    if (shouldSkipValidation(path)) {
      log.debug("Skipping OAuth2 token validation for path: {}", path);
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      log.warn("Missing or invalid Authorization header for path: {}", path);
      exchange
        .getResponse()
        .setStatusCode(HttpStatus.UNAUTHORIZED)
        .setBody(
          "{\"error\":\"missing_or_invalid_authorization_header\",\"message\":\"Authorization header is required\"}"
        )
        .setHeaders(
          HttpHeaders.writableHttpHeaders()
            .setContentType(MediaType.APPLICATION_JSON)
        );
      return exchange.getResponse().setComplete();
    }

    String token = authHeader.substring(BEARER_PREFIX.length()).trim();

    if (token.isEmpty()) {
      log.warn("Empty token in Authorization header for path: {}", path);
      exchange
        .getResponse()
        .setStatusCode(HttpStatus.UNAUTHORIZED)
        .setBody(
          "{\"error\":\"empty_token\",\"message\":\"Token cannot be empty\"}"
        )
        .setHeaders(
          HttpHeaders.writableHttpHeaders()
            .setContentType(MediaType.APPLICATION_JSON)
        );
      return exchange.getResponse().setComplete();
    }

    // Validate the JWT token using OAuth2TokenValidator
    boolean isValid = tokenValidator.validateTokenSilent(token);

    if (!isValid) {
      log.warn("Invalid OAuth2 token for path: {}. Token: {}", path, token);
      exchange
        .getResponse()
        .setStatusCode(HttpStatus.UNAUTHORIZED)
        .setBody(
          "{\"error\":\"invalid_token\",\"message\":\"The provided token is invalid or expired\"}"
        )
        .setHeaders(
          HttpHeaders.writableHttpHeaders()
            .setContentType(MediaType.APPLICATION_JSON)
        );
      return exchange.getResponse().setComplete();
    }

    log.debug("OAuth2 token validated successfully for path: {}", path);

    // Add validation flag to exchange attributes for downstream filters
    exchange.getAttributes().put("oauth2.token.validated", true);

    return chain.filter(exchange);
  }

  /**
   * Détérmine si la validation doit être sautée pour certains chemins
   */
  private boolean shouldSkipValidation(String path) {
    return path.startsWith("/actuator") ||
           path.startsWith("/swagger") ||
           path.startsWith("/v3/api-docs") ||
           path.startsWith("/public") ||
           path.equals("/favicon.ico");
  }

  @Override
  public int getOrder() {
    return -100; // Execute before JwtToHeadersGatewayFilter
  }
}
