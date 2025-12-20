package tn.cyberious.compta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Flyway commune pour tous les services. Chaque service peut avoir ses propres
 * migrations dans db/migration. Un service peut fournir son propre FlywayMigrationStrategy pour
 * remplacer celui-ci.
 */
@Configuration
public class FlywayConfig {

  @Value("${compta.flyway.auto-migrate:true}")
  private boolean autoMigrate;

  /**
   * Stratégie de migration par défaut. Les migrations sont exécutées automatiquement au démarrage
   * si: - flyway.enabled=true - compta.flyway.auto-migrate=true (défaut: true)
   *
   * <p>Pour désactiver l'exécution automatique tout en gardant Flyway pour la consultation, mettre
   * compta.flyway.auto-migrate=false dans application.yml
   *
   * <p>Ce bean est conditionnel et ne sera pas créé si un service définit son propre
   * FlywayMigrationStrategy.
   */
  @Bean
  @ConditionalOnMissingBean(FlywayMigrationStrategy.class)
  @ConditionalOnProperty(
      prefix = "spring.flyway",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = false)
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      if (autoMigrate) {
        // Exécuter les migrations
        // La validation est déjà activée via validate-on-migrate: true dans application.yml
        flyway.migrate();
      }
      // Sinon, ne rien faire - Flyway est disponible pour consultation uniquement
    };
  }
}
