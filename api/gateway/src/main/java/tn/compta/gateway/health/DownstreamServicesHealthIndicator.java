package tn.compta.gateway.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator qui vérifie l'état de tous les services downstream.
 * 
 * Permet de savoir si la gateway peut router correctement vers les services.
 */
@Slf4j
@Component
public class DownstreamServicesHealthIndicator implements ReactiveHealthIndicator {

  private final WebClient webClient;

  @Value("${AUTH_SERVICE_URL:http://localhost:8081}")
  private String authServiceUrl;

  @Value("${AUTHZ_SERVICE_URL:http://localhost:8084}")
  private String authzServiceUrl;

  @Value("${INVOICE_SERVICE_URL:http://localhost:8082}")
  private String invoiceServiceUrl;

  @Value("${EMPLOYEE_SERVICE_URL:http://localhost:8083}")
  private String employeeServiceUrl;

  public DownstreamServicesHealthIndicator(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder
        .build();
  }

  @Override
  public Mono<Health> health() {
    return Flux.merge(
            checkService("auth-service", authServiceUrl),
            checkService("authz-service", authzServiceUrl),
            checkService("invoice-service", invoiceServiceUrl),
            checkService("employee-service", employeeServiceUrl)
        )
        .collectList()
        .map(serviceStatuses -> {
          Map<String, Object> details = new HashMap<>();
          boolean allUp = true;

          for (Map<String, Object> serviceStatus : serviceStatuses) {
            String serviceName = (String) serviceStatus.get("name");
            String status = (String) serviceStatus.get("status");
            
            details.put(serviceName, serviceStatus);
            
            if (!"UP".equals(status)) {
              allUp = false;
            }
          }

          if (allUp) {
            return Health.up().withDetails(details).build();
          } else {
            return Health.down().withDetails(details).build();
          }
        })
        .onErrorResume(error -> {
          log.error("Error checking downstream services health", error);
          return Mono.just(Health.down()
              .withException(error)
              .build());
        });
  }

  /**
   * Vérifie l'état d'un service via son endpoint /actuator/health.
   */
  private Mono<Map<String, Object>> checkService(String serviceName, String serviceUrl) {
    String healthUrl = serviceUrl + "/actuator/health";

    return webClient.get()
        .uri(healthUrl)
        .retrieve()
        .toBodilessEntity()
        .map(response -> {
          Map<String, Object> status = new HashMap<>();
          status.put("name", serviceName);
          status.put("url", serviceUrl);
          
          if (response.getStatusCode().is2xxSuccessful()) {
            status.put("status", "UP");
          } else {
            status.put("status", "DOWN");
            status.put("code", response.getStatusCode().value());
          }
          
          return status;
        })
        .timeout(Duration.ofSeconds(5))
        .onErrorResume(error -> {
          log.warn("Service {} is DOWN: {}", serviceName, error.getMessage());
          
          Map<String, Object> status = new HashMap<>();
          status.put("name", serviceName);
          status.put("url", serviceUrl);
          status.put("status", "DOWN");
          status.put("error", error.getMessage());
          
          return Mono.just(status);
        });
  }
}
