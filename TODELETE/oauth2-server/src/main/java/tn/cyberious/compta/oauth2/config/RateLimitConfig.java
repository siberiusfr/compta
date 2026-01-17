package tn.cyberious.compta.oauth2.config;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.cyberious.compta.oauth2.filter.RateLimitFilter;
import tn.cyberious.compta.oauth2.metrics.OAuth2Metrics;

/**
 * Configuration for rate limiting on OAuth2 endpoints.
 *
 * <p>Rate limits: - /oauth2/token: 10 requests per minute per IP - /oauth2/revoke: 20 requests per
 * minute per IP - /oauth2/introspect: 100 requests per minute per IP - /login: 5 requests per
 * minute per IP - /api/users/password/reset: 3 requests per hour per email
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
    prefix = "oauth2.rate-limiting",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class RateLimitConfig {

  private static final Map<String, RateLimit> RATE_LIMITS =
      Map.of(
          "/oauth2/token", new RateLimit(10, TimeUnit.MINUTES),
          "/oauth2/revoke", new RateLimit(20, TimeUnit.MINUTES),
          "/oauth2/introspect", new RateLimit(100, TimeUnit.MINUTES),
          "/login", new RateLimit(5, TimeUnit.MINUTES),
          "/api/users/password/reset", new RateLimit(3, TimeUnit.HOURS));

  @Bean
  @Primary
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public OncePerRequestFilter rateLimitFilter(OAuth2Metrics oauth2Metrics) {
    return new RateLimitFilter(RATE_LIMITS, oauth2Metrics);
  }

  public record RateLimit(int limit, TimeUnit timeUnit) {}
}
