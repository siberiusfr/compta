package tn.cyberious.compta.oauth2.config;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

/** Customizes JWT tokens to include user roles and additional claims. */
@Component
public class JwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

  @Override
  public void customize(JwtEncodingContext context) {
    Authentication principal = context.getPrincipal();

    Set<String> roles =
        principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    // Add roles to both access token and ID token
    if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())
        || OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
      context.getClaims().claim("roles", roles);
    }
  }
}
