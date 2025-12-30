package tn.cyberious.compta.oauth2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.oauth2.dto.ClientResponse;
import tn.cyberious.compta.oauth2.dto.CreateClientRequest;
import tn.cyberious.compta.oauth2.dto.UpdateClientRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientManagementService {

  private final RegisteredClientRepository registeredClientRepository;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;

  @Transactional
  public ClientResponse createClient(CreateClientRequest request) {
    log.info("Creating new OAuth2 client with clientId: {}", request.getClientId());

    String id = UUID.randomUUID().toString();
    Instant issuedAt = Instant.now();

    RegisteredClient.Builder builder = RegisteredClient.withId(id)
        .clientId(request.getClientId())
        .clientIdIssuedAt(issuedAt)
        .clientName(request.getClientName());

    // Set client secret if provided
    if (request.getClientSecret() != null && !request.getClientSecret().isBlank()) {
      builder.clientSecret(passwordEncoder.encode(request.getClientSecret()));
    }

    // Set client authentication methods
    if (request.getClientAuthenticationMethods() != null) {
      request.getClientAuthenticationMethods().forEach(method ->
          builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method)));
    }

    // Set authorization grant types
    if (request.getAuthorizationGrantTypes() != null) {
      request.getAuthorizationGrantTypes().forEach(grantType ->
          builder.authorizationGrantType(new AuthorizationGrantType(grantType)));
    }

    // Set redirect URIs
    if (request.getRedirectUris() != null) {
      request.getRedirectUris().forEach(builder::redirectUri);
    }

    // Set post logout redirect URIs
    if (request.getPostLogoutRedirectUris() != null) {
      request.getPostLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);
    }

    // Set scopes
    if (request.getScopes() != null) {
      request.getScopes().forEach(builder::scope);
    }

    // Set client settings
    ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder();
    if (request.getRequireAuthorizationConsent() != null) {
      clientSettingsBuilder.requireAuthorizationConsent(request.getRequireAuthorizationConsent());
    }
    if (request.getRequireProofKey() != null) {
      clientSettingsBuilder.requireProofKey(request.getRequireProofKey());
    }
    builder.clientSettings(clientSettingsBuilder.build());

    // Set token settings
    TokenSettings.Builder tokenSettingsBuilder = TokenSettings.builder();
    if (request.getAccessTokenTimeToLive() != null) {
      tokenSettingsBuilder.accessTokenTimeToLive(request.getAccessTokenTimeToLive());
    }
    if (request.getAuthorizationCodeTimeToLive() != null) {
      tokenSettingsBuilder.authorizationCodeTimeToLive(request.getAuthorizationCodeTimeToLive());
    }
    if (request.getRefreshTokenTimeToLive() != null) {
      tokenSettingsBuilder.refreshTokenTimeToLive(request.getRefreshTokenTimeToLive());
    }
    if (request.getReuseRefreshTokens() != null) {
      tokenSettingsBuilder.reuseRefreshTokens(request.getReuseRefreshTokens());
    }
    builder.tokenSettings(tokenSettingsBuilder.build());

    RegisteredClient registeredClient = builder.build();
    registeredClientRepository.save(registeredClient);

    log.info("Successfully created OAuth2 client with clientId: {}", request.getClientId());
    return toClientResponse(registeredClient);
  }

  @Transactional(readOnly = true)
  public List<ClientResponse> getAllClients() {
    log.debug("Retrieving all OAuth2 clients");
    // Note: JdbcRegisteredClientRepository doesn't provide a findAll method
    // We'll need to query the database directly for this
    return List.of();
  }

  @Transactional(readOnly = true)
  public ClientResponse getClientById(String clientId) {
    log.debug("Retrieving OAuth2 client with clientId: {}", clientId);
    RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
    if (registeredClient == null) {
      throw new IllegalArgumentException("Client not found with clientId: " + clientId);
    }
    return toClientResponse(registeredClient);
  }

  @Transactional
  public ClientResponse updateClient(String clientId, UpdateClientRequest request) {
    log.info("Updating OAuth2 client with clientId: {}", clientId);

    RegisteredClient existingClient = registeredClientRepository.findByClientId(clientId);
    if (existingClient == null) {
      throw new IllegalArgumentException("Client not found with clientId: " + clientId);
    }

    // Build updated client
    RegisteredClient.Builder builder = RegisteredClient.withId(existingClient.getId())
        .clientId(existingClient.getClientId())
        .clientIdIssuedAt(existingClient.getClientIdIssuedAt())
        .clientName(request.getClientName() != null ? request.getClientName() : existingClient.getClientName());

    // Update client secret if provided
    if (request.getClientSecret() != null && !request.getClientSecret().isBlank()) {
      builder.clientSecret(passwordEncoder.encode(request.getClientSecret()));
    } else {
      builder.clientSecret(existingClient.getClientSecret());
    }

    // Update client authentication methods
    if (request.getClientAuthenticationMethods() != null && !request.getClientAuthenticationMethods().isEmpty()) {
      request.getClientAuthenticationMethods().forEach(method ->
          builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method)));
    } else {
      existingClient.getClientAuthenticationMethods().forEach(builder::clientAuthenticationMethod);
    }

    // Update authorization grant types
    if (request.getAuthorizationGrantTypes() != null && !request.getAuthorizationGrantTypes().isEmpty()) {
      request.getAuthorizationGrantTypes().forEach(grantType ->
          builder.authorizationGrantType(new AuthorizationGrantType(grantType)));
    } else {
      existingClient.getAuthorizationGrantTypes().forEach(builder::authorizationGrantType);
    }

    // Update redirect URIs
    if (request.getRedirectUris() != null && !request.getRedirectUris().isEmpty()) {
      request.getRedirectUris().forEach(builder::redirectUri);
    } else {
      existingClient.getRedirectUris().forEach(builder::redirectUri);
    }

    // Update post logout redirect URIs
    if (request.getPostLogoutRedirectUris() != null && !request.getPostLogoutRedirectUris().isEmpty()) {
      request.getPostLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);
    } else {
      existingClient.getPostLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);
    }

    // Update scopes
    if (request.getScopes() != null && !request.getScopes().isEmpty()) {
      request.getScopes().forEach(builder::scope);
    } else {
      existingClient.getScopes().forEach(builder::scope);
    }

    // Update client settings
    ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder();
    if (request.getRequireAuthorizationConsent() != null) {
      clientSettingsBuilder.requireAuthorizationConsent(request.getRequireAuthorizationConsent());
    } else {
      clientSettingsBuilder.requireAuthorizationConsent(
          existingClient.getClientSettings().isRequireAuthorizationConsent());
    }
    if (request.getRequireProofKey() != null) {
      clientSettingsBuilder.requireProofKey(request.getRequireProofKey());
    } else {
      clientSettingsBuilder.requireProofKey(
          existingClient.getClientSettings().isRequireProofKey());
    }
    builder.clientSettings(clientSettingsBuilder.build());

    // Update token settings
    TokenSettings.Builder tokenSettingsBuilder = TokenSettings.builder();
    if (request.getAccessTokenTimeToLive() != null) {
      tokenSettingsBuilder.accessTokenTimeToLive(request.getAccessTokenTimeToLive());
    } else {
      tokenSettingsBuilder.accessTokenTimeToLive(existingClient.getTokenSettings().getAccessTokenTimeToLive());
    }
    if (request.getAuthorizationCodeTimeToLive() != null) {
      tokenSettingsBuilder.authorizationCodeTimeToLive(request.getAuthorizationCodeTimeToLive());
    } else {
      tokenSettingsBuilder.authorizationCodeTimeToLive(existingClient.getTokenSettings().getAuthorizationCodeTimeToLive());
    }
    if (request.getRefreshTokenTimeToLive() != null) {
      tokenSettingsBuilder.refreshTokenTimeToLive(request.getRefreshTokenTimeToLive());
    } else {
      tokenSettingsBuilder.refreshTokenTimeToLive(existingClient.getTokenSettings().getRefreshTokenTimeToLive());
    }
    if (request.getReuseRefreshTokens() != null) {
      tokenSettingsBuilder.reuseRefreshTokens(request.getReuseRefreshTokens());
    } else {
      tokenSettingsBuilder.reuseRefreshTokens(existingClient.getTokenSettings().isReuseRefreshTokens());
    }
    builder.tokenSettings(tokenSettingsBuilder.build());

    RegisteredClient updatedClient = builder.build();
    registeredClientRepository.save(updatedClient);

    log.info("Successfully updated OAuth2 client with clientId: {}", clientId);
    return toClientResponse(updatedClient);
  }

  @Transactional
  public void deleteClient(String clientId) {
    log.info("Deleting OAuth2 client with clientId: {}", clientId);

    RegisteredClient existingClient = registeredClientRepository.findByClientId(clientId);
    if (existingClient == null) {
      throw new IllegalArgumentException("Client not found with clientId: " + clientId);
    }

    // JdbcRegisteredClientRepository doesn't have a delete method
    // We need to delete from the database directly
    throw new UnsupportedOperationException("Delete operation not yet implemented for JdbcRegisteredClientRepository");
  }

  @Transactional
  public String rotateClientSecret(String clientId) {
    log.info("Rotating client secret for clientId: {}", clientId);

    RegisteredClient existingClient = registeredClientRepository.findByClientId(clientId);
    if (existingClient == null) {
      throw new IllegalArgumentException("Client not found with clientId: " + clientId);
    }

    String newSecret = generateClientSecret();

    RegisteredClient updatedClient = RegisteredClient.withId(existingClient.getId())
        .clientId(existingClient.getClientId())
        .clientIdIssuedAt(existingClient.getClientIdIssuedAt())
        .clientSecret(passwordEncoder.encode(newSecret))
        .clientName(existingClient.getClientName())
        .clientAuthenticationMethods(existingClient.getClientAuthenticationMethods())
        .authorizationGrantTypes(existingClient.getAuthorizationGrantTypes())
        .redirectUris(existingClient.getRedirectUris())
        .postLogoutRedirectUris(existingClient.getPostLogoutRedirectUris())
        .scopes(existingClient.getScopes())
        .clientSettings(existingClient.getClientSettings())
        .tokenSettings(existingClient.getTokenSettings())
        .build();

    registeredClientRepository.save(updatedClient);

    log.info("Successfully rotated client secret for clientId: {}", clientId);
    return newSecret;
  }

  private String generateClientSecret() {
    return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
  }

  private ClientResponse toClientResponse(RegisteredClient registeredClient) {
    return ClientResponse.builder()
        .id(registeredClient.getId())
        .clientId(registeredClient.getClientId())
        .clientIdIssuedAt(LocalDateTime.ofInstant(
            registeredClient.getClientIdIssuedAt(), ZoneId.systemDefault()))
        .clientName(registeredClient.getClientName())
        .clientAuthenticationMethods(registeredClient.getClientAuthenticationMethods().stream()
            .map(ClientAuthenticationMethod::getValue)
            .toList())
        .authorizationGrantTypes(registeredClient.getAuthorizationGrantTypes().stream()
            .map(AuthorizationGrantType::getValue)
            .toList())
        .redirectUris(registeredClient.getRedirectUris().stream().toList())
        .postLogoutRedirectUris(registeredClient.getPostLogoutRedirectUris().stream().toList())
        .scopes(registeredClient.getScopes().stream().toList())
        .requireAuthorizationConsent(registeredClient.getClientSettings().isRequireAuthorizationConsent())
        .requireProofKey(registeredClient.getClientSettings().isRequireProofKey())
        .reuseRefreshTokens(registeredClient.getTokenSettings().isReuseRefreshTokens())
        .accessTokenTimeToLiveSeconds((int) registeredClient.getTokenSettings().getAccessTokenTimeToLive().getSeconds())
        .authorizationCodeTimeToLiveSeconds((int) registeredClient.getTokenSettings().getAuthorizationCodeTimeToLive().getSeconds())
        .refreshTokenTimeToLiveSeconds((int) registeredClient.getTokenSettings().getRefreshTokenTimeToLive().getSeconds())
        .build();
  }
}
