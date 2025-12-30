package tn.cyberious.compta.oauth2.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.oauth2.dto.IntrospectionResponse;
import tn.cyberious.compta.oauth2.generated.tables.Users;
import tn.cyberious.compta.oauth2.generated.tables.records.UsersRecord;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenIntrospectionService {

  private final OAuth2AuthorizationService authorizationService;
  private final DSLContext dsl;

  @Transactional(readOnly = true)
  public IntrospectionResponse introspectToken(String tokenValue, String tokenTypeHint) {
    log.debug("Introspecting token with type hint: {}", tokenTypeHint);

    IntrospectionResponse response = IntrospectionResponse.builder().active(false).build();

    // Try to find authorization by token
    OAuth2Authorization authorization = findAuthorizationByToken(tokenValue, tokenTypeHint);

    if (authorization == null) {
      log.debug("Token not found or invalid");
      return response;
    }

    // Check if token is expired
    OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
    if (accessToken != null && accessToken.getToken().getExpiresAt().isBefore(Instant.now())) {
      log.debug("Token has expired");
      return response;
    }

    // Token is valid
    response.setActive(true);

    // Extract token information
    OAuth2AccessToken token = accessToken.getToken();
    response.setTokenType(token.getTokenType().getValue());
    response.setExp(token.getExpiresAt());
    response.setIat(token.getIssuedAt());

    // Set client ID
    response.setClientId(authorization.getRegisteredClientId());

    // Set subject (user ID)
    response.setSub(authorization.getPrincipalName());

    // Extract additional claims from the token
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

    // Extract user information
    String principalName = authorization.getPrincipalName();
    UsersRecord user =
        dsl.selectFrom(Users.USERS).where(Users.USERS.USERNAME.eq(principalName)).fetchOne();

    if (user != null) {
      response.setUsername(user.getUsername());
      response.setEmail(user.getEmail());
      response.setRoles(
          dsl.select(tn.cyberious.compta.oauth2.generated.tables.Roles.ROLES.NAME)
              .from(tn.cyberious.compta.oauth2.generated.tables.UserRoles.USER_ROLES)
              .join(tn.cyberious.compta.oauth2.generated.tables.Roles.ROLES)
              .on(
                  tn.cyberious.compta.oauth2.generated.tables.UserRoles.USER_ROLES.ROLE_ID.eq(
                      tn.cyberious.compta.oauth2.generated.tables.Roles.ROLES.ID))
              .where(
                  tn.cyberious.compta.oauth2.generated.tables.UserRoles.USER_ROLES.USER_ID.eq(
                      user.getId()))
              .fetch(tn.cyberious.compta.oauth2.generated.tables.Roles.ROLES.NAME));
    }

    log.debug("Token introspection completed successfully for client: {}", response.getClientId());
    return response;
  }

  private OAuth2Authorization findAuthorizationByToken(String tokenValue, String tokenTypeHint) {
    // Try to find by access token
    OAuth2Authorization authorization =
        authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);

    if (authorization == null && tokenTypeHint != null) {
      // Try to find by refresh token if hint is provided
      if ("refresh_token".equalsIgnoreCase(tokenTypeHint)) {
        authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.REFRESH_TOKEN);
      }
    }

    return authorization;
  }
}
