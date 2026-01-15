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
import tn.cyberious.compta.referentiel.dto.ProduitRequest;
import tn.cyberious.compta.referentiel.dto.ProduitResponse;
import tn.cyberious.compta.referentiel.service.ProduitService;

@Slf4j
@RestController
@RequestMapping("/api/companies/{companyId}/produits")
@RequiredArgsConstructor
@Tag(name = "Produits", description = "Gestion des produits et services")
@SecurityRequirement(name = "bearer-jwt")
public class ProduitController {

  private final ProduitService produitService;

  @PostMapping
  @Operation(
      summary = "Créer un produit",
      description = "Crée un nouveau produit pour l'entreprise spécifiée")
  public ResponseEntity<ProduitResponse> create(
      @PathVariable Long companyId, @Valid @RequestBody ProduitRequest request) {

    log.info("Creating produit for company: {}", companyId);
    ProduitResponse response = produitService.create(request, companyId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Mettre à jour un produit", description = "Met à jour un produit existant")
  public ResponseEntity<ProduitResponse> update(
      @PathVariable Long companyId,
      @PathVariable Long id,
      @Valid @RequestBody ProduitRequest request) {

    log.info("Updating produit {} for company: {}", id, companyId);
    ProduitResponse response = produitService.update(id, request, companyId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Supprimer un produit", description = "Supprime un produit")
  public ResponseEntity<Void> delete(@PathVariable Long companyId, @PathVariable Long id) {

    log.info("Deleting produit {} for company: {}", id, companyId);
    produitService.delete(id, companyId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Récupérer un produit", description = "Récupère un produit par son ID")
  public ResponseEntity<ProduitResponse> getById(
      @PathVariable Long companyId, @PathVariable Long id) {

    log.info("Getting produit {} for company: {}", id, companyId);
    ProduitResponse response = produitService.getById(id, companyId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Lister tous les produits",
      description = "Récupère tous les produits de l'entreprise")
  public ResponseEntity<List<ProduitResponse>> getAll(@PathVariable Long companyId) {

    log.info("Getting all produits for company: {}", companyId);
    List<ProduitResponse> responses = produitService.getAllByCompany(companyId);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/famille/{familleId}")
  @Operation(
      summary = "Lister les produits par famille",
      description = "Récupère tous les produits d'une famille")
  public ResponseEntity<List<ProduitResponse>> getByFamille(
      @PathVariable Long companyId, @PathVariable Long familleId) {

    log.info("Getting produits by famille {} for company: {}", familleId, companyId);
    List<ProduitResponse> responses = produitService.getByFamilleAndCompany(familleId, companyId);
    return ResponseEntity.ok(responses);
  }
}
