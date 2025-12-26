package tn.compta.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom authentication entry point for JWT validation errors.
 *
 * Provides clear error messages when JWT authentication fails:
 * - Token expired
 * - Token invalid
 * - Token malformed
 * - Missing authorization header
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    log.error("Authentication error: {}", ex.getMessage());

    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", LocalDateTime.now().toString());
    errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
    errorResponse.put("error", "Unauthorized");
    errorResponse.put("message", getErrorMessage(ex));
    errorResponse.put("path", exchange.getRequest().getPath().value());

    byte[] bytes;
    try {
      bytes = objectMapper.writeValueAsBytes(errorResponse);
    } catch (JsonProcessingException e) {
      bytes = "{\"error\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
    }

    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }

  /**
   * Extract user-friendly error message from exception.
   */
  private String getErrorMessage(AuthenticationException ex) {
    String message = ex.getMessage();

    if (message == null) {
      return "Authentication failed";
    }

    // JWT expired
    if (message.contains("expired") || message.contains("Jwt expired")) {
      return "JWT token has expired. Please login again.";
    }

    // Invalid signature
    if (message.contains("signature") || message.contains("invalid")) {
      return "JWT token signature is invalid.";
    }

    // Malformed JWT
    if (message.contains("malformed") || message.contains("Malformed")) {
      return "JWT token is malformed.";
    }

    // Missing token
    if (message.contains("Bearer token") || message.contains("Authorization header")) {
      return "Missing or invalid Authorization header. Expected: Bearer <token>";
    }

    // Default message
    return "Authentication failed: " + message;
  }
}
