package tn.cyberious.compta.authz.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI/Swagger pour authz-service.
 *
 * <p>Ce service est derrière une API Gateway qui injecte des headers X-User-* après validation JWT.
 * Cette configuration documente ces headers dans Swagger UI pour faciliter les tests.
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Authz Service API")
                .version("1.0")
                .description(
                    "Service d'autorisation et de gestion des permissions.\n\n"
                        + "**Important:** Ce service est derrière une API Gateway. "
                        + "Les headers X-User-* ci-dessous sont normalement injectés par la gateway "
                        + "après validation du JWT. Pour tester directement ce service sans la gateway, "
                        + "vous devez fournir ces headers manuellement."))
        .components(
            new Components()
                .addSecuritySchemes(
                    "gateway-headers",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-User-Id")
                        .description("ID de l'utilisateur (injecté par la gateway)")));
  }

  /**
   * Ajoute les paramètres de header gateway à tous les endpoints (sauf les publics).
   *
   * @return customizer pour ajouter les headers
   */
  @Bean
  public OperationCustomizer gatewayHeadersCustomizer() {
    return (operation, handlerMethod) -> {
      // Ajouter les headers seulement si l'opération n'est pas publique
      // (les endpoints publics comme /actuator, /swagger-ui sont exclus automatiquement)

      operation.addParametersItem(
          new Parameter()
              .in("header")
              .name("X-User-Id")
              .description("ID de l'utilisateur authentifié (injecté par la gateway)")
              .schema(new StringSchema())
              .example("1")
              .required(false));

      operation.addParametersItem(
          new Parameter()
              .in("header")
              .name("X-User-Username")
              .description("Username de l'utilisateur (injecté par la gateway)")
              .schema(new StringSchema())
              .example("admin")
              .required(false));

      operation.addParametersItem(
          new Parameter()
              .in("header")
              .name("X-User-Email")
              .description("Email de l'utilisateur (injecté par la gateway)")
              .schema(new StringSchema())
              .example("admin@example.com")
              .required(false));

      operation.addParametersItem(
          new Parameter()
              .in("header")
              .name("X-User-Roles")
              .description(
                  "Rôles de l'utilisateur, séparés par des virgules (injecté par la gateway)")
              .schema(new StringSchema())
              .example("ADMIN,USER")
              .required(false));

      operation.addParametersItem(
          new Parameter()
              .in("header")
              .name("X-User-Societe-Ids")
              .description(
                  "IDs des sociétés associées, séparés par des virgules (injecté par la gateway)")
              .schema(new StringSchema())
              .example("1,2,3")
              .required(false));

      operation.addParametersItem(
          new Parameter()
              .in("header")
              .name("X-User-Primary-Societe-Id")
              .description("ID de la société principale (injecté par la gateway)")
              .schema(new StringSchema())
              .example("1")
              .required(false));

      operation.addParametersItem(
          new Parameter()
              .in("header")
              .name("X-User-Permissions")
              .description(
                  "Permissions de l'utilisateur, séparées par des virgules (injecté par la gateway)")
              .schema(new StringSchema())
              .example("READ_USER,WRITE_USER,DELETE_USER")
              .required(false));

      operation.addParametersItem(
          new Parameter()
              .in("header")
              .name("X-Request-Id")
              .description("ID unique de la requête pour le traçage (injecté par la gateway)")
              .schema(new StringSchema())
              .example("550e8400-e29b-41d4-a716-446655440000")
              .required(false));

      return operation;
    };
  }
}
