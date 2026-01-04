package tn.cyberious.compta.oauth2.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import tn.cyberious.compta.oauth2.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

  @Value("${oauth2.issuer:http://localhost:9000}")
  private String issuerUrl;

  @Value("${oauth2.gateway.secret:gateway-secret-change-in-production}")
  private String gatewaySecret;

  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
      throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(Customizer.withDefaults());

    // Configure CORS
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

    // Disable CSRF for OAuth2 endpoints (they use PKCE, client secrets, etc.)
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/oauth2/**", "/.well-known/**", "/jwks"));

    http.exceptionHandling(
            (exceptions) ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
        .oauth2ResourceServer((resourceServer) -> resourceServer.jwt(Customizer.withDefaults()));
    return http.build();
  }

  @Bean
  public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
    org.springframework.web.cors.CorsConfiguration configuration =
        new org.springframework.web.cors.CorsConfiguration();

    // Configure allowed origins
    configuration.setAllowedOrigins(
        Arrays.asList("http://localhost:3000", "http://localhost:8080", "https://app.compta.tn"));

    // Configure allowed methods (use String list, not HttpMethod)
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Configure allowed headers
    configuration.setAllowedHeaders(Arrays.asList("*"));

    // Allow credentials
    configuration.setAllowCredentials(true);

    // Set max age
    configuration.setMaxAge(3600L);

    // Use UrlBasedCorsConfigurationSource with path patterns
    org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
        new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    source.registerCorsConfiguration("/oauth2/**", configuration);
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }

  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(
      HttpSecurity http, CustomUserDetailsService userDetailsService, CsrfConfig csrfConfig)
      throws Exception {
    http.userDetailsService(userDetailsService)
        .authorizeHttpRequests(
            (authorize) ->
                authorize
                    .requestMatchers("/login", "/error", "/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(form -> form.loginPage("/login").permitAll())
        // Enable CSRF protection with custom token repository
        .csrf(
            csrf ->
                csrf.csrfTokenRepository(csrfConfig.csrfTokenRepository())
                    // Allow public endpoints
                    .ignoringRequestMatchers("/login", "/logout", "/error", "/actuator/**"));
    return http.build();
  }

  @Bean
  public RegisteredClientRepository registeredClientRepository(
      JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
    JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

    // Initialize default clients if they don't exist
    initializeDefaultClients(repository, passwordEncoder);

    return repository;
  }

  @Bean
  public OAuth2AuthorizationService authorizationService(
      JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
    return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
  }

  @Bean
  public OAuth2AuthorizationConsentService authorizationConsentService(
      JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
    return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
  }

  private void initializeDefaultClients(
      JdbcRegisteredClientRepository repository, PasswordEncoder passwordEncoder) {
    // Public client with PKCE support (for SPAs, mobile apps)
    if (repository.findByClientId("public-client") == null) {
      RegisteredClient publicClient =
          RegisteredClient.withId(UUID.randomUUID().toString())
              .clientId("public-client")
              .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
              .redirectUri("http://localhost:3000/authorized")
              .scope(OidcScopes.OPENID)
              .scope("read")
              .scope("write")
              .clientSettings(
                  ClientSettings.builder()
                      .requireAuthorizationConsent(true)
                      .requireProofKey(true)
                      .build())
              .tokenSettings(
                  TokenSettings.builder()
                      .accessTokenTimeToLive(Duration.ofMinutes(30))
                      .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                      .reuseRefreshTokens(false)
                      .build())
              .build();
      repository.save(publicClient);
    }

    // Gateway client for service-to-service communication (Client Credentials flow)
    if (repository.findByClientId("gateway") == null) {
      RegisteredClient gatewayClient =
          RegisteredClient.withId(UUID.randomUUID().toString())
              .clientId("gateway")
              .clientSecret(passwordEncoder.encode(gatewaySecret))
              .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
              .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
              .redirectUri("http://localhost:8080/authorized")
              .scope(OidcScopes.OPENID)
              .scope("read")
              .scope("write")
              .clientSettings(
                  ClientSettings.builder()
                      .requireAuthorizationConsent(false)
                      .requireProofKey(false)
                      .build())
              .tokenSettings(
                  TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(60)).build())
              .build();
      repository.save(gatewayClient);
    }
  }

  @Bean
  public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder().issuer(issuerUrl).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
  }

  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager("tokens");
  }
}
