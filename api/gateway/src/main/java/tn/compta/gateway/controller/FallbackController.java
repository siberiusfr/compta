package tn.compta.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Fallback controller for circuit breaker.
 *
 * Provides graceful degradation when services are unavailable.
 * Supports all HTTP methods (GET, POST, PUT, DELETE, PATCH).
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

  /**
   * Fallback for auth service.
   */
  @RequestMapping(value = "/auth", method = {GET, POST, PUT, DELETE, PATCH})
  public ResponseEntity<Map<String, Object>> authServiceFallback() {
    log.warn("Auth service circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service d'authentification est temporairement indisponible. Veuillez reessayer dans quelques instants.",
            "service", "auth-service"
        ));
  }

  /**
   * Fallback for authorization service.
   */
  @RequestMapping(value = "/authz", method = {GET, POST, PUT, DELETE, PATCH})
  public ResponseEntity<Map<String, Object>> authzServiceFallback() {
    log.warn("Authorization service circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service d'autorisation est temporairement indisponible. Acces restreint temporairement.",
            "service", "authz-service"
        ));
  }

  /**
   * Fallback for invoice service.
   */
  @RequestMapping(value = "/invoices", method = {GET, POST, PUT, DELETE, PATCH})
  public ResponseEntity<Map<String, Object>> invoiceServiceFallback() {
    log.warn("Invoice service circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service de facturation est temporairement indisponible. Vos donnees sont securisees.",
            "service", "invoice-service"
        ));
  }

  /**
   * Fallback for employee service.
   */
  @RequestMapping(value = "/employees", method = {GET, POST, PUT, DELETE, PATCH})
  public ResponseEntity<Map<String, Object>> employeeServiceFallback() {
    log.warn("Employee service circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service RH est temporairement indisponible. Les donnees des employes seront accessibles sous peu.",
            "service", "employee-service"
        ));
  }

  /**
   * Generic fallback for any service.
   */
  @RequestMapping(value = "/generic", method = {GET, POST, PUT, DELETE, PATCH})
  public ResponseEntity<Map<String, Object>> genericFallback() {
    log.warn("Generic circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service demande est temporairement indisponible. Veuillez reessayer dans quelques instants."
        ));
  }
}
