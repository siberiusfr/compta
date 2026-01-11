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
import tn.cyberious.compta.authz.dto.UserSocieteComptableDto;
import tn.cyberious.compta.authz.dto.request.AssignUserToSocieteComptableRequest;
import tn.cyberious.compta.authz.service.UserSocieteComptableService;

@RestController
@RequestMapping("/api/user-societe-comptable")
@RequiredArgsConstructor
@Tag(
    name = "Utilisateurs Cabinet",
    description = "Gestion des utilisateurs appartenant aux cabinets comptables")
public class UserSocieteComptableController {

  private final UserSocieteComptableService userSocieteComptableService;

  @PostMapping
  @Operation(
      summary = "Assigner un utilisateur a un cabinet",
      description = "Assigne un utilisateur a une societe comptable avec un rôle")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Utilisateur assigne avec succes"),
    @ApiResponse(
        responseCode = "400",
        description = "Donnees invalides ou utilisateur deja assigne")
  })
  public ResponseEntity<UserSocieteComptableDto> assignUser(
      @Valid @RequestBody AssignUserToSocieteComptableRequest request) {
    UserSocieteComptableDto created = userSocieteComptableService.assignUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}/role")
  @Operation(
      summary = "Modifier le rôle d'un utilisateur",
      description = "Change le rôle d'un utilisateur dans son cabinet")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Rôle mis a jour"),
    @ApiResponse(responseCode = "404", description = "Assignation non trouvee"),
    @ApiResponse(responseCode = "400", description = "Rôle invalide ou contrainte MANAGER violee")
  })
  public ResponseEntity<UserSocieteComptableDto> updateRole(
      @Parameter(description = "ID de l'assignation") @PathVariable Long id,
      @Parameter(description = "Nouveau rôle (MANAGER, COMPTABLE, ASSISTANT)") @RequestParam
          String role) {
    return ResponseEntity.ok(userSocieteComptableService.updateRole(id, role));
  }

  @PutMapping("/{id}/deactivate")
  @Operation(
      summary = "Desactiver une assignation",
      description = "Desactive l'assignation d'un utilisateur a son cabinet")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Assignation desactivee"),
    @ApiResponse(responseCode = "404", description = "Assignation non trouvee")
  })
  public ResponseEntity<Void> deactivate(
      @Parameter(description = "ID de l'assignation") @PathVariable Long id) {
    userSocieteComptableService.deactivate(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Supprimer une assignation",
      description = "Supprime definitivement l'assignation d'un utilisateur")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Assignation supprimee"),
    @ApiResponse(responseCode = "404", description = "Assignation non trouvee")
  })
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID de l'assignation") @PathVariable Long id) {
    userSocieteComptableService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Recuperer une assignation",
      description = "Recupere les details d'une assignation par son ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Assignation trouvee"),
    @ApiResponse(responseCode = "404", description = "Assignation non trouvee")
  })
  public ResponseEntity<UserSocieteComptableDto> findById(
      @Parameter(description = "ID de l'assignation") @PathVariable Long id) {
    return ResponseEntity.ok(userSocieteComptableService.findById(id));
  }

  @GetMapping("/user/{userId}")
  @Operation(
      summary = "Recuperer par utilisateur",
      description = "Recupere l'assignation d'un utilisateur a son cabinet")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Assignation trouvee"),
    @ApiResponse(responseCode = "404", description = "Aucune assignation trouvee")
  })
  public ResponseEntity<UserSocieteComptableDto> findByUserId(
      @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
    return ResponseEntity.ok(userSocieteComptableService.findByUserId(userId));
  }

  @GetMapping("/user/{userId}/active")
  @Operation(
      summary = "Recuperer l'assignation active",
      description = "Recupere l'assignation active d'un utilisateur")
  @ApiResponse(responseCode = "200", description = "Assignation active ou null si aucune")
  public ResponseEntity<UserSocieteComptableDto> findActiveByUserId(
      @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
    return ResponseEntity.ok(userSocieteComptableService.findActiveByUserId(userId));
  }

  @GetMapping("/cabinet/{societeComptableId}")
  @Operation(
      summary = "Lister par cabinet",
      description = "Recupere tous les utilisateurs d'un cabinet comptable")
  @ApiResponse(responseCode = "200", description = "Liste des utilisateurs du cabinet")
  public ResponseEntity<List<UserSocieteComptableDto>> findBySocieteComptableId(
      @Parameter(description = "ID de la societe comptable") @PathVariable
          Long societeComptableId) {
    return ResponseEntity.ok(
        userSocieteComptableService.findBySocieteComptableId(societeComptableId));
  }

  @GetMapping("/cabinet/{societeComptableId}/active")
  @Operation(
      summary = "Lister les actifs par cabinet",
      description = "Recupere les utilisateurs actifs d'un cabinet comptable")
  @ApiResponse(responseCode = "200", description = "Liste des utilisateurs actifs")
  public ResponseEntity<List<UserSocieteComptableDto>> findActiveBySocieteComptableId(
      @Parameter(description = "ID de la societe comptable") @PathVariable
          Long societeComptableId) {
    return ResponseEntity.ok(
        userSocieteComptableService.findActiveBySocieteComptableId(societeComptableId));
  }

  @GetMapping("/cabinet/{societeComptableId}/manager")
  @Operation(
      summary = "Recuperer le manager",
      description = "Recupere le manager d'un cabinet comptable")
  @ApiResponse(responseCode = "200", description = "Manager ou null si aucun")
  public ResponseEntity<UserSocieteComptableDto> findManagerBySocieteComptableId(
      @Parameter(description = "ID de la societe comptable") @PathVariable
          Long societeComptableId) {
    return ResponseEntity.ok(
        userSocieteComptableService.findManagerBySocieteComptableId(societeComptableId));
  }
}
