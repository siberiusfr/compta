package tn.cyberious.compta.oauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI oauth2ServerOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("OAuth2 Authorization Server API")
                .description(
                    "OAuth2 Authorization Server for Compta microservices. Provides token issuance and user authentication.")
                .version("1.0.0")
                .license(new License().name("Apache 2.0").url("http://springdoc.org")));
  }
}
