package tn.cyberious.compta.oauth2.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.oauth2.service.KeyManagementService;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JWKSourceConfig {

  private final KeyManagementService keyManagementService;

  @Bean
  public JWKSource<SecurityContext> jwkSource() {
    return keyManagementService.getJWKSource();
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    log.info("Initializing OAuth2 keys on application startup");
    keyManagementService.initializeKeys();
  }
}
