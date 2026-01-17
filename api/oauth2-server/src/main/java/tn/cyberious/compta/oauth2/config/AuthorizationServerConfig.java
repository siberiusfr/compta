package tn.cyberious.compta.oauth2.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tn.cyberious.compta.oauth2.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class AuthorizationServerConfig {

  private final CorsProperties corsProperties;
  private final JwtAuthenticationConverter jwtAuthenticationConverter;

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
            exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer.jwt(
                    jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Configure allowed origins from properties
    configuration.setAllowedOrigins(corsProperties.getAllowedOriginsList());

    // Configure allowed methods from properties
    configuration.setAllowedMethods(corsProperties.getAllowedMethodsList());

    // Configure allowed headers
    configuration.setAllowedHeaders(List.of(corsProperties.getAllowedHeaders()));

    // Allow credentials
    configuration.setAllowCredentials(corsProperties.isAllowCredentials());

    // Set max age
    configuration.setMaxAge(corsProperties.getMaxAge());

    // Use UrlBasedCorsConfigurationSource with path patterns
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(
      HttpSecurity http, CustomUserDetailsService userDetailsService, CsrfConfig csrfConfig)
      throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .userDetailsService(userDetailsService)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/login",
                        "/error",
                        "/actuator/**",
                        "/api/users/password/reset",
                        "/api/users/password/reset/confirm",
                        "/api/users/email/verify",
                        "/api/users/email/verify/confirm",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer.jwt(
                    jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
        // Return 401 for API requests, redirect to login for browser requests
        .exceptionHandling(
            exceptions ->
                exceptions
                    .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**"))
                    .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
        .formLogin(form -> form.loginPage("/login").permitAll())
        // Enable CSRF protection with custom token repository
        .csrf(
            csrf ->
                csrf.csrfTokenRepository(csrfConfig.csrfTokenRepository())
                    // Allow public endpoints and API endpoints (JWT doesn't need CSRF)
                    .ignoringRequestMatchers(
                        "/login", "/logout", "/error", "/actuator/**", "/api/**"));
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
              .postLogoutRedirectUri("http://localhost:3000")
              .scope(OidcScopes.OPENID)
              .scope("read")
              .scope("write")
              .clientSettings(
                  ClientSettings.builder()
                      .requireAuthorizationConsent(false)
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
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager("tokens");
  }
}
