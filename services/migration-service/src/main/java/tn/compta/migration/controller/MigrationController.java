package tn.compta.migration.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.MigrationInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.compta.migration.service.MigrationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final MigrationService migrationService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("GET /api/migration/status");
        List<MigrationInfo> migrations = migrationService.getStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("total", migrations.size());
        response.put("migrations", migrations);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPending() {
        log.info("GET /api/migration/pending");
        List<MigrationInfo> pending = migrationService.getPending();

        Map<String, Object> response = new HashMap<>();
        response.put("count", pending.size());
        response.put("migrations", pending);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/applied")
    public ResponseEntity<Map<String, Object>> getApplied() {
        log.info("GET /api/migration/applied");
        List<MigrationInfo> applied = migrationService.getApplied();

        Map<String, Object> response = new HashMap<>();
        response.put("count", applied.size());
        response.put("migrations", applied);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/migrate")
    public ResponseEntity<Map<String, Object>> migrate() {
        log.info("POST /api/migration/migrate");

        try {
            int migrationsExecuted = migrationService.migrate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("migrationsExecuted", migrationsExecuted);
            response.put("message", "Migration completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Migration failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/repair")
    public ResponseEntity<Map<String, Object>> repair() {
        log.info("POST /api/migration/repair");

        try {
            migrationService.repair();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Repair completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Repair failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate() {
        log.info("POST /api/migration/validate");

        try {
            migrationService.validate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Validation completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Validation failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/baseline")
    public ResponseEntity<Map<String, Object>> baseline() {
        log.info("POST /api/migration/baseline");

        try {
            migrationService.baseline();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Baseline created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Baseline creation failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}
