package tn.cyberious.compta.oauth2.service;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.oauth2.jti.TokenBlacklistService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

  private final OAuth2AuthorizationService authorizationService;
  private final CacheManager cacheManager;
  private final TokenBlacklistService tokenBlacklistService;

  @Transactional
  public void revokeToken(String tokenValue, String tokenTypeHint) {
    log.info("Revoking token with type hint: {}", tokenTypeHint);

    // Try to find authorization by token
    OAuth2Authorization authorization = findAuthorizationByToken(tokenValue, tokenTypeHint);

    if (authorization == null) {
      log.debug("Token not found, nothing to revoke");
      return;
    }

    // Remove from cache
    invalidateTokenInCache(tokenValue);

    // Remove from authorization (delete the authorization record)
    authorizationService.remove(authorization);

    log.info("Token revoked successfully for client: {}", authorization.getRegisteredClientId());
  }

  private OAuth2Authorization findAuthorizationByToken(String tokenValue, String tokenTypeHint) {
    // Try to find by access token
    OAuth2Authorization authorization =
        authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);

    if (authorization == null) {
      // Try to find by refresh token
      authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.REFRESH_TOKEN);
    }

    if (authorization == null && tokenTypeHint != null) {
      // Try based on the hint
      if ("access_token".equalsIgnoreCase(tokenTypeHint)) {
        authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
      } else if ("refresh_token".equalsIgnoreCase(tokenTypeHint)) {
        authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.REFRESH_TOKEN);
      }
    }

    return authorization;
  }

  /** Extract JTI from a JWT token. */
  private String extractJti(String tokenValue) {
    try {
      SignedJWT jwt = SignedJWT.parse(tokenValue);
      return jwt.getJWTClaimsSet().getJWTID();
    } catch (Exception e) {
      log.warn("Failed to extract JTI from token", e);
      return null;
    }
  }

  /** Extract expiration time from an authorization. */
  private java.time.Instant extractExpirationTime(OAuth2Authorization authorization) {
    try {
      if (authorization.getAccessToken() != null) {
        return authorization.getAccessToken().getToken().getExpiresAt();
      }
      if (authorization.getRefreshToken() != null) {
        return authorization.getRefreshToken().getToken().getExpiresAt();
      }
      return null;
    } catch (Exception e) {
      log.warn("Failed to extract expiration time from authorization", e);
      return null;
    }
  }

  private void invalidateTokenInCache(String tokenValue) {
    try {
      Cache tokenCache = cacheManager.getCache("oauth2Tokens");
      if (tokenCache != null) {
        tokenCache.evict(tokenValue);
        log.debug("Token evicted from cache");
      }
    } catch (Exception e) {
      log.warn("Failed to evict token from cache", e);
    }
  }
}
