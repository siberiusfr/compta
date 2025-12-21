package tn.compta.commons.security.util;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.compta.commons.security.exception.InvalidTokenException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

class JwtValidatorTest {

  private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256";

  private JwtValidator jwtValidator;
  private SecretKey secretKey;

  @BeforeEach
  void setUp() {
    jwtValidator = new JwtValidator(SECRET);
    secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void validateAndExtract_whenTokenIsValid_returnsUserDetails() {
    String token = generateValidToken();

    AuthenticatedUserDetails user = jwtValidator.validateAndExtract(token);

    assertNotNull(user);
    assertEquals(1L, user.getUserId());
    assertEquals("testuser", user.getUsername());
    assertEquals("test@example.com", user.getEmail());
    assertEquals(List.of("ADMIN", "USER"), user.getRoles());
  }

  @Test
  void validateAndExtract_whenTokenIsExpired_throwsInvalidTokenException() {
    String token = generateExpiredToken();

    assertThrows(InvalidTokenException.class, () -> jwtValidator.validateAndExtract(token));
  }

  @Test
  void validateAndExtract_whenTokenIsMalformed_throwsInvalidTokenException() {
    String malformedToken = "not.a.valid.token";

    assertThrows(
        InvalidTokenException.class, () -> jwtValidator.validateAndExtract(malformedToken));
  }

  @Test
  void isTokenExpired_whenTokenIsExpired_returnsTrue() {
    String token = generateExpiredToken();

    assertTrue(jwtValidator.isTokenExpired(token));
  }

  @Test
  void isTokenExpired_whenTokenIsValid_returnsFalse() {
    String token = generateValidToken();

    assertFalse(jwtValidator.isTokenExpired(token));
  }

  @Test
  void extractUsername_whenTokenIsValid_returnsUsername() {
    String token = generateValidToken();

    String username = jwtValidator.extractUsername(token);

    assertEquals("testuser", username);
  }

  private String generateValidToken() {
    return Jwts.builder()
        .subject("testuser")
        .claim("userId", 1L)
        .claim("email", "test@example.com")
        .claim("roles", List.of("ADMIN", "USER"))
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(secretKey)
        .compact();
  }

  private String generateExpiredToken() {
    return Jwts.builder()
        .subject("testuser")
        .claim("userId", 1L)
        .claim("email", "test@example.com")
        .issuedAt(new Date(System.currentTimeMillis() - 7200000))
        .expiration(new Date(System.currentTimeMillis() - 3600000))
        .signWith(secretKey)
        .compact();
  }
}
