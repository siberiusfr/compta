package tn.cyberious.compta.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
  private String secret;
  private Long expiration;
  private Long refreshExpiration;
  private String issuer;
  private String header = "Authorization";
  private String prefix = "Bearer ";
}
