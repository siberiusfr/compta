package tn.cyberious.compta.oauth2.service;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.oauth2.dto.IntrospectionResponse;
import tn.cyberious.compta.oauth2.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenIntrospectionService {

  private final OAuth2AuthorizationService authorizationService;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public IntrospectionResponse introspectToken(String tokenValue, String tokenTypeHint) {
    log.debug("Introspecting token with type hint: {}", tokenTypeHint);

    IntrospectionResponse response = IntrospectionResponse.builder().active(false).build();

    OAuth2Authorization authorization = findAuthorizationByToken(tokenValue, tokenTypeHint);

    if (authorization == null) {
      log.debug("Token not found or invalid");
      return response;
    }

    OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
    if (accessToken != null && accessToken.getToken().getExpiresAt().isBefore(Instant.now())) {
      log.debug("Token has expired");
      return response;
    }

    response.setActive(true);

    OAuth2AccessToken token = accessToken.getToken();
    response.setTokenType(token.getTokenType().getValue());
    response.setExp(token.getExpiresAt());
    response.setIat(token.getIssuedAt());

    response.setClientId(authorization.getRegisteredClientId());
    response.setSub(authorization.getPrincipalName());

    try {
      JWT jwt = JWTParser.parse(tokenValue);
      jwt.getJWTClaimsSet()
          .getClaims()
          .forEach(
              (key, value) -> {
                switch (key) {
                  case "aud":
                    if (value instanceof List) {
                      response.setAud(String.join(" ", (List<String>) value));
                    } else {
                      response.setAud(value.toString());
                    }
                    break;
                  case "iss":
                    response.setIss(value.toString());
                    break;
                  case "scope":
                    if (value instanceof List) {
                      response.setScope((List<String>) value);
                    } else if (value instanceof String) {
                      response.setScope(List.of(value.toString().split(" ")));
                    }
                    break;
                }
              });
    } catch (ParseException e) {
      log.warn("Failed to parse JWT claims", e);
    }

    String principalName = authorization.getPrincipalName();
    userRepository
        .findByUsername(principalName)
        .ifPresent(
            user -> {
              response.setUsername(user.getUsername());
              response.setEmail(user.getEmail());
              response.setRoles(userRepository.getUserRoles(user.getId()));
            });

    log.debug("Token introspection completed successfully for client: {}", response.getClientId());
    return response;
  }

  private OAuth2Authorization findAuthorizationByToken(String tokenValue, String tokenTypeHint) {
    OAuth2Authorization authorization =
        authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);

    if (authorization == null && tokenTypeHint != null) {
      if ("refresh_token".equalsIgnoreCase(tokenTypeHint)) {
        authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.REFRESH_TOKEN);
      }
    }

    return authorization;
  }
}
