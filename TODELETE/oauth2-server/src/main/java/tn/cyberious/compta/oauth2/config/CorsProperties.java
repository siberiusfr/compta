package tn.cyberious.compta.oauth2.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

  private String allowedOrigins = "http://localhost:3000";
  private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS,PATCH";
  private String allowedHeaders = "*";
  private boolean allowCredentials = true;
  private long maxAge = 3600;

  public List<String> getAllowedOriginsList() {
    return List.of(allowedOrigins.split(","));
  }

  public List<String> getAllowedMethodsList() {
    return List.of(allowedMethods.split(","));
  }

  public String getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(String allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  public String getAllowedMethods() {
    return allowedMethods;
  }

  public void setAllowedMethods(String allowedMethods) {
    this.allowedMethods = allowedMethods;
  }

  public String getAllowedHeaders() {
    return allowedHeaders;
  }

  public void setAllowedHeaders(String allowedHeaders) {
    this.allowedHeaders = allowedHeaders;
  }

  public boolean isAllowCredentials() {
    return allowCredentials;
  }

  public void setAllowCredentials(boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(long maxAge) {
    this.maxAge = maxAge;
  }
}
