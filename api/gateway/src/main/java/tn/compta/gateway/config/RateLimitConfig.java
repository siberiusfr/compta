package tn.compta.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.Optional;

/**
 * Rate limiting configuration.
 *
 * Uses authenticated user ID as key, falls back to client IP for anonymous requests.
 * This prevents a single anonymous attacker from exhausting the rate limit for all
 * unauthenticated users.
 */
@Slf4j
@Configuration
public class RateLimitConfig {

  @Bean
  public KeyResolver userKeyResolver() {
    return exchange -> ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(auth -> auth != null && auth.isAuthenticated()
            && !"anonymousUser".equals(auth.getPrincipal()))
        .map(Principal::getName)
        .switchIfEmpty(Mono.defer(() -> {
          // Fallback to client IP for anonymous users
          String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
              .map(InetSocketAddress::getAddress)
              .map(InetAddress::getHostAddress)
              .orElse("unknown");

          log.debug("Rate limiting anonymous request from IP: {}", clientIp);
          return Mono.just("anon:" + clientIp);
        }));
  }
}
