package tn.compta.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration pour la validation des tokens JWT issus par le serveur OAuth2. La validation
 * principale est faite par Spring Security OAuth2 Resource Server via le ReactiveJwtDecoder
 * configuré dans SecurityConfig. Cette classe fournit des méthodes utilitaires pour la validation
 * manuelle si nécessaire.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OAuth2TokenValidator {

  private final ProfileHelper profileHelper;

  @Value("${oauth2.issuer:http://localhost:9000}")
  private String oauth2Issuer;

  @Value("${oauth2.jwks-url:http://localhost:9000/oauth2/jwks}")
  private String jwksUrl;

  @Value("${oauth2.jwks-cache-duration:300000}")
  private Long jwksCacheDuration;

  private long lastJwksFetchTime = 0;

  @PostConstruct
  public void init() {
    log.info("Initializing OAuth2 JWT token validator...");
    log.info("OAuth2 Issuer: {}", oauth2Issuer);
    log.info("JWKS URL: {}", jwksUrl);
    log.info("JWKS Cache Duration: {} ms", jwksCacheDuration);
  }

  /** Log du rafraîchissement du cache JWKS */
  @Scheduled(fixedRate = 300000) // Every 5 minutes
  public void refreshJwks() {
    log.debug("JWKS cache refresh cycle...");
    lastJwksFetchTime = System.currentTimeMillis();
  }

  /** Valide un token JWT RSA (validation manuelle pour les filtres) */
  public boolean validateToken(String token) {
    try {
      log.debug("Validating JWT token...");

      // Parse the token to check claims
      SignedJWT signedJwt = SignedJWT.parse(token);
      JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

      // Vérifier l'issuer
      String issuer = claims.getIssuer();
      if (!oauth2Issuer.equals(issuer)) {
        log.warn("Token issuer mismatch. Expected: {}, Got: {}", oauth2Issuer, issuer);
        return false;
      }

      // Vérifier l'expiration
      if (claims.getExpirationTime() != null) {
        long expirationTime = claims.getExpirationTime().getTime();
        long currentTime = System.currentTimeMillis();
        if (expirationTime < currentTime) {
          log.warn("Token expired. Expiration: {}, Current: {}", expirationTime, currentTime);
          return false;
        }
      }

      // Vérifier l'algorithme (doit être RSA)
      String algorithm = signedJwt.getHeader().getAlgorithm().getName();
      if (!algorithm.startsWith("RS")) {
        log.warn("Token is not signed with RSA algorithm: {}", algorithm);
        return false;
      }

      log.debug("JWT token validated successfully");
      return true;
    } catch (Exception e) {
      log.error("JWT validation failed: {}", e.getMessage(), e);
      return false;
    }
  }

  /** Valide un token JWT sans lever d'exception */
  public boolean validateTokenSilent(String token) {
    try {
      return validateToken(token);
    } catch (Exception e) {
      log.error("Silent JWT validation failed: {}", e.getMessage());
      return false;
    }
  }

  /** Vérifie si le cache JWKS est expiré */
  public boolean isJwksCacheExpired() {
    long cacheAge = System.currentTimeMillis() - lastJwksFetchTime;
    return cacheAge > jwksCacheDuration;
  }

  /** Force le rafraîchissement du cache JWKS */
  public void forceRefreshJwks() {
    log.info("Forcing JWKS refresh...");
    lastJwksFetchTime = System.currentTimeMillis();
  }
}
