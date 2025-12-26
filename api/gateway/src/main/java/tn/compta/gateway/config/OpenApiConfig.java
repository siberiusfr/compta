package tn.compta.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI/Swagger configuration for API Gateway.
 *
 * Environment-specific configuration:
 * - Dev: Exposes local URLs only
 * - Prod: Exposes production URLs only
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

  private final ProfileHelper profileHelper;

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
   * Returns server URLs based on environment.
   * In production, only the production URL is exposed.
   */
  private List<Server> getServerUrls() {
    List<Server> servers = new ArrayList<>();

    if (profileHelper.isProduction()) {
      servers.add(new Server()
          .url(prodGatewayUrl)
          .description("Production Gateway"));
    } else {
      servers.add(new Server()
          .url(devGatewayUrl)
          .description("Development Gateway"));
    }

    return servers;
  }
}
