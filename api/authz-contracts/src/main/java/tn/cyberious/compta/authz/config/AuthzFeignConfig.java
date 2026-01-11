package tn.cyberious.compta.authz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;
import feign.RequestInterceptor;

@Configuration
public class AuthzFeignConfig {

  @Value("${authz.service.url:http://localhost:8085}")
  private String authzServiceUrl;

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }

  @Bean
  public RequestInterceptor authzRequestInterceptor() {
    return template -> {
      template.header("Content-Type", "application/json");
    };
  }
}
