package tn.compta.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS configuration for API Gateway.
 *
 * Allows frontend applications to call the API from different origins.
 * Configure allowed origins based on environment (dev/prod).
 */
@Configuration
public class CorsConfig {

  /**
   * Configure CORS for reactive WebFlux.
   *
   * Development: Allows localhost:3000 (React/Vue dev server)
   * Production: Configure actual frontend domain
   */
  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();

    // Allowed origins - CONFIGURE BASED ON ENVIRONMENT
    corsConfig.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",      // React/Vue dev server
        "http://localhost:4200",      // Angular dev server
        "http://localhost:8081",      // Another local frontend
        "https://compta.tn",          // Production frontend
        "https://app.compta.tn"       // Production app
    ));

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
        "X-Tenant-Id"  // Custom header if needed
    ));

    // Expose headers to frontend
    corsConfig.setExposedHeaders(Arrays.asList(
        "Authorization",
        "X-Total-Count"  // For pagination
    ));

    // Allow credentials (cookies, authorization headers)
    corsConfig.setAllowCredentials(true);

    // How long the browser should cache preflight requests (in seconds)
    corsConfig.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }
}
