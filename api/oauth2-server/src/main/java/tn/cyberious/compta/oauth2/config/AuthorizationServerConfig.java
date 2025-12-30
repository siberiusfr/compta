package tn.cyberious.compta.oauth2.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.time.Duration;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
import tn.cyberious.compta.oauth2.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
      throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(Customizer.withDefaults());
    http.exceptionHandling(
            (exceptions) ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
        .oauth2ResourceServer((resourceServer) -> resourceServer.jwt(Customizer.withDefaults()));
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(
      HttpSecurity http, CustomUserDetailsService userDetailsService) throws Exception {
    http.userDetailsService(userDetailsService)
        .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
        .formLogin(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public RegisteredClientRepository registeredClientRepository(
      DataSource dataSource, PasswordEncoder passwordEncoder) {
    JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(dataSource);

    // Initialize default clients if they don't exist
    initializeDefaultClients(repository, passwordEncoder);

    return repository;
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
              .clientSecret(passwordEncoder.encode("gateway-secret"))
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
                  TokenSettings.builder()
                      .accessTokenTimeToLive(Duration.ofMinutes(60))
                      .build())
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
    return AuthorizationServerSettings.builder().issuer("http://localhost:9000").build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
  }
}
