package tn.cyberious.compta;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tn.cyberious.compta.test.AbstractIntegrationTest;
import tn.cyberious.compta.test.config.TestConfiguration;

@SpringBootTest(classes = TestConfiguration.class)
class ComptaApplicationTests extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
    // Le contexte Spring Boot démarre avec une vraie base PostgreSQL via Testcontainers
    // Flyway exécute les migrations automatiquement
  }
}
