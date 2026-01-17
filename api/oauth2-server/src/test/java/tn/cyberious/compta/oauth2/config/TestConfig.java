package tn.cyberious.compta.oauth2.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Test configuration for OAuth2 server integration tests. */
@TestConfiguration
public class TestConfig {

  @Bean
  @Primary
  public PasswordEncoder testPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
