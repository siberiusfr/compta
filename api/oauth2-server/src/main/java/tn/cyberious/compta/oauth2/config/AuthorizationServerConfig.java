package tn.cyberious.compta.oauth2.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
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
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
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
  public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
    // Public client with PKCE support (for SPAs, mobile apps)
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
                    .accessTokenTimeToLive(java.time.Duration.ofMinutes(30))
                    .authorizationCodeTimeToLive(java.time.Duration.ofMinutes(5))
                    .reuseRefreshTokens(false)
                    .build())
            .build();

    // Gateway client for token validation (backend service)
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
                    .accessTokenTimeToLive(java.time.Duration.ofMinutes(60))
                    .build())
            .build();

    // Confidential clients with PKCE support (for backend services)
    RegisteredClient accountingServiceClient =
        RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("accounting-service")
            .clientSecret(passwordEncoder.encode("accounting-secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("http://127.0.0.1:8080/login/oauth2/code/accounting-service")
            .redirectUri("http://127.0.0.1:8080/authorized")
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
                    .accessTokenTimeToLive(java.time.Duration.ofMinutes(30))
                    .authorizationCodeTimeToLive(java.time.Duration.ofMinutes(5))
                    .build())
            .build();

    RegisteredClient authzServiceClient =
        RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("authz-service")
            .clientSecret(passwordEncoder.encode("authz-secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("http://127.0.0.1:8081/login/oauth2/code/authz-service")
            .redirectUri("http://127.0.0.1:8081/authorized")
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
                    .accessTokenTimeToLive(java.time.Duration.ofMinutes(30))
                    .authorizationCodeTimeToLive(java.time.Duration.ofMinutes(5))
                    .build())
            .build();

    RegisteredClient hrServiceClient =
        RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("hr-service")
            .clientSecret(passwordEncoder.encode("hr-secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("http://127.0.0.1:8082/login/oauth2/code/hr-service")
            .redirectUri("http://127.0.0.1:8082/authorized")
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
                    .accessTokenTimeToLive(java.time.Duration.ofMinutes(30))
                    .authorizationCodeTimeToLive(java.time.Duration.ofMinutes(5))
                    .build())
            .build();

    return new InMemoryRegisteredClientRepository(
        publicClient, gatewayClient, accountingServiceClient, authzServiceClient, hrServiceClient);
  }

  @Bean
  public JWKSource<SecurityContext> jwkSource() {
    KeyPair keyPair = generateRsaKey();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    RSAKey rsaKey =
        new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();
    JWKSet jwkSet = new JWKSet(rsaKey);
    return new ImmutableJWKSet<>(jwkSet);
  }

  private static KeyPair generateRsaKey() {
    KeyPair keyPair;
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      keyPair = keyPairGenerator.generateKeyPair();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    return keyPair;
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
