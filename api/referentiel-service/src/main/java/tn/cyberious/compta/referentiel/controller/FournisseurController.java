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
import tn.cyberious.compta.referentiel.dto.FournisseurRequest;
import tn.cyberious.compta.referentiel.dto.FournisseurResponse;
import tn.cyberious.compta.referentiel.service.FournisseurService;

@Slf4j
@RestController
@RequestMapping("/api/companies/{companyId}/fournisseurs")
@RequiredArgsConstructor
@Tag(name = "Fournisseurs", description = "Gestion des fournisseurs")
@SecurityRequirement(name = "bearer-jwt")
public class FournisseurController {

  private final FournisseurService fournisseurService;

  @PostMapping
  @Operation(
      summary = "Créer un fournisseur",
      description = "Crée un nouveau fournisseur pour l'entreprise spécifiée")
  public ResponseEntity<FournisseurResponse> create(
      @PathVariable Long companyId, @Valid @RequestBody FournisseurRequest request) {

    log.info("Creating fournisseur for company: {}", companyId);
    FournisseurResponse response = fournisseurService.create(request, companyId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Mettre à jour un fournisseur",
      description = "Met à jour un fournisseur existant")
  public ResponseEntity<FournisseurResponse> update(
      @PathVariable Long companyId,
      @PathVariable Long id,
      @Valid @RequestBody FournisseurRequest request) {

    log.info("Updating fournisseur {} for company: {}", id, companyId);
    FournisseurResponse response = fournisseurService.update(id, request, companyId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Supprimer un fournisseur", description = "Supprime un fournisseur")
  public ResponseEntity<Void> delete(@PathVariable Long companyId, @PathVariable Long id) {

    log.info("Deleting fournisseur {} for company: {}", id, companyId);
    fournisseurService.delete(id, companyId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Récupérer un fournisseur",
      description = "Récupère un fournisseur par son ID")
  public ResponseEntity<FournisseurResponse> getById(
      @PathVariable Long companyId, @PathVariable Long id) {

    log.info("Getting fournisseur {} for company: {}", id, companyId);
    FournisseurResponse response = fournisseurService.getById(id, companyId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Lister tous les fournisseurs",
      description = "Récupère tous les fournisseurs de l'entreprise")
  public ResponseEntity<List<FournisseurResponse>> getAll(@PathVariable Long companyId) {

    log.info("Getting all fournisseurs for company: {}", companyId);
    List<FournisseurResponse> responses = fournisseurService.getAllByCompany(companyId);
    return ResponseEntity.ok(responses);
  }
}
