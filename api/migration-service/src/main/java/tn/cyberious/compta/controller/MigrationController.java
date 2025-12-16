package tn.cyberious.compta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur pour consulter l'état des migrations de base de données.
 * Ce service ne gère plus l'exécution des migrations, seulement la consultation.
 */
@RestController
@RequestMapping("/api/migrations")
@Tag(name = "Migrations", description = "Consultation de l'état des migrations de base de données")
public class MigrationController {

    private final Flyway flyway;

    public MigrationController(Flyway flyway) {
        this.flyway = flyway;
    }

    @GetMapping
    @Operation(summary = "Liste toutes les migrations", description = "Retourne la liste de toutes les migrations (appliquées et en attente)")
    public ResponseEntity<List<Map<String, Object>>> getAllMigrations() {
        MigrationInfoService infoService = flyway.info();
        MigrationInfo[] migrations = infoService.all();

        List<Map<String, Object>> migrationList = Arrays.stream(migrations)
                .map(this::migrationInfoToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(migrationList);
    }

    @GetMapping("/applied")
    @Operation(summary = "Liste les migrations appliquées", description = "Retourne uniquement les migrations qui ont été appliquées")
    public ResponseEntity<List<Map<String, Object>>> getAppliedMigrations() {
        MigrationInfoService infoService = flyway.info();
        MigrationInfo[] migrations = infoService.applied();

        List<Map<String, Object>> migrationList = Arrays.stream(migrations)
                .map(this::migrationInfoToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(migrationList);
    }

    @GetMapping("/pending")
    @Operation(summary = "Liste les migrations en attente", description = "Retourne les migrations qui n'ont pas encore été appliquées")
    public ResponseEntity<List<Map<String, Object>>> getPendingMigrations() {
        MigrationInfoService infoService = flyway.info();
        MigrationInfo[] migrations = infoService.pending();

        List<Map<String, Object>> migrationList = Arrays.stream(migrations)
                .map(this::migrationInfoToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(migrationList);
    }

    @GetMapping("/info")
    @Operation(summary = "Informations sur l'état des migrations", description = "Retourne des informations générales sur l'état de la base de données")
    public ResponseEntity<Map<String, Object>> getMigrationInfo() {
        MigrationInfoService infoService = flyway.info();

        Map<String, Object> info = new HashMap<>();
        info.put("current", infoService.current() != null ? migrationInfoToMap(infoService.current()) : null);
        info.put("totalCount", infoService.all().length);
        info.put("appliedCount", infoService.applied().length);
        info.put("pendingCount", infoService.pending().length);
        info.put("failedCount", infoService.failed().length);

        return ResponseEntity.ok(info);
    }

    @GetMapping("/failed")
    @Operation(summary = "Liste les migrations échouées", description = "Retourne les migrations qui ont échoué lors de leur application")
    public ResponseEntity<List<Map<String, Object>>> getFailedMigrations() {
        MigrationInfoService infoService = flyway.info();
        MigrationInfo[] migrations = infoService.failed();

        List<Map<String, Object>> migrationList = Arrays.stream(migrations)
                .map(this::migrationInfoToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(migrationList);
    }

    /**
     * Convertit un MigrationInfo en Map pour la sérialisation JSON.
     */
    private Map<String, Object> migrationInfoToMap(MigrationInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("version", info.getVersion() != null ? info.getVersion().toString() : null);
        map.put("description", info.getDescription());
        map.put("type", info.getType().name());
        map.put("script", info.getScript());
        map.put("checksum", info.getChecksum());
        map.put("installedOn", info.getInstalledOn());
        map.put("installedBy", info.getInstalledBy());
        map.put("executionTime", info.getExecutionTime());
        map.put("state", info.getState().name());
        return map;
    }
}
