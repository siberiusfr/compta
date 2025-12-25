package tn.cyberious.compta.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  // IMPORTANT: Utilise un nom simple sans espaces!
  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Auth Service API")
            .version("1.0.0")
            .description("Service d'authentification - JWT requis pour les endpoints protégés")
            .contact(new Contact()
                .name("COMPTA Team")
                .email("support@compta.tn")))
        // Toutes les requêtes passent par la gateway
        .servers(List.of(
            new Server()
                .url("http://localhost:8080")
                .description("API Gateway")
        ))
        // Applique la sécurité JWT globalement
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(new Components()
            .addSecuritySchemes(SECURITY_SCHEME_NAME,
                new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .in(SecurityScheme.In.HEADER)
                    .description("Entrez votre token JWT (sans le préfixe 'Bearer ')")));
  }
}
