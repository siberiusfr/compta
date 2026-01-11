package tn.cyberious.compta.authz.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;
import feign.RequestInterceptor;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.model.UserPermission;

@Configuration
public class AuthzFeignConfig {

  private static final String HEADER_USER_ID = "X-User-Id";
  private static final String HEADER_USERNAME = "X-User-Username";
  private static final String HEADER_EMAIL = "X-User-Email";
  private static final String HEADER_ROLES = "X-User-Roles";
  private static final String HEADER_SOCIETE_IDS = "X-User-Societe-Ids";
  private static final String HEADER_PRIMARY_SOCIETE_ID = "X-User-Primary-Societe-Id";
  private static final String HEADER_PERMISSIONS = "X-User-Permissions";
  private static final String HEADER_REQUEST_ID = "X-Request-Id";

  @Value("${authz.service.url:http://localhost:8085}")
  private String authzServiceUrl;

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }

  @Bean
  public RequestInterceptor authzRequestInterceptor() {
    return template -> {
      template.header("Content-Type", "application/json");

      var context = SecurityContextHolder.getContext();
      if (context != null && context.isAuthenticated()) {
        var user = context.getUser();

        if (user.getUserId() != null) {
          template.header(HEADER_USER_ID, user.getUserId().toString());
        }
        if (user.getUsername() != null) {
          template.header(HEADER_USERNAME, user.getUsername());
        }
        if (user.getEmail() != null) {
          template.header(HEADER_EMAIL, user.getEmail());
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
          template.header(HEADER_ROLES, String.join(",", user.getRoles()));
        }
        if (user.getSocieteIds() != null && !user.getSocieteIds().isEmpty()) {
          template.header(
              HEADER_SOCIETE_IDS,
              user.getSocieteIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        if (user.getPrimarySocieteId() != null) {
          template.header(HEADER_PRIMARY_SOCIETE_ID, user.getPrimarySocieteId().toString());
        }
        if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
          template.header(
              HEADER_PERMISSIONS,
              user.getPermissions().stream().map(UserPermission::getPermission).collect(Collectors.joining(",")));
        }
      }
      if (context != null && context.getRequestId() != null) {
        template.header(HEADER_REQUEST_ID, context.getRequestId());
      }
    };
  }
}
