package tn.compta.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI/Swagger configuration for API Gateway.
 * 
 * Configuration adaptée selon l'environnement :
 * - Dev : Expose uniquement les URLs locales
 * - Prod : Expose uniquement les URLs de production
 */
@Configuration
public class OpenApiConfig {

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @Value("${gateway.url.dev:http://localhost:8080}")
  private String devGatewayUrl;

  @Value("${gateway.url.prod:https://api.compta.tn}")
  private String prodGatewayUrl;

  @Bean
  public OpenAPI gatewayOpenAPI() {
    OpenAPI api = new OpenAPI()
        .info(new Info()
            .title("COMPTA API Gateway")
            .version("1.0.0")
            .description("API Gateway for COMPTA ERP System. "
                + "This gateway routes requests to downstream microservices "
                + "and handles authentication via OAuth2 JWT.")
            .contact(new Contact()
                .name("COMPTA Team")
                .email("support@compta.tn")
            )
        )
        // ✅ Serveurs adaptés selon l'environnement
        .servers(getServerUrls())
        // ✅ Configuration du security scheme
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token obtenu via /auth/login")
            )
        );

    return api;
  }

  /**
   * Retourne les URLs des serveurs selon l'environnement.
   * En production, seul le serveur de production est exposé.
   */
  private List<Server> getServerUrls() {
    List<Server> servers = new ArrayList<>();

    if (isProduction()) {
      // Production : uniquement l'URL de prod
      servers.add(new Server()
          .url(prodGatewayUrl)
          .description("Production Gateway"));
    } else {
      // Development : URLs locales
      servers.add(new Server()
          .url(devGatewayUrl)
          .description("Development Gateway"));
    }

    return servers;
  }

  /**
   * Vérifie si on est en environnement de production.
   * Gère les profils multiples (ex: "prod,monitoring").
   */
  private boolean isProduction() {
    if (activeProfile == null) {
      return false;
    }
    String lowerProfile = activeProfile.toLowerCase();
    return lowerProfile.contains("prod");
  }
}
