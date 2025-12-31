package tn.cyberious.compta;

import org.junit.jupiter.api.Test;

import tn.cyberious.compta.test.AbstractIntegrationTest;

class MigrationServiceApplicationTests extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
    // Le contexte Spring Boot démarre avec une vraie base PostgreSQL via Testcontainers
    // Flyway exécute les migrations automatiquement
  }
}
