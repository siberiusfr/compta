package tn.cyberious.compta.authz.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.authz.dto.ComptableSocietesDto;
import tn.cyberious.compta.authz.dto.SocieteDto;
import tn.cyberious.compta.authz.dto.request.AssignComptableToSocieteRequest;
import tn.cyberious.compta.authz.dto.request.UpdateComptableSocieteAccessRequest;
import tn.cyberious.compta.authz.service.ComptableSocietesService;

@RestController
@RequestMapping("/api/comptable-societes")
@RequiredArgsConstructor
@Tag(
    name = "Acces Comptables",
    description = "Gestion des acces des comptables aux societes clientes")
public class ComptableSocietesController {

  private final ComptableSocietesService comptableSocietesService;

  @PostMapping
  @Operation(
      summary = "Assigner un comptable a une societe",
      description = "Donne acces a un comptable sur une societe cliente")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Acces cree avec succes"),
    @ApiResponse(responseCode = "400", description = "Donnees invalides ou acces deja existant")
  })
  public ResponseEntity<ComptableSocietesDto> assignComptableToSociete(
      @Valid @RequestBody AssignComptableToSocieteRequest request) {
    ComptableSocietesDto created = comptableSocietesService.assignComptableToSociete(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Modifier les droits d'acces",
      description = "Met a jour les droits d'un comptable sur une societe")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Droits mis a jour"),
    @ApiResponse(responseCode = "404", description = "Acces non trouve")
  })
  public ResponseEntity<ComptableSocietesDto> updateAccess(
      @Parameter(description = "ID de l'acces") @PathVariable Long id,
      @Valid @RequestBody UpdateComptableSocieteAccessRequest request) {
    return ResponseEntity.ok(comptableSocietesService.updateAccess(id, request));
  }

  @DeleteMapping("/user/{userId}/societe/{societeId}")
  @Operation(
      summary = "Revoquer l'acces",
      description = "Revoque l'acces d'un comptable a une societe")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Acces revoque"),
    @ApiResponse(responseCode = "404", description = "Acces non trouve")
  })
  public ResponseEntity<Void> revokeAccess(
      @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId,
      @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
    comptableSocietesService.revokeAccess(userId, societeId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Recuperer un acces",
      description = "Recupere les details d'un acces par son ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Acces trouve"),
    @ApiResponse(responseCode = "404", description = "Acces non trouve")
  })
  public ResponseEntity<ComptableSocietesDto> findById(
      @Parameter(description = "ID de l'acces") @PathVariable Long id) {
    return ResponseEntity.ok(comptableSocietesService.findById(id));
  }

  @GetMapping("/user/{userId}/societe/{societeId}")
  @Operation(
      summary = "Recuperer l'acces utilisateur-societe",
      description = "Recupere l'acces d'un utilisateur sur une societe")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Acces trouve"),
    @ApiResponse(responseCode = "404", description = "Acces non trouve")
  })
  public ResponseEntity<ComptableSocietesDto> findByUserIdAndSocieteId(
      @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId,
      @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
    return ResponseEntity.ok(comptableSocietesService.findByUserIdAndSocieteId(userId, societeId));
  }

  @GetMapping("/user/{userId}")
  @Operation(
      summary = "Lister les acces d'un utilisateur",
      description = "Recupere tous les acces d'un comptable")
  @ApiResponse(responseCode = "200", description = "Liste des acces")
  public ResponseEntity<List<ComptableSocietesDto>> findByUserId(
      @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
    return ResponseEntity.ok(comptableSocietesService.findByUserId(userId));
  }

  @GetMapping("/user/{userId}/active")
  @Operation(
      summary = "Lister les acces actifs",
      description = "Recupere les acces actifs d'un comptable")
  @ApiResponse(responseCode = "200", description = "Liste des acces actifs")
  public ResponseEntity<List<ComptableSocietesDto>> findActiveByUserId(
      @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
    return ResponseEntity.ok(comptableSocietesService.findActiveByUserId(userId));
  }

  @GetMapping("/societe/{societeId}")
  @Operation(
      summary = "Lister les comptables d'une societe",
      description = "Recupere tous les comptables ayant acces a une societe")
  @ApiResponse(responseCode = "200", description = "Liste des acces")
  public ResponseEntity<List<ComptableSocietesDto>> findBySocieteId(
      @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
    return ResponseEntity.ok(comptableSocietesService.findBySocieteId(societeId));
  }

  @GetMapping("/user/{userId}/societes")
  @Operation(
      summary = "Lister les societes accessibles",
      description = "Recupere les societes auxquelles un comptable a acces")
  @ApiResponse(responseCode = "200", description = "Liste des societes")
  public ResponseEntity<List<SocieteDto>> findSocietesByUserId(
      @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
    return ResponseEntity.ok(comptableSocietesService.findSocietesByUserId(userId));
  }

  @GetMapping("/user/{userId}/societes/write")
  @Operation(
      summary = "Lister les societes en ecriture",
      description = "Recupere les societes où le comptable a le droit d'ecriture")
  @ApiResponse(responseCode = "200", description = "Liste des societes avec droit d'ecriture")
  public ResponseEntity<List<SocieteDto>> findSocietesWithWriteAccessByUserId(
      @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
    return ResponseEntity.ok(comptableSocietesService.findSocietesWithWriteAccessByUserId(userId));
  }

  @GetMapping("/check/access")
  @Operation(
      summary = "Verifier l'acces",
      description = "Verifie si un comptable a acces a une societe")
  @ApiResponse(responseCode = "200", description = "Resultat de la verification")
  public ResponseEntity<Boolean> hasAccess(
      @Parameter(description = "ID de l'utilisateur") @RequestParam Long userId,
      @Parameter(description = "ID de la societe") @RequestParam Long societeId) {
    return ResponseEntity.ok(comptableSocietesService.hasAccess(userId, societeId));
  }

  @GetMapping("/check/write")
  @Operation(
      summary = "Verifier le droit d'ecriture",
      description = "Verifie si un comptable a le droit d'ecriture sur une societe")
  @ApiResponse(responseCode = "200", description = "Resultat de la verification")
  public ResponseEntity<Boolean> hasWriteAccess(
      @Parameter(description = "ID de l'utilisateur") @RequestParam Long userId,
      @Parameter(description = "ID de la societe") @RequestParam Long societeId) {
    return ResponseEntity.ok(comptableSocietesService.hasWriteAccess(userId, societeId));
  }

  @GetMapping("/check/validate")
  @Operation(
      summary = "Verifier le droit de validation",
      description = "Verifie si un comptable a le droit de validation sur une societe")
  @ApiResponse(responseCode = "200", description = "Resultat de la verification")
  public ResponseEntity<Boolean> hasValidateAccess(
      @Parameter(description = "ID de l'utilisateur") @RequestParam Long userId,
      @Parameter(description = "ID de la societe") @RequestParam Long societeId) {
    return ResponseEntity.ok(comptableSocietesService.hasValidateAccess(userId, societeId));
  }
}
