package tn.compta.commons.security.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import tn.compta.commons.security.exception.InvalidTokenException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;
import tn.compta.commons.security.model.UserPermission;

/**
 * Utility class for validating and parsing JWT tokens.
 *
 * <p>This class provides methods to validate JWT tokens and extract user details from claims.
 */
@Slf4j
public class JwtValidator {

  private final SecretKey secretKey;
  private final ObjectMapper objectMapper;

  /**
   * Create a new JWT validator.
   *
   * @param secret the JWT secret key
   */
  public JwtValidator(String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Validate a JWT token and extract user details.
   *
   * @param token the JWT token
   * @return the authenticated user details
   * @throws InvalidTokenException if the token is invalid
   */
  public AuthenticatedUserDetails validateAndExtract(String token) {
    try {
      Claims claims = parseToken(token);
      return extractUserDetails(claims);
    } catch (ExpiredJwtException e) {
      log.warn("JWT token expired: {}", e.getMessage());
      throw new InvalidTokenException("Token has expired", e);
    } catch (JwtException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      throw new InvalidTokenException("Invalid token", e);
    } catch (Exception e) {
      log.error("Error validating JWT token", e);
      throw new InvalidTokenException("Error validating token", e);
    }
  }

  /**
   * Parse the JWT token and return claims.
   *
   * @param token the JWT token
   * @return the claims
   * @throws JwtException if the token is invalid
   */
  private Claims parseToken(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Extract user details from JWT claims.
   *
   * @param claims the JWT claims
   * @return the authenticated user details
   */
  @SuppressWarnings("unchecked")
  private AuthenticatedUserDetails extractUserDetails(Claims claims) {
    Long userId = claims.get("userId", Long.class);
    String username = claims.getSubject();
    String email = claims.get("email", String.class);

    List<String> roles = extractList(claims, "roles");
    List<Long> societeIds = extractLongList(claims, "societeIds");
    List<UserPermission> permissions = extractPermissions(claims);

    Long primarySocieteId = claims.get("primarySocieteId", Long.class);
    Boolean active = claims.get("active", Boolean.class);
    Boolean locked = claims.get("locked", Boolean.class);

    return AuthenticatedUserDetails.builder()
        .userId(userId)
        .username(username)
        .email(email)
        .roles(roles != null ? roles : Collections.emptyList())
        .permissions(permissions != null ? permissions : Collections.emptyList())
        .societeIds(societeIds != null ? societeIds : Collections.emptyList())
        .primarySocieteId(primarySocieteId)
        .active(active != null && active)
        .locked(locked != null && locked)
        .build();
  }

  /**
   * Extract a list of strings from claims.
   *
   * @param claims the claims
   * @param key the claim key
   * @return the list of strings
   */
  @SuppressWarnings("unchecked")
  private List<String> extractList(Claims claims, String key) {
    Object value = claims.get(key);
    if (value instanceof List) {
      return (List<String>) value;
    }
    return Collections.emptyList();
  }

  /**
   * Extract a list of Longs from claims.
   *
   * @param claims the claims
   * @param key the claim key
   * @return the list of Longs
   */
  @SuppressWarnings("unchecked")
  private List<Long> extractLongList(Claims claims, String key) {
    Object value = claims.get(key);
    if (value instanceof List) {
      List<?> list = (List<?>) value;
      List<Long> result = new ArrayList<>();
      for (Object item : list) {
        if (item instanceof Number) {
          result.add(((Number) item).longValue());
        }
      }
      return result;
    }
    return Collections.emptyList();
  }

  /**
   * Extract permissions from claims.
   *
   * @param claims the claims
   * @return the list of permissions
   */
  @SuppressWarnings("unchecked")
  private List<UserPermission> extractPermissions(Claims claims) {
    Object value = claims.get("permissions");
    if (value instanceof List) {
      try {
        List<Map<String, String>> permissionMaps =
            objectMapper.convertValue(value, new TypeReference<List<Map<String, String>>>() {});
        List<UserPermission> permissions = new ArrayList<>();
        for (Map<String, String> map : permissionMaps) {
          String permission = map.get("permission");
          if (permission != null) {
            permissions.add(UserPermission.of(permission));
          }
        }
        return permissions;
      } catch (Exception e) {
        log.warn("Failed to parse permissions from claims", e);
      }
    }
    return Collections.emptyList();
  }

  /**
   * Check if a token is expired.
   *
   * @param token the JWT token
   * @return true if the token is expired
   */
  public boolean isTokenExpired(String token) {
    try {
      Claims claims = parseToken(token);
      return claims.getExpiration().before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Extract username from token without full validation.
   *
   * @param token the JWT token
   * @return the username
   */
  public String extractUsername(String token) {
    try {
      Claims claims = parseToken(token);
      return claims.getSubject();
    } catch (Exception e) {
      return null;
    }
  }
}
