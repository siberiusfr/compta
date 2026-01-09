package tn.compta.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.compta.gateway.exception.JwtAuthenticationEntryPoint;

/** Security configuration for API Gateway using OAuth2 Resource Server with JWKS. */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Value("${oauth2.jwks-url:http://localhost:9000/oauth2/jwks}")
  private String jwksUrl;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        // Disable CSRF for stateless JWT API
        .csrf(ServerHttpSecurity.CsrfSpec::disable)

        // Enable CORS (uses CorsWebFilter bean)
        .cors(cors -> {})

        // Stateless session - don't create sessions
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

        // Authorization rules
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers(PublicEndpoints.PATTERNS)
                    .permitAll()
                    .anyExchange()
                    .authenticated())

        // OAuth2 Resource Server with JWT (RSA via JWKS)
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(
                        jwt ->
                            jwt.jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                    // Custom authentication entry point for JWT errors
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint))

        // Exception handling
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint));

    return http.build();
  }

  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    log.info("Configuring JWT decoder with JWKS URL: {}", jwksUrl);
    return NimbusReactiveJwtDecoder.withJwkSetUri(jwksUrl).build();
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
