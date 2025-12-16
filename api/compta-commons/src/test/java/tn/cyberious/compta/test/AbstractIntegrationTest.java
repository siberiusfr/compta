package tn.cyberious.compta.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe de base pour les tests d'intégration.
 * Configure automatiquement un conteneur PostgreSQL via Testcontainers.
 * Utilisez @ActiveProfiles("test") si vous avez besoin d'un profil spécifique supplémentaire.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("integration-test")
public abstract class AbstractIntegrationTest {

    /**
     * Conteneur PostgreSQL partagé entre tous les tests.
     * L'annotation @ServiceConnection permet à Spring Boot de configurer automatiquement
     * la DataSource, Flyway, etc. avec les paramètres du conteneur.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Réutiliser le conteneur entre les exécutions pour gagner du temps

}
