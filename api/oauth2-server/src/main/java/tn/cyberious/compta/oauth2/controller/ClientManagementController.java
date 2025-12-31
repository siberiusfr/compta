package tn.cyberious.compta.oauth2.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.oauth2.dto.ClientResponse;
import tn.cyberious.compta.oauth2.dto.CreateClientRequest;
import tn.cyberious.compta.oauth2.dto.UpdateClientRequest;
import tn.cyberious.compta.oauth2.service.ClientManagementService;

@Tag(name = "Client Management", description = "OAuth2 client management endpoints")
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ClientManagementController {

  private final ClientManagementService clientManagementService;

  @Operation(
      summary = "Create a new OAuth2 client",
      description = "Create a new OAuth2 registered client")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ClientResponse> createClient(
      @Valid @RequestBody CreateClientRequest request) {
    ClientResponse response = clientManagementService.createClient(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      summary = "Get all OAuth2 clients",
      description = "Retrieve all registered OAuth2 clients")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<?> getAllClients() {
    return ResponseEntity.ok(clientManagementService.getAllClients());
  }

  @Operation(
      summary = "Get OAuth2 client by ID",
      description = "Retrieve a specific OAuth2 client by its client ID")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{clientId}")
  public ResponseEntity<ClientResponse> getClientById(
      @Parameter(description = "Client ID") @PathVariable String clientId) {
    ClientResponse response = clientManagementService.getClientById(clientId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update OAuth2 client", description = "Update an existing OAuth2 client")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{clientId}")
  public ResponseEntity<ClientResponse> updateClient(
      @Parameter(description = "Client ID") @PathVariable String clientId,
      @Valid @RequestBody UpdateClientRequest request) {
    ClientResponse response = clientManagementService.updateClient(clientId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete OAuth2 client", description = "Delete an OAuth2 client")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{clientId}")
  public ResponseEntity<Void> deleteClient(
      @Parameter(description = "Client ID") @PathVariable String clientId) {
    clientManagementService.deleteClient(clientId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Rotate client secret",
      description = "Generate a new client secret for an OAuth2 client")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{clientId}/secret")
  public ResponseEntity<Map<String, String>> rotateClientSecret(
      @Parameter(description = "Client ID") @PathVariable String clientId) {
    String newSecret = clientManagementService.rotateClientSecret(clientId);
    return ResponseEntity.ok(Map.of("clientSecret", newSecret));
  }
}
