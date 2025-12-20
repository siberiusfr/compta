package tn.cyberious.compta.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour le service de migration. Ce service ne gère plus l'exécution des migrations,
 * seulement la consultation.
 */
@Configuration
public class MigrationServiceConfig {

  /**
   * Stratégie de migration qui ne fait rien. Les migrations ne sont plus exécutées automatiquement
   * par ce service. Chaque service applicatif gère ses propres migrations.
   */
  @Bean
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      // Ne rien faire - ce service est en lecture seule
      // Les migrations sont gérées par chaque service individuellement
    };
  }
}
