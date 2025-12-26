package tn.compta.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import java.net.ConnectException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Global error handler for Gateway routing errors.
 *
 * Handles:
 * - Service unavailable (ConnectException)
 * - Timeout errors
 * - 404 Not Found
 * - Generic routing errors
 */
@Slf4j
@Component
@Order(-2) // Higher priority than DefaultErrorWebExceptionHandler
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

  public GlobalErrorWebExceptionHandler(
      ErrorAttributes errorAttributes,
      WebProperties.Resources resources,
      ApplicationContext applicationContext,
      ServerCodecConfigurer configurer) {
    super(errorAttributes, resources, applicationContext);
    this.setMessageWriters(configurer.getWriters());
    this.setMessageReaders(configurer.getReaders());
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    Throwable error = getError(request);
    HttpStatus status = determineHttpStatus(error);

    Map<String, Object> errorAttributes = new HashMap<>();
    errorAttributes.put("timestamp", Instant.now().toString());
    errorAttributes.put("status", status.value());
    errorAttributes.put("error", status.getReasonPhrase());
    errorAttributes.put("message", determineErrorMessage(error));
    errorAttributes.put("path", request.path());

    log.error("Gateway error: {} - {}", status, errorAttributes.get("message"), error);

    return ServerResponse
        .status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(errorAttributes));
  }

  /**
   * Determine HTTP status based on exception type.
   */
  private HttpStatus determineHttpStatus(Throwable error) {
    if (error instanceof TimeoutException) {
      return HttpStatus.GATEWAY_TIMEOUT;
    } else if (error instanceof ConnectException) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    } else if (error instanceof CallNotPermittedException) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    } else if (error instanceof RequestNotPermitted) {
      return HttpStatus.TOO_MANY_REQUESTS;
    } else if (error.getMessage() != null && error.getMessage().contains("404")) {
      return HttpStatus.NOT_FOUND;
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  /**
   * Generate user-friendly error message.
   */
  private String determineErrorMessage(Throwable error) {
    if (error instanceof TimeoutException) {
      return "Le service a mis trop de temps à répondre. Veuillez réessayer.";
    } else if (error instanceof ConnectException) {
      return "Le service est temporairement indisponible. Veuillez réessayer plus tard.";
    } else if (error instanceof CallNotPermittedException) {
      return "Le service est temporairement indisponible (circuit ouvert). Veuillez réessayer plus tard.";
    } else if (error instanceof RequestNotPermitted) {
      return "Trop de requêtes. Veuillez patienter avant de réessayer.";
    } else if (error.getMessage() != null && error.getMessage().contains("404")) {
      return "Service ou endpoint non trouvé.";
    }
    return "Une erreur interne s'est produite. Veuillez contacter le support si le problème persiste.";
  }
}
