package tn.cyberious.compta.test.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Configuration minimale pour les tests de compta-commons. Cette classe permet de tester la
 * configuration commune sans créer un service complet. Elle est placée dans un sous-package pour
 * éviter les conflits avec les classes @SpringBootApplication des services.
 */
@SpringBootApplication
public class TestConfiguration {
  // Configuration minimale pour les tests
}
