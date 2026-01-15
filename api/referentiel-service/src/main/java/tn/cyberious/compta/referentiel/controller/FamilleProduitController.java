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
import tn.cyberious.compta.referentiel.dto.FamilleProduitRequest;
import tn.cyberious.compta.referentiel.dto.FamilleProduitResponse;
import tn.cyberious.compta.referentiel.service.FamilleProduitService;

@Slf4j
@RestController
@RequestMapping("/api/companies/{companyId}/familles-produits")
@RequiredArgsConstructor
@Tag(name = "Familles Produits", description = "Gestion des familles de produits")
@SecurityRequirement(name = "bearer-jwt")
public class FamilleProduitController {

  private final FamilleProduitService familleProduitService;

  @PostMapping
  @Operation(
      summary = "Créer une famille produit",
      description = "Crée une nouvelle famille de produits pour l'entreprise spécifiée")
  public ResponseEntity<FamilleProduitResponse> create(
      @PathVariable Long companyId, @Valid @RequestBody FamilleProduitRequest request) {

    log.info("Creating famille produit for company: {}", companyId);
    FamilleProduitResponse response = familleProduitService.create(request, companyId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Mettre à jour une famille produit",
      description = "Met à jour une famille de produits existante")
  public ResponseEntity<FamilleProduitResponse> update(
      @PathVariable Long companyId,
      @PathVariable Long id,
      @Valid @RequestBody FamilleProduitRequest request) {

    log.info("Updating famille produit {} for company: {}", id, companyId);
    FamilleProduitResponse response = familleProduitService.update(id, request, companyId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Supprimer une famille produit",
      description = "Supprime une famille de produits")
  public ResponseEntity<Void> delete(@PathVariable Long companyId, @PathVariable Long id) {

    log.info("Deleting famille produit {} for company: {}", id, companyId);
    familleProduitService.delete(id, companyId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Récupérer une famille produit",
      description = "Récupère une famille de produits par son ID")
  public ResponseEntity<FamilleProduitResponse> getById(
      @PathVariable Long companyId, @PathVariable Long id) {

    log.info("Getting famille produit {} for company: {}", id, companyId);
    FamilleProduitResponse response = familleProduitService.getById(id, companyId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Lister toutes les familles produits",
      description = "Récupère toutes les familles de produits de l'entreprise")
  public ResponseEntity<List<FamilleProduitResponse>> getAll(@PathVariable Long companyId) {

    log.info("Getting all familles produits for company: {}", companyId);
    List<FamilleProduitResponse> responses = familleProduitService.getAllByCompany(companyId);
    return ResponseEntity.ok(responses);
  }
}
