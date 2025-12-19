package tn.cyberious.compta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI/Swagger pour la documentation des API REST.
 * L'interface Swagger UI sera accessible à : /swagger-ui.html
 * La définition OpenAPI sera accessible à : /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Compta")
                        .version("1.0.0")
                        .description("API REST pour la gestion de compta")
                        .contact(new Contact()
                                .name("Cyberious")
                                .email("contact@cyberious.tn")
                                .url("https://cyberious.tn"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
