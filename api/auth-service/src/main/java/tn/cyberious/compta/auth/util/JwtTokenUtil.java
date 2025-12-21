package tn.cyberious.compta.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import tn.cyberious.compta.auth.config.JwtProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

  private final JwtProperties jwtProperties;

  private SecretKey getSigningKey() {
    byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(UserDetails userDetails, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("authorities", userDetails.getAuthorities());
    return createToken(claims, userDetails.getUsername(), jwtProperties.getExpiration());
  }

  public String generateRefreshToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername(), jwtProperties.getRefreshExpiration());
  }

  private String createToken(Map<String, Object> claims, String subject, Long expiration) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuer(jwtProperties.getIssuer())
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  public Boolean isTokenExpired(String token) {
    try {
      final Date expiration = getExpirationDateFromToken(token);
      return expiration.before(new Date());
    } catch (Exception e) {
      log.error("Error checking token expiration", e);
      return true;
    }
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    try {
      final String username = getUsernameFromToken(token);
      return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    } catch (Exception e) {
      log.error("Token validation error", e);
      return false;
    }
  }
}
