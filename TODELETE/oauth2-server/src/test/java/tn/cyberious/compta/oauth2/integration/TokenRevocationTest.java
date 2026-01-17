package tn.cyberious.compta.oauth2.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.cyberious.compta.oauth2.BaseIntegrationTest;

/** Integration tests for OAuth2 Token Revocation (RFC 7009). */
@DisplayName("Token Revocation Tests")
class TokenRevocationTest extends BaseIntegrationTest {

  private String validAccessToken;

  @BeforeEach
  void obtainToken() throws Exception {
    // Get a valid access token first
    MvcResult result =
        mockMvc
            .perform(
                post("/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                    .param("grant_type", "client_credentials")
                    .param("scope", "openid read"))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    validAccessToken = JsonPath.read(responseBody, "$.access_token");
  }

  @Nested
  @DisplayName("Revocation Endpoint")
  class RevocationEndpointTests {

    @Test
    @DisplayName("Should revoke valid access token")
    void shouldRevokeValidAccessToken() throws Exception {
      // Revoke the token (requires authentication)
      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"token\":\""
                          + validAccessToken
                          + "\",\"tokenTypeHint\":\"access_token\"}"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept revocation of already revoked token")
    void shouldAcceptRevocationOfAlreadyRevokedToken() throws Exception {
      // Get a second token for authentication
      MvcResult result =
          mockMvc
              .perform(
                  post("/oauth2/token")
                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                      .param("grant_type", "client_credentials")
                      .param("scope", "openid read"))
              .andExpect(status().isOk())
              .andReturn();
      String authToken = JsonPath.read(result.getResponse().getContentAsString(), "$.access_token");

      // Revoke the token twice - should not fail
      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + authToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk());

      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + authToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept revocation of invalid token")
    void shouldAcceptRevocationOfInvalidToken() throws Exception {
      // RFC 7009: Server MUST respond with HTTP 200 even for invalid tokens
      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"invalid-token\"}"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject revocation without token")
    void shouldRejectRevocationWithoutToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept token_type_hint for access_token")
    void shouldAcceptTokenTypeHintForAccessToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"token\":\""
                          + validAccessToken
                          + "\",\"tokenTypeHint\":\"access_token\"}"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept token_type_hint for refresh_token")
    void shouldAcceptTokenTypeHintForRefreshToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"token\":\"some-refresh-token\",\"tokenTypeHint\":\"refresh_token\"}"))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("Token Blacklist Verification")
  class TokenBlacklistTests {

    @Test
    @DisplayName("Should blacklist token after revocation")
    void shouldBlacklistTokenAfterRevocation() throws Exception {
      // Get a second token for authentication (to use after revoking the first)
      MvcResult result =
          mockMvc
              .perform(
                  post("/oauth2/token")
                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                      .param("grant_type", "client_credentials")
                      .param("scope", "openid read"))
              .andExpect(status().isOk())
              .andReturn();
      String authToken = JsonPath.read(result.getResponse().getContentAsString(), "$.access_token");

      // Revoke the first token
      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + authToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk());

      // Introspect the revoked token - should be inactive
      mockMvc
          .perform(
              post("/oauth2/introspect")
                  .header("Authorization", "Bearer " + authToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.active").value(false));
    }
  }
}
