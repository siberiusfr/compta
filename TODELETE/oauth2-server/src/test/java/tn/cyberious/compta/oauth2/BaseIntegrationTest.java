package tn.cyberious.compta.oauth2;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class for all integration tests. Provides common setup and utility methods for OAuth2
 * testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

  @Autowired protected WebApplicationContext context;

  @Autowired protected ObjectMapper objectMapper;

  protected MockMvc mockMvc;

  // Test constants
  protected static final String PUBLIC_CLIENT_ID = "public-client";
  protected static final String GATEWAY_CLIENT_ID = "gateway";
  protected static final String GATEWAY_CLIENT_SECRET = "gateway-secret-change-in-production";
  protected static final String TEST_REDIRECT_URI = "http://localhost:3000/authorized";
  protected static final String ADMIN_USERNAME = "admin";
  protected static final String ADMIN_PASSWORD = "admin123";
  protected static final String USER_USERNAME = "user";
  protected static final String USER_PASSWORD = "user123";

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  /**
   * Generate PKCE code verifier (random string).
   *
   * @return code verifier string
   */
  protected String generateCodeVerifier() {
    byte[] bytes = new byte[32];
    new java.security.SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /**
   * Generate PKCE code challenge from verifier using S256 method.
   *
   * @param codeVerifier the code verifier
   * @return code challenge string
   */
  protected String generateCodeChallenge(String codeVerifier) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate code challenge", e);
    }
  }

  /**
   * Create Basic Auth header value.
   *
   * @param clientId client ID
   * @param clientSecret client secret
   * @return Basic auth header value
   */
  protected String basicAuth(String clientId, String clientSecret) {
    String credentials = clientId + ":" + clientSecret;
    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  /**
   * Convert object to JSON string.
   *
   * @param object the object to convert
   * @return JSON string
   */
  protected String toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert object to JSON", e);
    }
  }
}
