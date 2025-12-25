package tn.compta.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Actuator endpoints - public
                        .pathMatchers("/actuator/**").permitAll()
                        // Auth service public endpoints
                        .pathMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .pathMatchers("/login", "/register").permitAll()
                        // Swagger UI and API docs - public
                        .pathMatchers("/auth/swagger-ui/**", "/auth/webjars/**", "/auth/v3/api-docs/**").permitAll()
                        .pathMatchers("/authz/swagger-ui/**", "/authz/webjars/**", "/authz/v3/api-docs/**").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        // All other requests - JWT authentication is handled by JwtAuthenticationFilter
                        .anyExchange().permitAll()
                )
                .build();
    }
}
