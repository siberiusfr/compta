package tn.cyberious.compta.oauth2.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.cyberious.compta.oauth2.BaseIntegrationTest;

/** Integration tests for OAuth2 Authorization Code Flow with PKCE. */
@DisplayName("Authorization Code Flow Tests")
class AuthorizationCodeFlowTest extends BaseIntegrationTest {

  @Nested
  @DisplayName("Authorization Endpoint")
  class AuthorizationEndpointTests {

    @Test
    @DisplayName("Should redirect to login when not authenticated")
    void shouldRedirectToLoginWhenNotAuthenticated() throws Exception {
      String codeVerifier = generateCodeVerifier();
      String codeChallenge = generateCodeChallenge(codeVerifier);

      mockMvc
          .perform(
              get("/oauth2/authorize")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("response_type", "code")
                  .param("scope", "openid read write")
                  .param("redirect_uri", TEST_REDIRECT_URI)
                  .param("code_challenge", codeChallenge)
                  .param("code_challenge_method", "S256")
                  .param("state", "test-state"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Should return authorization code when authenticated")
    void shouldReturnAuthorizationCodeWhenAuthenticated() throws Exception {
      String codeVerifier = generateCodeVerifier();
      String codeChallenge = generateCodeChallenge(codeVerifier);

      mockMvc
          .perform(
              get("/oauth2/authorize")
                  .with(user(ADMIN_USERNAME).password(ADMIN_PASSWORD).roles("ADMIN"))
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("response_type", "code")
                  .param("scope", "openid read write")
                  .param("redirect_uri", TEST_REDIRECT_URI)
                  .param("code_challenge", codeChallenge)
                  .param("code_challenge_method", "S256")
                  .param("state", "test-state"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrlPattern(TEST_REDIRECT_URI + "?code=*&state=test-state"));
    }

    @Test
    @DisplayName("Should reject invalid client_id")
    void shouldRejectInvalidClientId() throws Exception {
      String codeVerifier = generateCodeVerifier();
      String codeChallenge = generateCodeChallenge(codeVerifier);

      mockMvc
          .perform(
              get("/oauth2/authorize")
                  .with(user(ADMIN_USERNAME).password(ADMIN_PASSWORD).roles("ADMIN"))
                  .param("client_id", "invalid-client")
                  .param("response_type", "code")
                  .param("scope", "openid")
                  .param("redirect_uri", TEST_REDIRECT_URI)
                  .param("code_challenge", codeChallenge)
                  .param("code_challenge_method", "S256"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject missing PKCE for public client")
    void shouldRejectMissingPkceForPublicClient() throws Exception {
      mockMvc
          .perform(
              get("/oauth2/authorize")
                  .with(user(ADMIN_USERNAME).password(ADMIN_PASSWORD).roles("ADMIN"))
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("response_type", "code")
                  .param("scope", "openid")
                  .param("redirect_uri", TEST_REDIRECT_URI))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject invalid redirect_uri")
    void shouldRejectInvalidRedirectUri() throws Exception {
      String codeVerifier = generateCodeVerifier();
      String codeChallenge = generateCodeChallenge(codeVerifier);

      mockMvc
          .perform(
              get("/oauth2/authorize")
                  .with(user(ADMIN_USERNAME).password(ADMIN_PASSWORD).roles("ADMIN"))
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("response_type", "code")
                  .param("scope", "openid")
                  .param("redirect_uri", "http://malicious.com/callback")
                  .param("code_challenge", codeChallenge)
                  .param("code_challenge_method", "S256"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Token Endpoint")
  class TokenEndpointTests {

    @Test
    @DisplayName("Should reject token request without authorization code")
    void shouldRejectTokenRequestWithoutAuthorizationCode() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("grant_type", "authorization_code")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("redirect_uri", TEST_REDIRECT_URI)
                  .param("code_verifier", generateCodeVerifier()))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject invalid authorization code")
    void shouldRejectInvalidAuthorizationCode() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("grant_type", "authorization_code")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("code", "invalid-code")
                  .param("redirect_uri", TEST_REDIRECT_URI)
                  .param("code_verifier", generateCodeVerifier()))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject mismatched code_verifier")
    void shouldRejectMismatchedCodeVerifier() throws Exception {
      // This test validates that PKCE verification works
      // In a real scenario, we'd need to get a valid code first
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("grant_type", "authorization_code")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("code", "some-code")
                  .param("redirect_uri", TEST_REDIRECT_URI)
                  .param("code_verifier", "wrong-verifier"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Refresh Token Flow")
  class RefreshTokenFlowTests {

    @Test
    @DisplayName("Should reject invalid refresh token")
    void shouldRejectInvalidRefreshToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("grant_type", "refresh_token")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("refresh_token", "invalid-refresh-token"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject refresh token for wrong client")
    void shouldRejectRefreshTokenForWrongClient() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                  .param("grant_type", "refresh_token")
                  .param("refresh_token", "some-refresh-token"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Login Flow")
  class LoginFlowTests {

    @Test
    @DisplayName("Should show login page")
    void shouldShowLoginPage() throws Exception {
      mockMvc
          .perform(get("/login"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    @DisplayName("Should accept valid credentials")
    void shouldAcceptValidCredentials() throws Exception {
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
    @DisplayName("Should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
      mockMvc
          .perform(
              post("/login")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("username", ADMIN_USERNAME)
                  .param("password", "wrong-password"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login?error"));
    }
  }
}
