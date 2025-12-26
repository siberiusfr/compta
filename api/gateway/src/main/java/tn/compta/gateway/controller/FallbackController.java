package tn.compta.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker.
 *
 * Provides graceful degradation when services are unavailable.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

  /**
   * Fallback for auth service.
   */
  @GetMapping("/auth")
  public ResponseEntity<Map<String, Object>> authServiceFallback() {
    log.warn("Auth service circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service d'authentification est temporairement indisponible. Veuillez réessayer dans quelques instants.",
            "service", "auth-service"
        ));
  }

  /**
   * Fallback for authorization service.
   */
  @GetMapping("/authz")
  public ResponseEntity<Map<String, Object>> authzServiceFallback() {
    log.warn("Authorization service circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service d'autorisation est temporairement indisponible. Accès restreint temporairement.",
            "service", "authz-service"
        ));
  }

  /**
   * Fallback for invoice service.
   */
  @GetMapping("/invoices")
  public ResponseEntity<Map<String, Object>> invoiceServiceFallback() {
    log.warn("Invoice service circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service de facturation est temporairement indisponible. Vos données sont sécurisées et seront disponibles sous peu.",
            "service", "invoice-service",
            "suggestion", "Vous pouvez consulter les factures en cache ou réessayer dans quelques minutes."
        ));
  }

  /**
   * Fallback for employee service.
   */
  @GetMapping("/employees")
  public ResponseEntity<Map<String, Object>> employeeServiceFallback() {
    log.warn("Employee service circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service RH est temporairement indisponible. Les données des employés seront accessibles sous peu.",
            "service", "employee-service"
        ));
  }

  /**
   * Generic fallback for any service.
   */
  @GetMapping("/generic")
  public ResponseEntity<Map<String, Object>> genericFallback() {
    log.warn("Generic circuit breaker activated - returning fallback response");

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", "Le service demandé est temporairement indisponible. Nos équipes travaillent à rétablir le service.",
            "suggestion", "Veuillez réessayer dans quelques instants."
        ));
  }
}
