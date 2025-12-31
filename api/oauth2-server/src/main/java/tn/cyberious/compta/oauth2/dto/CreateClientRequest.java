package tn.cyberious.compta.oauth2.dto;

import java.time.Duration;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientRequest {

  @NotBlank(message = "Client ID is required")
  private String clientId;

  private String clientSecret;

  @NotBlank(message = "Client name is required")
  private String clientName;

  @NotNull(message = "Client authentication methods are required")
  private List<String> clientAuthenticationMethods;

  @NotNull(message = "Authorization grant types are required")
  private List<String> authorizationGrantTypes;

  @NotNull(message = "Redirect URIs are required")
  private List<String> redirectUris;

  private List<String> postLogoutRedirectUris;

  @NotNull(message = "Scopes are required")
  private List<String> scopes;

  private Boolean requireAuthorizationConsent;

  private Boolean requireProofKey;

  private Boolean reuseRefreshTokens;

  private Duration accessTokenTimeToLive;

  private Duration authorizationCodeTimeToLive;

  private Duration refreshTokenTimeToLive;
}
