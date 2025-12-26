package tn.compta.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import java.security.Principal;

@Configuration
public class RateLimitConfig {
  @Bean
  public KeyResolver userKeyResolver() {
    return exchange -> ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Principal::getName)
        .defaultIfEmpty("anonymous");
  }
}
