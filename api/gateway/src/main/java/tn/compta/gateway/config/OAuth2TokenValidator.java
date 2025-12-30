package tn.compta.gateway.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.URLJWKSource;
import com.nimbusds.jose.proc.JWSAlgorithm;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.PostConstruct;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import tn.compta.gateway.config.ProfileHelper;

/**
 * Configuration pour la validation des tokens JWT RSA issus par le serveur OAuth2.
 * Le serveur OAuth2 expose les clés publiques via l'endpoint JWKS.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OAuth2TokenValidator {

  private final ProfileHelper profileHelper;
  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${oauth2.issuer:http://localhost:9000}")
  private String oauth2Issuer;

  @Value("${oauth2.jwks-cache-duration:300000}")
  private Long jwksCacheDuration;

  private RemoteJWKSet<SecurityContext> jwkSource;
  private long lastJwksFetchTime = 0;
  private JWKSet cachedJwkSet;

  @PostConstruct
  public void init() {
    log.info("Initializing OAuth2 JWT token validator...");
    log.info("OAuth2 Issuer: {}", oauth2Issuer);
    log.info("JWKS Cache Duration: {} ms", jwksCacheDuration);
    fetchJwks();
  }

  /**
   * Récupère les clés JWKS depuis le serveur OAuth2
   */
  @Scheduled(fixedRate = 300000) // Refresh every 5 minutes
  public void refreshJwks() {
    log.debug("Refreshing JWKS from OAuth2 server...");
    fetchJwks();
  }

  private void fetchJwks() {
    try {
      String jwksUrl = oauth2Issuer + "/.well-known/jwks.json";
      log.info("Fetching JWKS from: {}", jwksUrl);

      URL url = new URL(jwksUrl);
      RemoteJWKSet RemoteJWKSet = new URLJWKSource(url);
      RemoteJWKSet.setConnectTimeout(5000); // 5 seconds timeout
      RemoteJWKSet.setReadTimeout(5000);

      JWKSet jwkSet = RemoteJWKSet.getJWKSet();
      this.jwkSource = new RemoteJWKSet(url, jwkSet);
      this.cachedJwkSet = jwkSet;
      this.lastJwksFetchTime = System.currentTimeMillis();

      log.info("JWKS fetched successfully. Keys: {}", jwkSet.getKeys().size());
    } catch (Exception e) {
      log.error("Failed to fetch JWKS from OAuth2 server: {}", e.getMessage(), e);
      // Keep previous cache on error
    }
  }

  /**
   * Valide un token JWT RSA
   */
  public boolean validateToken(String token) {
    try {
      log.debug("Validating JWT token...");

      com.nimbusds.jose.jwt.JWT jwt = com.nimbusds.jose.jwt.JWT.parse(token);

      // Vérifier l'algorithme (doit être RSA)
      JWSAlgorithm algorithm = jwt.getHeader().getAlgorithm();
      if (!algorithm.getName().startsWith("RS")) {
        log.warn("Token is not signed with RSA algorithm: {}", algorithm.getName());
        return false;
      }

      // Vérifier l'issuer
      String issuer = jwt.getJWTClaimsSet().getIssuer();
      if (!oauth2Issuer.equals(issuer)) {
        log.warn("Token issuer mismatch. Expected: {}, Got: {}", oauth2Issuer, issuer);
        return false;
      }

      // Vérifier l'expiration
      long expirationTime = jwt.getJWTClaimsSet().getExpirationTime().getTime();
      long currentTime = System.currentTimeMillis();
      if (expirationTime < currentTime) {
        log.warn("Token expired. Expiration: {}, Current: {}", expirationTime, currentTime);
        return false;
      }

      // Vérifier la signature avec les clés JWKS
      jwt.verify(jwkSource);

      log.debug("JWT token validated successfully");
      return true;
    } catch (JOSEException e) {
      log.error("JWT validation failed: {}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * Valide un token JWT sans lever d'exception
   */
  public boolean validateTokenSilent(String token) {
    try {
      return validateToken(token);
    } catch (Exception e) {
      log.error("Silent JWT validation failed: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Récupère les clés JWKS en cache
   */
  public JWKSet getCachedJwkSet() {
    return cachedJwkSet;
  }

  /**
   * Vérifie si le cache JWKS est expiré
   */
  public boolean isJwksCacheExpired() {
    long cacheAge = System.currentTimeMillis() - lastJwksFetchTime;
    return cacheAge > jwksCacheDuration;
  }

  /**
   * Force le rafraîchissement du cache JWKS
   */
  public void forceRefreshJwks() {
    log.info("Forcing JWKS refresh...");
    fetchJwks();
  }
}
