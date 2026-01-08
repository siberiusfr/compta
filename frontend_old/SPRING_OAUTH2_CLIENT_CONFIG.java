import java.time.Duration;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import javax.sql.DataSource;

/**
 * Configuration du client OAuth2 pour Spring Authorization Server
 *
 * Cette configuration crée le client 'public-client' requis par l'application frontend.
 *
 * Pour utiliser cette configuration :
 * 1. Copiez cette classe dans votre projet Spring Authorization Server
 * 2. Assurez-vous que les dépendances Spring Authorization Server sont incluses
 * 3. Redémarrez le serveur
 */
@Configuration
public class OAuth2ClientConfig {

    /**
     * Initialise le client OAuth2 'public-client' au démarrage de l'application
     */
    @Bean
    public CommandLineRunner initializeOAuth2Client(JdbcRegisteredClientRepository registeredClientRepository) {
        return args -> {
            // Vérifier si le client existe déjà
            if (registeredClientRepository.findByClientId("public-client") != null) {
                System.out.println("Client 'public-client' existe déjà, création ignorée.");
                return;
            }

            // Créer le client OAuth2
            RegisteredClient publicClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("public-client")
                    .clientSecret(null) // Public client - pas de secret
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://localhost:3000/authorized")
                    .redirectUri("https://app.compta.tn/authorized")
                    .scope(OidcScopes.OPENID)
                    .scope("read")
                    .scope("write")
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .requireProofKey(true) // PKCE requis pour public client
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(30)) // Access token valide 30 minutes
                            .refreshTokenTimeToLive(Duration.ofHours(24)) // Refresh token valide 24 heures
                            .reuseRefreshTokens(false)
                            .build())
                    .build();

            // Sauvegarder le client dans la base de données
            registeredClientRepository.save(publicClient);

            System.out.println("Client OAuth2 'public-client' créé avec succès !");
            System.out.println("Client ID: " + publicClient.getClientId());
            System.out.println("Redirect URIs: " + publicClient.getRedirectUris());
            System.out.println("Scopes: " + publicClient.getScopes());
        };
    }

    /**
     * Alternative : Configuration avec DataSource pour vérification manuelle
     */
    @Bean
    public CommandLineRunner verifyClientConfiguration(DataSource dataSource) {
        return args -> {
            // Vérifier que les tables OAuth2 existent
            try {
                var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery(
                    "SELECT COUNT(*) FROM oauth2_registered_client WHERE client_id = 'public-client'"
                );

                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    System.out.println("✓ Client 'public-client' trouvé dans la base de données");
                } else {
                    System.out.println("✗ Client 'public-client' NON trouvé dans la base de données");
                    System.out.println("  Le client sera créé automatiquement au démarrage si la configuration ci-dessus est utilisée.");
                }

                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                System.err.println("Erreur lors de la vérification du client OAuth2: " + e.getMessage());
            }
        };
    }
}
