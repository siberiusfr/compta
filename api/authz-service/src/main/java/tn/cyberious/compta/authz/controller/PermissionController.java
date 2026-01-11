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
import tn.cyberious.compta.authz.dto.PermissionDto;
import tn.cyberious.compta.authz.dto.RolePermissionDto;
import tn.cyberious.compta.authz.dto.request.AssignPermissionToRoleRequest;
import tn.cyberious.compta.authz.dto.request.CreatePermissionRequest;
import tn.cyberious.compta.authz.service.PermissionService;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Gestion des permissions et des rôles")
public class PermissionController {

    private final PermissionService permissionService;

    // ===== Permission CRUD =====

    @PostMapping
    @Operation(summary = "Creer une permission", description = "Cree une nouvelle permission granulaire")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Permission creee avec succes"),
        @ApiResponse(responseCode = "400", description = "Donnees invalides ou code deja existant")
    })
    public ResponseEntity<PermissionDto> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        PermissionDto created = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/description")
    @Operation(summary = "Modifier la description", description = "Met a jour la description d'une permission")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Description mise a jour"),
        @ApiResponse(responseCode = "404", description = "Permission non trouvee")
    })
    public ResponseEntity<PermissionDto> updateDescription(
            @Parameter(description = "ID de la permission") @PathVariable Long id,
            @Parameter(description = "Nouvelle description") @RequestBody String description) {
        return ResponseEntity.ok(permissionService.updateDescription(id, description));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une permission", description = "Supprime une permission et ses associations aux rôles")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Permission supprimee"),
        @ApiResponse(responseCode = "404", description = "Permission non trouvee")
    })
    public ResponseEntity<Void> deletePermission(
            @Parameter(description = "ID de la permission") @PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une permission", description = "Recupere une permission par son ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Permission trouvee"),
        @ApiResponse(responseCode = "404", description = "Permission non trouvee")
    })
    public ResponseEntity<PermissionDto> findById(
            @Parameter(description = "ID de la permission") @PathVariable Long id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Recuperer par code", description = "Recupere une permission par son code unique")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Permission trouvee"),
        @ApiResponse(responseCode = "404", description = "Permission non trouvee")
    })
    public ResponseEntity<PermissionDto> findByCode(
            @Parameter(description = "Code de la permission") @PathVariable String code) {
        return ResponseEntity.ok(permissionService.findByCode(code));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les permissions", description = "Recupere la liste de toutes les permissions")
    @ApiResponse(responseCode = "200", description = "Liste des permissions")
    public ResponseEntity<List<PermissionDto>> findAll() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @GetMapping("/resource/{resource}")
    @Operation(summary = "Lister par ressource", description = "Recupere les permissions d'une ressource")
    @ApiResponse(responseCode = "200", description = "Liste des permissions de la ressource")
    public ResponseEntity<List<PermissionDto>> findByResource(
            @Parameter(description = "Nom de la ressource") @PathVariable String resource) {
        return ResponseEntity.ok(permissionService.findByResource(resource));
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Lister par action", description = "Recupere les permissions d'une action")
    @ApiResponse(responseCode = "200", description = "Liste des permissions de l'action")
    public ResponseEntity<List<PermissionDto>> findByAction(
            @Parameter(description = "Nom de l'action") @PathVariable String action) {
        return ResponseEntity.ok(permissionService.findByAction(action));
    }

    @GetMapping("/resources")
    @Operation(summary = "Lister les ressources", description = "Recupere la liste des ressources distinctes")
    @ApiResponse(responseCode = "200", description = "Liste des ressources")
    public ResponseEntity<List<String>> findDistinctResources() {
        return ResponseEntity.ok(permissionService.findDistinctResources());
    }

    @GetMapping("/actions")
    @Operation(summary = "Lister les actions", description = "Recupere la liste des actions distinctes")
    @ApiResponse(responseCode = "200", description = "Liste des actions")
    public ResponseEntity<List<String>> findDistinctActions() {
        return ResponseEntity.ok(permissionService.findDistinctActions());
    }

    // ===== Role-Permission Management =====

    @PostMapping("/role-assignment")
    @Operation(summary = "Assigner une permission a un rôle", description = "Associe une permission a un rôle")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Permission assignee avec succes"),
        @ApiResponse(responseCode = "400", description = "Permission deja assignee ou donnees invalides")
    })
    public ResponseEntity<RolePermissionDto> assignPermissionToRole(
            @Valid @RequestBody AssignPermissionToRoleRequest request) {
        RolePermissionDto created = permissionService.assignPermissionToRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/role/{role}/permission/{permissionId}")
    @Operation(summary = "Revoquer une permission", description = "Revoque une permission d'un rôle")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Permission revoquee"),
        @ApiResponse(responseCode = "404", description = "Association non trouvee")
    })
    public ResponseEntity<Void> revokePermissionFromRole(
            @Parameter(description = "Nom du rôle") @PathVariable String role,
            @Parameter(description = "ID de la permission") @PathVariable Long permissionId) {
        permissionService.revokePermissionFromRole(role, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Lister les permissions d'un rôle", description = "Recupere toutes les permissions associees a un rôle")
    @ApiResponse(responseCode = "200", description = "Liste des permissions du rôle")
    public ResponseEntity<List<PermissionDto>> findPermissionsByRole(
            @Parameter(description = "Nom du rôle") @PathVariable String role) {
        return ResponseEntity.ok(permissionService.findPermissionsByRole(role));
    }

    @GetMapping("/role/{role}/assignments")
    @Operation(summary = "Lister les assignations d'un rôle", description = "Recupere les assignations role-permission d'un rôle")
    @ApiResponse(responseCode = "200", description = "Liste des assignations")
    public ResponseEntity<List<RolePermissionDto>> findRolePermissionsByRole(
            @Parameter(description = "Nom du rôle") @PathVariable String role) {
        return ResponseEntity.ok(permissionService.findRolePermissionsByRole(role));
    }

    @GetMapping("/roles")
    @Operation(summary = "Lister les rôles", description = "Recupere la liste des rôles ayant des permissions")
    @ApiResponse(responseCode = "200", description = "Liste des rôles")
    public ResponseEntity<List<String>> findDistinctRoles() {
        return ResponseEntity.ok(permissionService.findDistinctRoles());
    }

    @GetMapping("/check")
    @Operation(summary = "Verifier une permission", description = "Verifie si un rôle possede une permission")
    @ApiResponse(responseCode = "200", description = "Resultat de la verification")
    public ResponseEntity<Boolean> hasPermission(
            @Parameter(description = "Nom du rôle") @RequestParam String role,
            @Parameter(description = "Code de la permission") @RequestParam String permissionCode) {
        return ResponseEntity.ok(permissionService.hasPermission(role, permissionCode));
    }

    @GetMapping("/check/resource")
    @Operation(summary = "Verifier l'acces ressource", description = "Verifie si un rôle a acces a une ressource avec une action")
    @ApiResponse(responseCode = "200", description = "Resultat de la verification")
    public ResponseEntity<Boolean> hasPermissionOnResource(
            @Parameter(description = "Nom du rôle") @RequestParam String role,
            @Parameter(description = "Nom de la ressource") @RequestParam String resource,
            @Parameter(description = "Nom de l'action") @RequestParam String action) {
        return ResponseEntity.ok(permissionService.hasPermissionOnResource(role, resource, action));
    }
}
