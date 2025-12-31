package tn.compta.gateway.exception;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    log.warn("Authentication error: {}", ex.getMessage());

    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
    errorResponse.put("error", "Unauthorized");
    errorResponse.put("message", getErrorMessage(ex));
    errorResponse.put("path", exchange.getRequest().getPath().value());

    byte[] bytes;
    try {
      bytes = objectMapper.writeValueAsBytes(errorResponse);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize authentication error response", e);
      bytes = "{\"error\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
    }

    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }

  private String getErrorMessage(AuthenticationException ex) {
    String message = ex.getMessage();
    if (message == null) {
      return "Authentication failed";
    }
    if (message.contains("expired") || message.contains("Jwt expired")) {
      return "JWT token has expired. Please login again.";
    }
    if (message.contains("signature") || message.contains("invalid")) {
      return "JWT token signature is invalid.";
    }
    if (message.contains("malformed") || message.contains("Malformed")) {
      return "JWT token is malformed.";
    }
    if (message.contains("Bearer token") || message.contains("Authorization header")) {
      return "Missing or invalid Authorization header. Expected: Bearer <token>";
    }
    return "Authentication failed: " + message;
  }
}
