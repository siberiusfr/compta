package tn.cyberious.compta.authz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.cyberious.compta.authz.dto.SocieteDto;
import tn.cyberious.compta.authz.dto.UserSocietesDto;
import tn.cyberious.compta.authz.dto.request.AssignUserToSocieteRequest;
import tn.cyberious.compta.authz.service.UserSocietesService;

@RestController
@RequestMapping("/api/user-societes")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs Societe", description = "Gestion des utilisateurs appartenant aux societes clientes")
public class UserSocietesController {

    private final UserSocietesService userSocietesService;

    @PostMapping
    @Operation(summary = "Assigner un utilisateur a une societe", description = "Assigne un utilisateur a une societe cliente avec un rôle")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utilisateur assigne avec succes"),
        @ApiResponse(responseCode = "400", description = "Donnees invalides ou utilisateur deja assigne")
    })
    public ResponseEntity<UserSocietesDto> assignUser(
            @Valid @RequestBody AssignUserToSocieteRequest request) {
        UserSocietesDto created = userSocietesService.assignUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Modifier le rôle d'un utilisateur", description = "Change le rôle d'un utilisateur dans sa societe")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rôle mis a jour"),
        @ApiResponse(responseCode = "404", description = "Assignation non trouvee"),
        @ApiResponse(responseCode = "400", description = "Rôle invalide ou contrainte MANAGER violee")
    })
    public ResponseEntity<UserSocietesDto> updateRole(
            @Parameter(description = "ID de l'assignation") @PathVariable Long id,
            @Parameter(description = "Nouveau rôle (MANAGER, FINANCE, VIEWER)") @RequestParam String role) {
        return ResponseEntity.ok(userSocietesService.updateRole(id, role));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Desactiver une assignation", description = "Desactive l'assignation d'un utilisateur a sa societe")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Assignation desactivee"),
        @ApiResponse(responseCode = "404", description = "Assignation non trouvee")
    })
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "ID de l'assignation") @PathVariable Long id) {
        userSocietesService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une assignation", description = "Supprime definitivement l'assignation d'un utilisateur")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Assignation supprimee"),
        @ApiResponse(responseCode = "404", description = "Assignation non trouvee")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de l'assignation") @PathVariable Long id) {
        userSocietesService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une assignation", description = "Recupere les details d'une assignation par son ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Assignation trouvee"),
        @ApiResponse(responseCode = "404", description = "Assignation non trouvee")
    })
    public ResponseEntity<UserSocietesDto> findById(
            @Parameter(description = "ID de l'assignation") @PathVariable Long id) {
        return ResponseEntity.ok(userSocietesService.findById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Recuperer par utilisateur", description = "Recupere l'assignation d'un utilisateur a sa societe")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Assignation trouvee"),
        @ApiResponse(responseCode = "404", description = "Aucune assignation trouvee")
    })
    public ResponseEntity<UserSocietesDto> findByUserId(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
        return ResponseEntity.ok(userSocietesService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Recuperer l'assignation active", description = "Recupere l'assignation active d'un utilisateur")
    @ApiResponse(responseCode = "200", description = "Assignation active ou null si aucune")
    public ResponseEntity<UserSocietesDto> findActiveByUserId(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
        return ResponseEntity.ok(userSocietesService.findActiveByUserId(userId));
    }

    @GetMapping("/societe/{societeId}")
    @Operation(summary = "Lister par societe", description = "Recupere tous les utilisateurs d'une societe cliente")
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs de la societe")
    public ResponseEntity<List<UserSocietesDto>> findBySocieteId(
            @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
        return ResponseEntity.ok(userSocietesService.findBySocieteId(societeId));
    }

    @GetMapping("/societe/{societeId}/active")
    @Operation(summary = "Lister les actifs par societe", description = "Recupere les utilisateurs actifs d'une societe cliente")
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs actifs")
    public ResponseEntity<List<UserSocietesDto>> findActiveBySocieteId(
            @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
        return ResponseEntity.ok(userSocietesService.findActiveBySocieteId(societeId));
    }

    @GetMapping("/societe/{societeId}/manager")
    @Operation(summary = "Recuperer le manager", description = "Recupere le manager d'une societe cliente")
    @ApiResponse(responseCode = "200", description = "Manager ou null si aucun")
    public ResponseEntity<UserSocietesDto> findManagerBySocieteId(
            @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
        return ResponseEntity.ok(userSocietesService.findManagerBySocieteId(societeId));
    }

    @GetMapping("/user/{userId}/societe")
    @Operation(summary = "Recuperer la societe de l'utilisateur", description = "Recupere la societe a laquelle appartient l'utilisateur")
    @ApiResponse(responseCode = "200", description = "Societe ou null si aucune")
    public ResponseEntity<SocieteDto> findSocieteByUserId(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
        return ResponseEntity.ok(userSocietesService.findSocieteByUserId(userId));
    }
}
