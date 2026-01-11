package tn.cyberious.compta.authz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.cyberious.compta.authz.dto.SocieteAccessDto;
import tn.cyberious.compta.authz.dto.UserAccessDto;
import tn.cyberious.compta.authz.service.AccessService;

@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
@Tag(name = "Acces Unifie", description = "Verification unifiee des acces utilisateur (comptable ou membre)")
public class AccessController {

    private final AccessService accessService;

    @GetMapping("/user/{userId}/societe/{societeId}")
    @Operation(
            summary = "Obtenir les details d'acces",
            description = "Retourne les details complets d'acces d'un utilisateur a une societe. "
                    + "L'acces peut etre de type COMPTABLE (via comptable_societes) ou MEMBRE (via user_societes).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Details d'acces retournes")
    })
    public ResponseEntity<UserAccessDto> getUserAccess(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId,
            @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
        return ResponseEntity.ok(accessService.getUserAccess(userId, societeId));
    }

    @GetMapping("/check")
    @Operation(
            summary = "Verifier l'acces rapidement",
            description = "Verifie rapidement si un utilisateur a acces a une societe (comptable OU membre)")
    @ApiResponse(responseCode = "200", description = "Resultat de la verification")
    public ResponseEntity<Boolean> hasAccess(
            @Parameter(description = "ID de l'utilisateur") @RequestParam Long userId,
            @Parameter(description = "ID de la societe") @RequestParam Long societeId) {
        return ResponseEntity.ok(accessService.hasAccess(userId, societeId));
    }

    @GetMapping("/check/write")
    @Operation(
            summary = "Verifier le droit d'ecriture",
            description = "Verifie si un utilisateur a le droit d'ecriture sur une societe. "
                    + "Les membres de la societe ont toujours ce droit, les comptables selon leur configuration.")
    @ApiResponse(responseCode = "200", description = "Resultat de la verification")
    public ResponseEntity<Boolean> hasWriteAccess(
            @Parameter(description = "ID de l'utilisateur") @RequestParam Long userId,
            @Parameter(description = "ID de la societe") @RequestParam Long societeId) {
        return ResponseEntity.ok(accessService.hasWriteAccess(userId, societeId));
    }

    @GetMapping("/check/validate")
    @Operation(
            summary = "Verifier le droit de validation",
            description = "Verifie si un utilisateur a le droit de validation sur une societe. "
                    + "Pour les membres: MANAGER et FINANCE ont ce droit. Pour les comptables: selon leur configuration.")
    @ApiResponse(responseCode = "200", description = "Resultat de la verification")
    public ResponseEntity<Boolean> hasValidateAccess(
            @Parameter(description = "ID de l'utilisateur") @RequestParam Long userId,
            @Parameter(description = "ID de la societe") @RequestParam Long societeId) {
        return ResponseEntity.ok(accessService.hasValidateAccess(userId, societeId));
    }

    @GetMapping("/check/permission")
    @Operation(
            summary = "Verifier une permission specifique",
            description = "Verifie si un utilisateur a une permission specifique sur une societe")
    @ApiResponse(responseCode = "200", description = "Resultat de la verification")
    public ResponseEntity<Boolean> hasPermission(
            @Parameter(description = "ID de l'utilisateur") @RequestParam Long userId,
            @Parameter(description = "ID de la societe") @RequestParam Long societeId,
            @Parameter(description = "Code de la permission") @RequestParam String permissionCode) {
        return ResponseEntity.ok(accessService.hasPermission(userId, societeId, permissionCode));
    }

    @GetMapping("/user/{userId}/societe/{societeId}/permissions")
    @Operation(
            summary = "Lister les permissions",
            description = "Retourne toutes les permissions d'un utilisateur sur une societe")
    @ApiResponse(responseCode = "200", description = "Liste des codes de permission")
    public ResponseEntity<List<String>> getUserPermissions(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId,
            @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
        return ResponseEntity.ok(accessService.getUserPermissions(userId, societeId));
    }

    // ===== Liste des societes accessibles =====

    @GetMapping("/user/{userId}/societes")
    @Operation(
            summary = "Lister toutes les societes accessibles",
            description = "Retourne toutes les societes auxquelles un utilisateur a acces, "
                    + "que ce soit en tant que COMPTABLE (via comptable_societes) ou MEMBRE (via user_societes)")
    @ApiResponse(responseCode = "200", description = "Liste des societes avec details d'acces")
    public ResponseEntity<List<SocieteAccessDto>> getAccessibleSocietes(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
        return ResponseEntity.ok(accessService.getAccessibleSocietes(userId));
    }

    @GetMapping("/user/{userId}/societes/write")
    @Operation(
            summary = "Lister les societes avec droit d'ecriture",
            description = "Retourne uniquement les societes sur lesquelles l'utilisateur a le droit d'ecriture")
    @ApiResponse(responseCode = "200", description = "Liste des societes avec droit d'ecriture")
    public ResponseEntity<List<SocieteAccessDto>> getWriteAccessibleSocietes(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
        return ResponseEntity.ok(accessService.getWriteAccessibleSocietes(userId));
    }

    // ===== Cache Management =====

    @DeleteMapping("/cache")
    @Operation(
            summary = "Invalider tout le cache d'acces",
            description = "Invalide tout le cache d'acces utilisateur. A utiliser apres des modifications massives.")
    @ApiResponse(responseCode = "204", description = "Cache invalide")
    public ResponseEntity<Void> evictAllCache() {
        accessService.evictUserAccessCache();
        accessService.evictPermissionsCache();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cache/user/{userId}/societe/{societeId}")
    @Operation(
            summary = "Invalider le cache pour un acces specifique",
            description = "Invalide le cache d'acces pour un utilisateur et une societe specifiques")
    @ApiResponse(responseCode = "204", description = "Cache invalide")
    public ResponseEntity<Void> evictUserCache(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId,
            @Parameter(description = "ID de la societe") @PathVariable Long societeId) {
        accessService.evictUserAccess(userId, societeId);
        return ResponseEntity.noContent().build();
    }
}
