package tn.cyberious.compta.oauth2.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClientRequest {

  private String clientSecret;

  @NotBlank(message = "Client name is required")
  private String clientName;

  private List<String> clientAuthenticationMethods;

  private List<String> authorizationGrantTypes;

  private List<String> redirectUris;

  private List<String> postLogoutRedirectUris;

  private List<String> scopes;

  private Boolean requireAuthorizationConsent;

  private Boolean requireProofKey;

  private Boolean reuseRefreshTokens;

  private Duration accessTokenTimeToLive;

  private Duration authorizationCodeTimeToLive;

  private Duration refreshTokenTimeToLive;
}
