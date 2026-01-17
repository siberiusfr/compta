package tn.cyberious.compta.oauth2.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.cyberious.compta.oauth2.BaseIntegrationTest;

/** Integration tests for OAuth2 Client Credentials Flow. */
@DisplayName("Client Credentials Flow Tests")
class ClientCredentialsFlowTest extends BaseIntegrationTest {

  @Nested
  @DisplayName("Token Endpoint - Client Credentials")
  class ClientCredentialsTokenTests {

    @Test
    @DisplayName("Should issue access token for valid client credentials")
    void shouldIssueAccessTokenForValidClientCredentials() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                  .param("grant_type", "client_credentials")
                  .param("scope", "openid read write"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.access_token").exists())
          .andExpect(jsonPath("$.token_type").value("Bearer"))
          .andExpect(jsonPath("$.expires_in").isNumber())
          .andExpect(jsonPath("$.scope").exists());
    }

    @Test
    @DisplayName("Should issue token with requested scopes")
    void shouldIssueTokenWithRequestedScopes() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                  .param("grant_type", "client_credentials")
                  .param("scope", "read"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.scope").value(containsString("read")));
    }

    @Test
    @DisplayName("Should reject invalid client secret")
    void shouldRejectInvalidClientSecret() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, "wrong-secret"))
                  .param("grant_type", "client_credentials")
                  .param("scope", "openid"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject unknown client")
    void shouldRejectUnknownClient() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth("unknown-client", "some-secret"))
                  .param("grant_type", "client_credentials")
                  .param("scope", "openid"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject missing authorization header")
    void shouldRejectMissingAuthorizationHeader() throws Exception {
      // OAuth2 token endpoint without client auth redirects to login or returns 401
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("grant_type", "client_credentials")
                  .param("scope", "openid"))
          .andExpect(
              result -> {
                int status = result.getResponse().getStatus();
                assertTrue(status == 401 || status == 302, "Expected 401 or 302, got " + status);
              });
    }

    private void assertTrue(boolean condition, String message) {
      if (!condition) {
        throw new AssertionError(message);
      }
    }

    @Test
    @DisplayName("Should reject invalid scope")
    void shouldRejectInvalidScope() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                  .param("grant_type", "client_credentials")
                  .param("scope", "invalid-scope"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not return refresh token for client credentials")
    void shouldNotReturnRefreshTokenForClientCredentials() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                  .param("grant_type", "client_credentials")
                  .param("scope", "openid"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.refresh_token").doesNotExist());
    }

    @Test
    @DisplayName("Should reject client credentials for public client")
    void shouldRejectClientCredentialsForPublicClient() throws Exception {
      // Public client cannot use client_credentials grant - returns 401 or redirects
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("grant_type", "client_credentials")
                  .param("client_id", PUBLIC_CLIENT_ID)
                  .param("scope", "openid"))
          .andExpect(
              result -> {
                int status = result.getResponse().getStatus();
                assertTrue(
                    status == 401 || status == 302 || status == 400,
                    "Expected 400, 401 or 302, got " + status);
              });
    }
  }

  @Nested
  @DisplayName("Token Validation")
  class TokenValidationTests {

    @Test
    @DisplayName("Should issue valid JWT token")
    void shouldIssueValidJwtToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                  .param("grant_type", "client_credentials")
                  .param("scope", "openid"))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.access_token")
                  .value(matchesPattern("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")));
    }
  }
}
