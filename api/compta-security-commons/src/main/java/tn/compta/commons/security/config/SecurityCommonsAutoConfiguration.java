package tn.compta.commons.security.config;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tn.compta.commons.security.aspect.SecurityAspect;
import tn.compta.commons.security.filter.GatewayAuthenticationFilter;
import tn.compta.commons.security.interceptor.PermissionCheckInterceptor;
import tn.compta.commons.security.interceptor.RoleCheckInterceptor;
import tn.compta.commons.security.resolver.AuthenticatedUserArgumentResolver;
import tn.compta.commons.security.resolver.SecurityContextArgumentResolver;

/**
 * Auto-configuration for Compta Security Commons.
 *
 * <p>This configuration is automatically loaded by Spring Boot via spring.factories.
 *
 * <p>This module extracts authentication information from API Gateway headers (X-User-*).
 *
 * <p>To configure, add the following properties:
 *
 * <pre>
 * compta.security.enabled=true
 * compta.security.public-paths=/actuator/**,/v3/api-docs/**,/swagger-ui/**
 * </pre>
 */
@Slf4j
@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "compta.security.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SecurityCommonsAutoConfiguration implements WebMvcConfigurer {

  private final SecurityProperties securityProperties;

  /**
   * Create security aspect bean.
   *
   * @return the security aspect
   */
  @Bean
  @ConditionalOnMissingBean
  public SecurityAspect securityAspect() {
    log.info("Creating SecurityAspect bean");
    return new SecurityAspect();
  }

  /**
   * Create role check interceptor bean.
   *
   * @return the interceptor
   */
  @Bean
  @ConditionalOnMissingBean
  public RoleCheckInterceptor roleCheckInterceptor() {
    log.info("Creating RoleCheckInterceptor bean");
    return new RoleCheckInterceptor();
  }

  /**
   * Create permission check interceptor bean.
   *
   * @return the interceptor
   */
  @Bean
  @ConditionalOnMissingBean
  public PermissionCheckInterceptor permissionCheckInterceptor() {
    log.info("Creating PermissionCheckInterceptor bean");
    return new PermissionCheckInterceptor();
  }

  /**
   * Create authenticated user argument resolver bean.
   *
   * @return the argument resolver
   */
  @Bean
  @ConditionalOnMissingBean
  public AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver() {
    log.info("Creating AuthenticatedUserArgumentResolver bean");
    return new AuthenticatedUserArgumentResolver();
  }

  /**
   * Create security context argument resolver bean.
   *
   * @return the argument resolver
   */
  @Bean
  @ConditionalOnMissingBean
  public SecurityContextArgumentResolver securityContextArgumentResolver() {
    log.info("Creating SecurityContextArgumentResolver bean");
    return new SecurityContextArgumentResolver();
  }

  /**
   * Register gateway authentication filter.
   *
   * @return the filter registration bean
   */
  @Bean
  @ConditionalOnMissingBean
  public FilterRegistrationBean<GatewayAuthenticationFilter> gatewayAuthenticationFilter() {

    log.info("Registering GatewayAuthenticationFilter");

    List<String> publicPaths = parsePublicPaths(securityProperties.getPublicPaths());
    log.info("Public paths: {}", publicPaths);

    GatewayAuthenticationFilter filter = new GatewayAuthenticationFilter(publicPaths);

    FilterRegistrationBean<GatewayAuthenticationFilter> registration =
        new FilterRegistrationBean<>(filter);
    registration.setOrder(1);
    registration.addUrlPatterns("/*");

    return registration;
  }

  /**
   * Parse public paths from comma-separated string.
   *
   * @param pathsString the paths string
   * @return the list of paths
   */
  private List<String> parsePublicPaths(String pathsString) {
    if (pathsString == null || pathsString.isEmpty()) {
      return List.of();
    }
    return Arrays.stream(pathsString.split(",")).map(String::trim).toList();
  }

  /**
   * Add custom argument resolvers.
   *
   * @param resolvers the list of resolvers
   */
  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(authenticatedUserArgumentResolver());
    resolvers.add(securityContextArgumentResolver());
    log.info("Added custom argument resolvers");
  }

  /**
   * Add interceptors.
   *
   * @param registry the interceptor registry
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(roleCheckInterceptor()).order(1);
    registry.addInterceptor(permissionCheckInterceptor()).order(2);
    log.info("Added security interceptors");
  }
}
