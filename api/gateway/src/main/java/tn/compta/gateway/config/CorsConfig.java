package tn.compta.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for API Gateway.
 *
 * Allows frontend applications to call the API from different origins.
 * Origins are configured via application.yml based on environment.
 */
@Configuration
public class CorsConfig {

  @Value("${cors.allowed-origins:http://localhost:3000}")
  private List<String> allowedOrigins;

  @Value("${cors.max-age:3600}")
  private Long maxAge;

  /**
   * Configure CORS for reactive WebFlux.
   */
  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();

    // ✅ Allowed origins from configuration
    corsConfig.setAllowedOrigins(allowedOrigins);

    // Allowed HTTP methods
    corsConfig.setAllowedMethods(Arrays.asList(
        "GET",
        "POST",
        "PUT",
        "DELETE",
        "PATCH",
        "OPTIONS"
    ));

    // Allowed headers
    corsConfig.setAllowedHeaders(Arrays.asList(
        "Authorization",
        "Content-Type",
        "Accept",
        "X-Requested-With",
        "X-Tenant-Id"
    ));

    // Expose headers to frontend
    corsConfig.setExposedHeaders(Arrays.asList(
        "Authorization",
        "X-Total-Count",
        "X-Page-Number",
        "X-Page-Size"
    ));

    // Allow credentials (cookies, authorization headers)
    corsConfig.setAllowCredentials(true);

    // Cache preflight requests
    corsConfig.setMaxAge(maxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }
}
