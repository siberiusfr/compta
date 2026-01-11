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
import tn.cyberious.compta.authz.dto.request.CreateSocieteRequest;
import tn.cyberious.compta.authz.dto.request.UpdateSocieteRequest;
import tn.cyberious.compta.authz.service.SocieteService;

@RestController
@RequestMapping("/api/societes")
@RequiredArgsConstructor
@Tag(name = "Societes Clientes", description = "Gestion des societes clientes")
public class SocieteController {

    private final SocieteService societeService;

    @PostMapping
    @Operation(summary = "Creer une societe cliente", description = "Cree une nouvelle societe cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Societe creee avec succes"),
        @ApiResponse(responseCode = "400", description = "Donnees invalides ou matricule fiscale deja existant")
    })
    public ResponseEntity<SocieteDto> create(@Valid @RequestBody CreateSocieteRequest request) {
        SocieteDto created = societeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une societe cliente", description = "Met a jour les informations d'une societe cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Societe mise a jour"),
        @ApiResponse(responseCode = "404", description = "Societe non trouvee")
    })
    public ResponseEntity<SocieteDto> update(
            @Parameter(description = "ID de la societe") @PathVariable Long id,
            @Valid @RequestBody UpdateSocieteRequest request) {
        return ResponseEntity.ok(societeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une societe cliente", description = "Supprime une societe cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Societe supprimee"),
        @ApiResponse(responseCode = "404", description = "Societe non trouvee")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la societe") @PathVariable Long id) {
        societeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une societe cliente", description = "Recupere les details d'une societe cliente par son ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Societe trouvee"),
        @ApiResponse(responseCode = "404", description = "Societe non trouvee")
    })
    public ResponseEntity<SocieteDto> findById(
            @Parameter(description = "ID de la societe") @PathVariable Long id) {
        return ResponseEntity.ok(societeService.findById(id));
    }

    @GetMapping("/matricule/{matriculeFiscale}")
    @Operation(summary = "Recuperer par matricule fiscale", description = "Recupere une societe cliente par son matricule fiscale")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Societe trouvee"),
        @ApiResponse(responseCode = "404", description = "Societe non trouvee")
    })
    public ResponseEntity<SocieteDto> findByMatriculeFiscale(
            @Parameter(description = "Matricule fiscale (13 caracteres)") @PathVariable String matriculeFiscale) {
        return ResponseEntity.ok(societeService.findByMatriculeFiscale(matriculeFiscale));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les societes clientes", description = "Recupere la liste de toutes les societes clientes")
    @ApiResponse(responseCode = "200", description = "Liste des societes")
    public ResponseEntity<List<SocieteDto>> findAll() {
        return ResponseEntity.ok(societeService.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Lister les societes clientes actives", description = "Recupere uniquement les societes clientes actives")
    @ApiResponse(responseCode = "200", description = "Liste des societes actives")
    public ResponseEntity<List<SocieteDto>> findAllActive() {
        return ResponseEntity.ok(societeService.findAllActive());
    }

    @GetMapping("/cabinet/{societeComptableId}")
    @Operation(summary = "Lister par cabinet comptable", description = "Recupere les societes gerees par un cabinet comptable")
    @ApiResponse(responseCode = "200", description = "Liste des societes")
    public ResponseEntity<List<SocieteDto>> findBySocieteComptableId(
            @Parameter(description = "ID de la societe comptable") @PathVariable Long societeComptableId) {
        return ResponseEntity.ok(societeService.findBySocieteComptableId(societeComptableId));
    }

    @GetMapping("/cabinet/{societeComptableId}/active")
    @Operation(summary = "Lister les actives par cabinet comptable", description = "Recupere les societes actives gerees par un cabinet comptable")
    @ApiResponse(responseCode = "200", description = "Liste des societes actives")
    public ResponseEntity<List<SocieteDto>> findActiveBySocieteComptableId(
            @Parameter(description = "ID de la societe comptable") @PathVariable Long societeComptableId) {
        return ResponseEntity.ok(societeService.findActiveBySocieteComptableId(societeComptableId));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des societes clientes", description = "Recherche par raison sociale (recherche partielle)")
    @ApiResponse(responseCode = "200", description = "Resultats de la recherche")
    public ResponseEntity<List<SocieteDto>> search(
            @Parameter(description = "Terme de recherche") @RequestParam String q) {
        return ResponseEntity.ok(societeService.search(q));
    }

    @GetMapping("/secteur/{secteur}")
    @Operation(summary = "Lister par secteur", description = "Recupere les societes clientes d'un secteur d'activite")
    @ApiResponse(responseCode = "200", description = "Liste des societes du secteur")
    public ResponseEntity<List<SocieteDto>> findBySecteur(
            @Parameter(description = "Secteur d'activite") @PathVariable String secteur) {
        return ResponseEntity.ok(societeService.findBySecteur(secteur));
    }
}
