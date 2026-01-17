package tn.cyberious.compta.oauth2.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for audit log entries. Represents a security event that needs to be logged for compliance and
 * auditing purposes.
 */
public class AuditLog {

  private Long id;
  private String eventType;
  private String eventCategory;
  private String userId;
  private String username;
  private String clientId;
  private String ipAddress;
  private String userAgent;
  private String requestUri;
  private String requestMethod;
  private String status;
  private String errorMessage;
  private Map<String, Object> details;
  private Instant createdAt;
  private String tenantId;

  public AuditLog() {}

  public AuditLog(
      String eventType,
      String eventCategory,
      String userId,
      String username,
      String clientId,
      String ipAddress,
      String userAgent,
      String requestUri,
      String requestMethod,
      String status,
      String errorMessage,
      Map<String, Object> details,
      String tenantId) {
    this.eventType = eventType;
    this.eventCategory = eventCategory;
    this.userId = userId;
    this.username = username;
    this.clientId = clientId;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.requestUri = requestUri;
    this.requestMethod = requestMethod;
    this.status = status;
    this.errorMessage = errorMessage;
    this.details = details;
    this.tenantId = tenantId;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEventCategory() {
    return eventCategory;
  }

  public void setEventCategory(String eventCategory) {
    this.eventCategory = eventCategory;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getRequestUri() {
    return requestUri;
  }

  public void setRequestUri(String requestUri) {
    this.requestUri = requestUri;
  }

  public String getRequestMethod() {
    return requestMethod;
  }

  public void setRequestMethod(String requestMethod) {
    this.requestMethod = requestMethod;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public void setDetails(Map<String, Object> details) {
    this.details = details;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  // Builder pattern
  public static class Builder {
    private String eventType;
    private String eventCategory;
    private String userId;
    private String username;
    private String clientId;
    private String ipAddress;
    private String userAgent;
    private String requestUri;
    private String requestMethod;
    private String status;
    private String errorMessage;
    private Map<String, Object> details;
    private String tenantId;

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder eventCategory(String eventCategory) {
      this.eventCategory = eventCategory;
      return this;
    }

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    public Builder ipAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder userAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    public Builder requestUri(String requestUri) {
      this.requestUri = requestUri;
      return this;
    }

    public Builder requestMethod(String requestMethod) {
      this.requestMethod = requestMethod;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public Builder details(Map<String, Object> details) {
      this.details = details;
      return this;
    }

    public Builder tenantId(String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public AuditLog build() {
      return new AuditLog(
          eventType,
          eventCategory,
          userId,
          username,
          clientId,
          ipAddress,
          userAgent,
          requestUri,
          requestMethod,
          status,
          errorMessage,
          details,
          tenantId);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  // Event Types
  public static class EventTypes {
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String TOKEN_ISSUED = "TOKEN_ISSUED";
    public static final String TOKEN_REFRESHED = "TOKEN_REFRESHED";
    public static final String TOKEN_REVOKED = "TOKEN_REVOKED";
    public static final String TOKEN_INTROSPECTED = "TOKEN_INTROSPECTED";
    public static final String AUTHORIZATION_CODE_ISSUED = "AUTHORIZATION_CODE_ISSUED";
    public static final String AUTHORIZATION_GRANTED = "AUTHORIZATION_GRANTED";
    public static final String AUTHORIZATION_DENIED = "AUTHORIZATION_DENIED";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    public static final String PASSWORD_RESET_COMPLETED = "PASSWORD_RESET_COMPLETED";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
    public static final String EMAIL_VERIFIED = "EMAIL_VERIFIED";
    public static final String EMAIL_VERIFICATION_REQUESTED = "EMAIL_VERIFICATION_REQUESTED";
    public static final String EMAIL_VERIFICATION_COMPLETED = "EMAIL_VERIFICATION_COMPLETED";
    public static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String ROLE_ASSIGNED = "ROLE_ASSIGNED";
    public static final String ROLE_REMOVED = "ROLE_REMOVED";
    public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
    public static final String CLIENT_CREATED = "CLIENT_CREATED";
    public static final String CLIENT_UPDATED = "CLIENT_UPDATED";
    public static final String CLIENT_DELETED = "CLIENT_DELETED";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String CSRF_TOKEN_VALIDATION_FAILED = "CSRF_TOKEN_VALIDATION_FAILED";

    private EventTypes() {}
  }

  // Event Categories
  public static class EventCategories {
    public static final String AUTHENTICATION = "AUTHENTICATION";
    public static final String AUTHORIZATION = "AUTHORIZATION";
    public static final String TOKEN = "TOKEN";
    public static final String USER = "USER";
    public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
    public static final String CLIENT = "CLIENT";
    public static final String SECURITY = "SECURITY";
    public static final String AUDIT = "AUDIT";

    private EventCategories() {}
  }

  // Status Values
  public static class Status {
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String WARNING = "WARNING";

    private Status() {}
  }
}
