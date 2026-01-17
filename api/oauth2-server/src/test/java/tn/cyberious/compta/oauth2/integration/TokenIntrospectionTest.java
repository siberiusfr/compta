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

/** Integration tests for OAuth2 Token Introspection (RFC 7662). */
@DisplayName("Token Introspection Tests")
class TokenIntrospectionTest extends BaseIntegrationTest {

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
                    .param("scope", "openid read write"))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    validAccessToken = JsonPath.read(responseBody, "$.access_token");
  }

  @Nested
  @DisplayName("Introspection Endpoint")
  class IntrospectionEndpointTests {

    @Test
    @DisplayName("Should return active=true for valid token")
    void shouldReturnActiveTrueForValidToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/introspect")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should return token metadata for valid token")
    void shouldReturnTokenMetadataForValidToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/introspect")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.active").value(true))
          .andExpect(jsonPath("$.client_id").value(GATEWAY_CLIENT_ID))
          .andExpect(jsonPath("$.token_type").value("Bearer"))
          .andExpect(jsonPath("$.scope").exists())
          .andExpect(jsonPath("$.exp").isNumber())
          .andExpect(jsonPath("$.iat").isNumber());
    }

    @Test
    @DisplayName("Should return active=false for invalid token")
    void shouldReturnActiveFalseForInvalidToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/introspect")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"invalid-token\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should return active=false for expired token")
    void shouldReturnActiveFalseForExpiredToken() throws Exception {
      // Create an obviously invalid/expired token
      String expiredToken =
          "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxfQ.invalid";

      mockMvc
          .perform(
              post("/oauth2/introspect")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + expiredToken + "\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should reject introspection without token")
    void shouldRejectIntrospectionWithoutToken() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/introspect")
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
              post("/oauth2/introspect")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"token\":\""
                          + validAccessToken
                          + "\",\"tokenTypeHint\":\"access_token\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should return active=false for revoked token")
    void shouldReturnActiveFalseForRevokedToken() throws Exception {
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

      // Revoke the token first
      mockMvc
          .perform(
              post("/oauth2/revoke")
                  .header("Authorization", "Bearer " + authToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk());

      // Now introspect - should be inactive
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

  @Nested
  @DisplayName("Token Claims Validation")
  class TokenClaimsTests {

    @Test
    @DisplayName("Should return correct issuer claim")
    void shouldReturnCorrectIssuerClaim() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/introspect")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.iss").exists());
    }

    @Test
    @DisplayName("Should return scope in response")
    void shouldReturnScopeInResponse() throws Exception {
      mockMvc
          .perform(
              post("/oauth2/introspect")
                  .header("Authorization", "Bearer " + validAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"" + validAccessToken + "\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.scope").exists());
    }
  }
}
