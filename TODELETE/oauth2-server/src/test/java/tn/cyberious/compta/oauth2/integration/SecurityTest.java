package tn.cyberious.compta.oauth2.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.cyberious.compta.oauth2.BaseIntegrationTest;

/** Integration tests for Security features (CSRF, CORS, authentication, authorization). */
@DisplayName("Security Tests")
class SecurityTest extends BaseIntegrationTest {

  @Nested
  @DisplayName("CSRF Protection")
  class CsrfProtectionTests {

    @Test
    @DisplayName("Should reject login without CSRF token")
    void shouldRejectLoginWithoutCsrfToken() throws Exception {
      mockMvc
          .perform(
              post("/login")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("username", ADMIN_USERNAME)
                  .param("password", ADMIN_PASSWORD))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should accept login with CSRF token")
    void shouldAcceptLoginWithCsrfToken() throws Exception {
      mockMvc
          .perform(
              post("/login")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("username", ADMIN_USERNAME)
                  .param("password", ADMIN_PASSWORD))
          .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should not require CSRF for OAuth2 endpoints")
    void shouldNotRequireCsrfForOAuth2Endpoints() throws Exception {
      // OAuth2 token endpoint should work without CSRF (uses client credentials)
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                  .param("grant_type", "client_credentials")
                  .param("scope", "openid"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should not require CSRF for API endpoints with JWT")
    void shouldNotRequireCsrfForApiEndpointsWithJwt() throws Exception {
      // First get a token
      var tokenResult =
          mockMvc
              .perform(
                  post("/oauth2/token")
                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                      .param("grant_type", "client_credentials")
                      .param("scope", "openid read"))
              .andExpect(status().isOk())
              .andReturn();

      String token =
          com.jayway.jsonpath.JsonPath.read(
              tokenResult.getResponse().getContentAsString(), "$.access_token");

      // API calls with JWT should not need CSRF
      mockMvc
          .perform(get("/api/users").header("Authorization", "Bearer " + token))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("CORS Configuration")
  class CorsTests {

    @Test
    @DisplayName("Should include CORS headers for allowed origin")
    void shouldIncludeCorsHeadersForAllowedOrigin() throws Exception {
      mockMvc
          .perform(
              options("/oauth2/token")
                  .header("Origin", "http://localhost:3000")
                  .header("Access-Control-Request-Method", "POST"))
          .andExpect(status().isOk())
          .andExpect(header().exists("Access-Control-Allow-Origin"))
          .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("Should allow credentials in CORS")
    void shouldAllowCredentialsInCors() throws Exception {
      mockMvc
          .perform(
              options("/oauth2/token")
                  .header("Origin", "http://localhost:3000")
                  .header("Access-Control-Request-Method", "POST"))
          .andExpect(status().isOk())
          .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
  }

  @Nested
  @DisplayName("Authentication")
  class AuthenticationTests {

    @Test
    @DisplayName("Should reject access to protected endpoints without authentication")
    void shouldRejectAccessWithoutAuthentication() throws Exception {
      mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject access with expired token")
    void shouldRejectAccessWithExpiredToken() throws Exception {
      // Use an obviously invalid/expired token
      mockMvc
          .perform(get("/api/users").header("Authorization", "Bearer expired-token"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject access with malformed token")
    void shouldRejectAccessWithMalformedToken() throws Exception {
      mockMvc
          .perform(get("/api/users").header("Authorization", "Bearer not.a.valid.jwt"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject access with wrong authorization scheme")
    void shouldRejectAccessWithWrongAuthorizationScheme() throws Exception {
      mockMvc
          .perform(get("/api/users").header("Authorization", "Basic sometoken"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Public Endpoints")
  class PublicEndpointsTests {

    @Test
    @DisplayName("Should allow access to login page without authentication")
    void shouldAllowAccessToLoginPage() throws Exception {
      mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to JWKS endpoint without authentication")
    void shouldAllowAccessToJwksEndpoint() throws Exception {
      mockMvc
          .perform(get("/.well-known/jwks.json"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.keys").isArray());
    }

    @Test
    @DisplayName("Should allow access to password reset without authentication")
    void shouldAllowAccessToPasswordReset() throws Exception {
      mockMvc
          .perform(
              post("/api/users/password/reset")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"test@example.com\"}"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to actuator health without authentication")
    void shouldAllowAccessToActuatorHealth() throws Exception {
      mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to swagger UI without authentication")
    void shouldAllowAccessToSwaggerUi() throws Exception {
      mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should allow access to OpenAPI docs without authentication")
    void shouldAllowAccessToOpenApiDocs() throws Exception {
      mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("JWT Validation")
  class JwtValidationTests {

    @Test
    @DisplayName("Should reject JWT with invalid signature")
    void shouldRejectJwtWithInvalidSignature() throws Exception {
      // Create a JWT with invalid signature
      String invalidJwt =
          "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.invalidsignature";

      mockMvc
          .perform(get("/api/users").header("Authorization", "Bearer " + invalidJwt))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject JWT signed with wrong key")
    void shouldRejectJwtSignedWithWrongKey() throws Exception {
      // A valid-looking JWT but signed with a different key
      String wrongKeyJwt =
          "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9."
              + "eyJzdWIiOiJ0ZXN0IiwiZXhwIjo5OTk5OTk5OTk5fQ."
              + "cXVpdGVhbG9uZ3NpZ25hdHVyZXRoYXRpc25vdHZhbGlk";

      mockMvc
          .perform(get("/api/users").header("Authorization", "Bearer " + wrongKeyJwt))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("PKCE Security")
  class PkceSecurityTests {

    @Test
    @DisplayName("Should require PKCE for public client authorization")
    void shouldRequirePkceForPublicClient() throws Exception {
      // Try to authorize without PKCE - should fail
      mockMvc
          .perform(
              get("/oauth2/authorize")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("response_type", "code")
                  .param("scope", "openid")
                  .param("redirect_uri", TEST_REDIRECT_URI))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept authorization with valid PKCE")
    void shouldAcceptAuthorizationWithValidPkce() throws Exception {
      String codeVerifier = generateCodeVerifier();
      String codeChallenge = generateCodeChallenge(codeVerifier);

      // This will redirect to login, but validates PKCE is accepted
      mockMvc
          .perform(
              get("/oauth2/authorize")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("response_type", "code")
                  .param("scope", "openid")
                  .param("redirect_uri", TEST_REDIRECT_URI)
                  .param("code_challenge", codeChallenge)
                  .param("code_challenge_method", "S256"))
          .andExpect(status().is3xxRedirection());
    }
  }

  @Nested
  @DisplayName("Input Validation")
  class InputValidationTests {

    @Test
    @DisplayName("Should reject SQL injection in username")
    void shouldRejectSqlInjectionInUsername() throws Exception {
      mockMvc
          .perform(
              post("/login")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("username", "admin' OR '1'='1")
                  .param("password", "password"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @DisplayName("Should reject XSS in redirect_uri")
    void shouldRejectXssInRedirectUri() throws Exception {
      String codeVerifier = generateCodeVerifier();
      String codeChallenge = generateCodeChallenge(codeVerifier);

      mockMvc
          .perform(
              get("/oauth2/authorize")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("response_type", "code")
                  .param("scope", "openid")
                  .param("redirect_uri", "javascript:alert('xss')")
                  .param("code_challenge", codeChallenge)
                  .param("code_challenge_method", "S256"))
          .andExpect(status().isBadRequest());
    }
  }
}
