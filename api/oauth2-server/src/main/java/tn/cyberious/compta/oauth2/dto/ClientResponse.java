package tn.cyberious.compta.oauth2.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

  private String id;
  private String clientId;
  private LocalDateTime clientIdIssuedAt;
  private String clientName;
  private List<String> clientAuthenticationMethods;
  private List<String> authorizationGrantTypes;
  private List<String> redirectUris;
  private List<String> postLogoutRedirectUris;
  private List<String> scopes;
  private Boolean requireAuthorizationConsent;
  private Boolean requireProofKey;
  private Boolean reuseRefreshTokens;
  private Integer accessTokenTimeToLiveSeconds;
  private Integer authorizationCodeTimeToLiveSeconds;
  private Integer refreshTokenTimeToLiveSeconds;
  private LocalDateTime clientSecretExpiresAt;
}
