package tn.cyberious.compta.oauth2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for scheduled tasks. Enables Spring's scheduling support for background jobs like
 * audit log cleanup.
 */
@Configuration
@EnableScheduling
public class ScheduledTasksConfig {
  // Scheduled tasks are defined in service classes with @Scheduled annotation
}
