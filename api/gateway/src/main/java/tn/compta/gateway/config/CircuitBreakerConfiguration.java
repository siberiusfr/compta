package tn.compta.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit Breaker configuration using Resilience4j.
 *
 * Prevents cascade failures by:
 * - Opening circuit after failure threshold
 * - Half-open state for recovery testing
 * - Automatic recovery monitoring
 */
@Configuration
public class CircuitBreakerConfiguration {

  /**
   * Default circuit breaker configuration for all services.
   */
  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
        .circuitBreakerConfig(CircuitBreakerConfig.custom()
            // Open circuit after 50% failure rate
            .failureRateThreshold(50)

            // Minimum number of calls before calculating failure rate
            .minimumNumberOfCalls(5)

            // Wait 30 seconds before attempting recovery
            .waitDurationInOpenState(Duration.ofSeconds(30))

            // Number of calls in half-open state to test recovery
            .permittedNumberOfCallsInHalfOpenState(3)

            // Size of sliding window for failure rate calculation
            .slidingWindowSize(10)

            // Slow call threshold (consider as failure)
            .slowCallDurationThreshold(Duration.ofSeconds(10))
            .slowCallRateThreshold(50)

            .build())

        .timeLimiterConfig(TimeLimiterConfig.custom()
            // Timeout for each call
            .timeoutDuration(Duration.ofSeconds(30))
            .build())

        .build());
  }

  /**
   * Custom configuration for critical services (e.g., auth-service).
   * More aggressive to protect authentication.
   */
  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> authServiceCustomizer() {
    return factory -> factory.configure(builder -> builder
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .failureRateThreshold(30) // More sensitive
                .minimumNumberOfCalls(3)
                .waitDurationInOpenState(Duration.ofSeconds(60)) // Longer wait
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(5)
                .build())

            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10)) // Shorter timeout
                .build()),

        "authService");
  }

  /**
   * Custom configuration for invoice service (longer processing time).
   */
  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> invoiceServiceCustomizer() {
    return factory -> factory.configure(builder -> builder
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .failureRateThreshold(60) // More tolerant
                .minimumNumberOfCalls(10)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .slidingWindowSize(20)
                .slowCallDurationThreshold(Duration.ofSeconds(20))
                .build())

            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(60)) // Longer timeout
                .build()),

        "invoiceService");
  }
}
