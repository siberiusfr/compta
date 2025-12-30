package tn.cyberious.compta.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

  private final OAuth2AuthorizationService authorizationService;
  private final CacheManager cacheManager;

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
