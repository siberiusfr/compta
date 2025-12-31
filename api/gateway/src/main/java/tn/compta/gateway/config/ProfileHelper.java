package tn.compta.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for environment/profile detection. Centralized logic to avoid duplication across
 * configuration classes.
 */
@Component
public class ProfileHelper {

  private final String activeProfile;

  public ProfileHelper(@Value("${spring.profiles.active:dev}") String activeProfile) {
    this.activeProfile = activeProfile;
  }

  /**
   * Check if running in production environment. Handles multiple profiles (e.g.,
   * "prod,monitoring").
   */
  public boolean isProduction() {
    if (activeProfile == null) {
      return false;
    }
    return activeProfile.toLowerCase().contains("prod");
  }

  /** Check if running in development environment. */
  public boolean isDevelopment() {
    return !isProduction();
  }

  /** Get the active profile string. */
  public String getActiveProfile() {
    return activeProfile;
  }
}
