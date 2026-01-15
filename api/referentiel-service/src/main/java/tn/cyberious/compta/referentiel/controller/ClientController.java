package tn.cyberious.compta.referentiel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.cyberious.compta.referentiel.dto.ClientRequest;
import tn.cyberious.compta.referentiel.dto.ClientResponse;
import tn.cyberious.compta.referentiel.service.ClientService;

@Slf4j
@RestController
@RequestMapping("/api/companies/{companyId}/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestion des clients")
@SecurityRequirement(name = "bearer-jwt")
public class ClientController {

  private final ClientService clientService;

  @PostMapping
  @Operation(
      summary = "Créer un client",
      description = "Crée un nouveau client pour l'entreprise spécifiée")
  public ResponseEntity<ClientResponse> create(
      @PathVariable Long companyId, @Valid @RequestBody ClientRequest request) {

    log.info("Creating client for company: {}", companyId);
    ClientResponse response = clientService.create(request, companyId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Mettre à jour un client", description = "Met à jour un client existant")
  public ResponseEntity<ClientResponse> update(
      @PathVariable Long companyId,
      @PathVariable Long id,
      @Valid @RequestBody ClientRequest request) {

    log.info("Updating client {} for company: {}", id, companyId);
    ClientResponse response = clientService.update(id, request, companyId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Supprimer un client", description = "Supprime un client")
  public ResponseEntity<Void> delete(@PathVariable Long companyId, @PathVariable Long id) {

    log.info("Deleting client {} for company: {}", id, companyId);
    clientService.delete(id, companyId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Récupérer un client", description = "Récupère un client par son ID")
  public ResponseEntity<ClientResponse> getById(
      @PathVariable Long companyId, @PathVariable Long id) {

    log.info("Getting client {} for company: {}", id, companyId);
    ClientResponse response = clientService.getById(id, companyId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Lister tous les clients",
      description = "Récupère tous les clients de l'entreprise")
  public ResponseEntity<List<ClientResponse>> getAll(@PathVariable Long companyId) {

    log.info("Getting all clients for company: {}", companyId);
    List<ClientResponse> responses = clientService.getAllByCompany(companyId);
    return ResponseEntity.ok(responses);
  }
}
