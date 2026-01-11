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
import tn.cyberious.compta.authz.dto.SocieteComptableDto;
import tn.cyberious.compta.authz.dto.request.CreateSocieteComptableRequest;
import tn.cyberious.compta.authz.dto.request.UpdateSocieteComptableRequest;
import tn.cyberious.compta.authz.service.SocieteComptableService;

@RestController
@RequestMapping("/api/societes-comptables")
@RequiredArgsConstructor
@Tag(name = "Societes Comptables", description = "Gestion des cabinets comptables")
public class SocieteComptableController {

    private final SocieteComptableService societeComptableService;

    @PostMapping
    @Operation(summary = "Creer une societe comptable", description = "Cree un nouveau cabinet comptable")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Societe comptable creee avec succes"),
        @ApiResponse(responseCode = "400", description = "Donnees invalides ou matricule fiscale deja existant")
    })
    public ResponseEntity<SocieteComptableDto> create(
            @Valid @RequestBody CreateSocieteComptableRequest request) {
        SocieteComptableDto created = societeComptableService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une societe comptable", description = "Met a jour les informations d'un cabinet comptable")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Societe comptable mise a jour"),
        @ApiResponse(responseCode = "404", description = "Societe comptable non trouvee")
    })
    public ResponseEntity<SocieteComptableDto> update(
            @Parameter(description = "ID de la societe comptable") @PathVariable Long id,
            @Valid @RequestBody UpdateSocieteComptableRequest request) {
        return ResponseEntity.ok(societeComptableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une societe comptable", description = "Supprime un cabinet comptable s'il n'a pas de societes clientes")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Societe comptable supprimee"),
        @ApiResponse(responseCode = "404", description = "Societe comptable non trouvee"),
        @ApiResponse(responseCode = "400", description = "Impossible de supprimer: societes clientes existantes")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la societe comptable") @PathVariable Long id) {
        societeComptableService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une societe comptable", description = "Recupere les details d'un cabinet comptable par son ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Societe comptable trouvee"),
        @ApiResponse(responseCode = "404", description = "Societe comptable non trouvee")
    })
    public ResponseEntity<SocieteComptableDto> findById(
            @Parameter(description = "ID de la societe comptable") @PathVariable Long id) {
        return ResponseEntity.ok(societeComptableService.findById(id));
    }

    @GetMapping("/matricule/{matriculeFiscale}")
    @Operation(summary = "Recuperer par matricule fiscale", description = "Recupere un cabinet comptable par son matricule fiscale")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Societe comptable trouvee"),
        @ApiResponse(responseCode = "404", description = "Societe comptable non trouvee")
    })
    public ResponseEntity<SocieteComptableDto> findByMatriculeFiscale(
            @Parameter(description = "Matricule fiscale (13 caracteres)") @PathVariable String matriculeFiscale) {
        return ResponseEntity.ok(societeComptableService.findByMatriculeFiscale(matriculeFiscale));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les societes comptables", description = "Recupere la liste de tous les cabinets comptables")
    @ApiResponse(responseCode = "200", description = "Liste des societes comptables")
    public ResponseEntity<List<SocieteComptableDto>> findAll() {
        return ResponseEntity.ok(societeComptableService.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Lister les societes comptables actives", description = "Recupere uniquement les cabinets comptables actifs")
    @ApiResponse(responseCode = "200", description = "Liste des societes comptables actives")
    public ResponseEntity<List<SocieteComptableDto>> findAllActive() {
        return ResponseEntity.ok(societeComptableService.findAllActive());
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des societes comptables", description = "Recherche par raison sociale (recherche partielle)")
    @ApiResponse(responseCode = "200", description = "Resultats de la recherche")
    public ResponseEntity<List<SocieteComptableDto>> search(
            @Parameter(description = "Terme de recherche") @RequestParam String q) {
        return ResponseEntity.ok(societeComptableService.search(q));
    }
}
