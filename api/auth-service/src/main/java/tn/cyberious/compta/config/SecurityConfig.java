package tn.cyberious.compta.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tn.cyberious.compta.security.CustomUserDetailsService;
import tn.cyberious.compta.security.JwtAuthenticationEntryPoint;
import tn.cyberious.compta.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Public endpoints
                    .requestMatchers("/api/auth/login", "/api/auth/refresh")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()

                    // Auth profile endpoints - authenticated users
                    .requestMatchers("/api/auth/logout", "/api/auth/me", "/api/auth/password")
                    .authenticated()

                    // User management endpoints - create users
                    .requestMatchers(HttpMethod.POST, "/api/users/comptable")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/users/societe")
                    .hasAnyRole("ADMIN", "COMPTABLE")
                    .requestMatchers(HttpMethod.POST, "/api/users/employee")
                    .hasAnyRole("ADMIN", "COMPTABLE", "SOCIETE")

                    // User CRUD endpoints
                    .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/*")
                    .hasAnyRole("ADMIN", "COMPTABLE")
                    .requestMatchers(HttpMethod.PUT, "/api/users/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/users/*")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.PUT,
                        "/api/users/*/activate",
                        "/api/users/*/deactivate",
                        "/api/users/*/unlock")
                    .hasRole("ADMIN")

                    // Role management endpoints
                    .requestMatchers(HttpMethod.GET, "/api/users/*/roles")
                    .hasAnyRole("ADMIN", "COMPTABLE")
                    .requestMatchers(HttpMethod.POST, "/api/users/*/roles")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/users/*/roles/*")
                    .hasRole("ADMIN")

                    // Societe CRUD endpoints
                    .requestMatchers(HttpMethod.GET, "/api/societes", "/api/societes/*")
                    .hasAnyRole("ADMIN", "COMPTABLE", "SOCIETE")
                    .requestMatchers(HttpMethod.POST, "/api/societes")
                    .hasAnyRole("ADMIN", "COMPTABLE")
                    .requestMatchers(HttpMethod.PUT, "/api/societes/*")
                    .hasAnyRole("ADMIN", "COMPTABLE")
                    .requestMatchers(HttpMethod.DELETE, "/api/societes/*")
                    .hasAnyRole("ADMIN", "COMPTABLE")

                    // Societe associations endpoints
                    .requestMatchers(
                        HttpMethod.GET, "/api/societes/*/users", "/api/societes/*/employees")
                    .hasAnyRole("ADMIN", "COMPTABLE", "SOCIETE")
                    .requestMatchers(HttpMethod.GET, "/api/societes/user/*")
                    .hasAnyRole("ADMIN", "COMPTABLE", "SOCIETE")
                    .requestMatchers(HttpMethod.POST, "/api/societes/comptable-assignment")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/societes/comptable-assignment/*/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/societes/user-assignment")
                    .hasAnyRole("ADMIN", "COMPTABLE")
                    .requestMatchers(HttpMethod.DELETE, "/api/societes/user-assignment/*/*")
                    .hasAnyRole("ADMIN", "COMPTABLE")

                    // Employee endpoints
                    .requestMatchers(HttpMethod.POST, "/api/employees")
                    .hasAnyRole("ADMIN", "COMPTABLE", "SOCIETE")

                    // Auth logs endpoints
                    .requestMatchers(HttpMethod.GET, "/api/auth/logs")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/auth/logs/user/*")
                    .hasAnyRole("ADMIN", "COMPTABLE")
                    .requestMatchers(HttpMethod.GET, "/api/auth/logs/action/*")
                    .hasRole("ADMIN")

                    // All other requests must be authenticated
                    .anyRequest()
                    .authenticated())
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
