package tn.cyberious.compta.config;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de base pour Sentry. Chaque service doit définir son propre tag 'service' dans son
 * application.yml.
 */
@Configuration
public class SentryConfig {

  @Value("${spring.application.name:unknown}")
  private String applicationName;

  @Value("${sentry.environment:development}")
  private String environment;

  @Bean
  public Sentry.OptionsConfiguration<SentryOptions> sentryOptionsConfigurer() {
    return options -> {
      // Configurer le nom du service comme tag
      options.setTag("service", applicationName);

      // Configurer l'environnement
      options.setEnvironment(environment);

      // Configurer le taux d'échantillonnage des traces (100% en dev, à réduire en prod)
      options.setTracesSampleRate(1.0);

      // Envoyer les données de performance
      options.setSendDefaultPii(false);

      // Configurer le nom de la release (peut être overridé par service)
      options.setRelease(applicationName + "@" + getVersion());

      // Activer la détection automatique des erreurs non gérées
      options.setEnableUncaughtExceptionHandler(true);
    };
  }

  private String getVersion() {
    // Récupérer la version depuis le manifest ou utiliser une valeur par défaut
    String version = getClass().getPackage().getImplementationVersion();
    return version != null ? version : "dev";
  }
}
