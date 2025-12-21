package tn.cyberious.compta.auth.config;

import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration JOOQ pour la gestion de la base de données. Configure les paramètres de rendu SQL
 * et autres options.
 */
@Configuration
public class JooqConfig {

  @Bean
  public Settings jooqSettings() {
    return new Settings()
        // Utiliser les noms de tables/colonnes tels quels (pas de conversion en majuscules)
        .withRenderNameCase(RenderNameCase.LOWER)
        // Formater le SQL généré de manière lisible
        .withRenderFormatted(true)
        // Activer les logs de requêtes SQL (peut être désactivé en production)
        .withExecuteLogging(true);
  }
}
