package tn.cyberious.compta.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Flyway commune pour tous les services.
 * Chaque service peut avoir ses propres migrations dans db/migration.
 */
@Configuration
public class FlywayConfig {

    /**
     * Stratégie de migration par défaut.
     * Les migrations sont exécutées automatiquement au démarrage si flyway.enabled=true.
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true", matchIfMissing = false)
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Valider les migrations avant de les exécuter
            flyway.validate();
            // Exécuter les migrations
            flyway.migrate();
        };
    }
}
