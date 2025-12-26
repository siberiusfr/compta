package tn.compta.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import tn.compta.gateway.exception.JwtAuthenticationEntryPoint;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Security configuration for API Gateway using OAuth2 Resource Server.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Value("${jwt.secret}")
  private String jwtSecret;

  /**
   * Public endpoints that don't require authentication.
   */
  private static final String[] PUBLIC_ENDPOINTS = {
      "/auth/**",
      "/actuator/health",
      "/actuator/info",
      "/swagger-ui.html",
      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/webjars/**"
  };

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        // ✅ Disable CSRF for stateless JWT API
        .csrf(ServerHttpSecurity.CsrfSpec::disable)

        // ✅ Enable CORS (uses CorsWebFilter bean)
        .cors(cors -> {})

        // ✅ Stateless session - don't create sessions
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

        // ✅ Authorization rules
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers(PUBLIC_ENDPOINTS).permitAll()
            .anyExchange().authenticated()
        )

        // ✅ OAuth2 Resource Server with JWT
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtDecoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
            // ✅ Custom authentication entry point for JWT errors
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )

        // ✅ Exception handling
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        );

    return http.build();
  }

  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

    NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
        .withSecretKey(secretKey)
        .macAlgorithm(MacAlgorithm.HS256)
        .build();

    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("compta-auth"));

    return decoder;
  }

  @Bean
  public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
    return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
  }

  @Bean
  public JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
    JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
    converter.setAuthoritiesClaimName("roles");
    converter.setAuthorityPrefix("ROLE_");
    return converter;
  }
}
