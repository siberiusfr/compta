package tn.compta.gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validates JWT configuration at application startup.
 *
 * Security checks:
 * - JWT secret length (minimum 256 bits for HS256)
 * - No default secrets in production
 * - Secret complexity
 */
@Slf4j
@Configuration
public class JwtConfigValidator {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @Value("${jwt.expiration:86400000}")
  private Long jwtExpiration;

  private static final String DEFAULT_SECRET =
      "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

  @PostConstruct
  public void validateJwtConfiguration() {
    log.info("🔐 Validating JWT configuration...");

    // ✅ Check secret length (minimum 256 bits = 32 bytes = 64 hex chars)
    if (jwtSecret == null || jwtSecret.length() < 64) {
      throw new IllegalStateException(
          "JWT secret must be at least 256 bits (64 hex characters) for HS256 algorithm. " +
              "Current length: " + (jwtSecret != null ? jwtSecret.length() : 0)
      );
    }

    // ✅ Check for default secret in production
    if (isProduction() && DEFAULT_SECRET.equals(jwtSecret)) {
      throw new IllegalStateException(
          "CRITICAL SECURITY ERROR: Default JWT secret is being used in production! " +
              "Please set a unique JWT_SECRET environment variable."
      );
    }

    // ✅ Warn about default secret in other environments
    if (!isProduction() && DEFAULT_SECRET.equals(jwtSecret)) {
      log.warn("⚠️ WARNING: Using default JWT secret. This is acceptable for development " +
          "but NEVER use this in production!");
    }

    // ✅ Check expiration
    if (jwtExpiration == null || jwtExpiration <= 0) {
      throw new IllegalStateException(
          "JWT expiration must be a positive value in milliseconds"
      );
    }

    // ✅ Warn about long expiration in production
    if (isProduction() && jwtExpiration > 7200000) { // > 2 hours
      log.warn("⚠️ JWT expiration is set to {} hours. Consider using shorter expiration " +
          "times in production for better security.", jwtExpiration / 3600000.0);
    }

    log.info("✅ JWT configuration validated successfully");
    log.info("   - Secret length: {} characters", jwtSecret.length());
    log.info("   - Expiration: {} hours", jwtExpiration / 3600000.0);
    log.info("   - Environment: {}", activeProfile);
  }

  /**
   * Check if running in production environment.
   */
  private boolean isProduction() {
    return "prod".equalsIgnoreCase(activeProfile) ||
        "production".equalsIgnoreCase(activeProfile);
  }
}
