package tn.cyberious.compta.oauth2.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class JwtAuthenticationConfig {

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
    return converter;
  }

  private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
    return jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>();

      // Extract roles from "roles" claim
      List<String> roles = jwt.getClaimAsStringList("roles");
      if (roles != null) {
        for (String role : roles) {
          // Add role as-is if it already starts with ROLE_, otherwise add ROLE_ prefix
          if (role.startsWith("ROLE_")) {
            authorities.add(new SimpleGrantedAuthority(role));
          } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
          }
        }
      }

      // Also extract scopes from "scope" claim
      List<String> scopes = jwt.getClaimAsStringList("scope");
      if (scopes != null) {
        for (String scope : scopes) {
          authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
        }
      }

      return authorities;
    };
  }
}
