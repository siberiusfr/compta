package tn.compta.migration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationService {

    private final Flyway flyway;

    public List<MigrationInfo> getStatus() {
        log.info("Getting migration status");
        MigrationInfoService info = flyway.info();
        return Arrays.asList(info.all());
    }

    public List<MigrationInfo> getPending() {
        log.info("Getting pending migrations");
        MigrationInfoService info = flyway.info();
        return Arrays.stream(info.pending())
                .collect(Collectors.toList());
    }

    public List<MigrationInfo> getApplied() {
        log.info("Getting applied migrations");
        MigrationInfoService info = flyway.info();
        return Arrays.stream(info.applied())
                .collect(Collectors.toList());
    }

    public int migrate() {
        log.info("Starting database migration");
        try {
            int migrationsExecuted = flyway.migrate().migrationsExecuted;
            log.info("Migration completed. {} migrations executed", migrationsExecuted);
            return migrationsExecuted;
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            throw new RuntimeException("Migration failed: " + e.getMessage(), e);
        }
    }

    public void repair() {
        log.info("Repairing Flyway schema history");
        try {
            flyway.repair();
            log.info("Repair completed successfully");
        } catch (Exception e) {
            log.error("Repair failed: {}", e.getMessage(), e);
            throw new RuntimeException("Repair failed: " + e.getMessage(), e);
        }
    }

    public void validate() {
        log.info("Validating migrations");
        try {
            flyway.validate();
            log.info("Validation successful");
        } catch (Exception e) {
            log.error("Validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Validation failed: " + e.getMessage(), e);
        }
    }

    public void baseline() {
        log.info("Creating baseline");
        try {
            flyway.baseline();
            log.info("Baseline created successfully");
        } catch (Exception e) {
            log.error("Baseline creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Baseline creation failed: " + e.getMessage(), e);
        }
    }
}
