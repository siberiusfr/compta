package tn.cyberious.compta.oauth2.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tn.cyberious.compta.oauth2.dto.AuditLog;

/**
 * Service for managing audit logs. Provides methods to log security events asynchronously for
 * performance.
 */
@Service
public class AuditLogService {

  private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;

  public AuditLogService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
  }

  /**
   * Log an audit event asynchronously. This method returns immediately and logs the event in the
   * background.
   *
   * @param auditLog The audit log to record
   */
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logAsync(AuditLog auditLog) {
    try {
      log(auditLog);
    } catch (Exception e) {
      log.error("Failed to log audit event asynchronously", e);
    }
  }

  /**
   * Log an audit event synchronously. This method blocks until the log is written to the database.
   *
   * @param auditLog The audit log to record
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void log(AuditLog auditLog) {
    try {
      String sql =
          """
          INSERT INTO oauth2.audit_logs (
              event_type, event_category, user_id, username, client_id,
              ip_address, user_agent, request_uri, request_method, status,
              error_message, details, tenant_id
          ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
          """;

      String detailsJson = null;
      if (auditLog.getDetails() != null && !auditLog.getDetails().isEmpty()) {
        try {
          detailsJson = objectMapper.writeValueAsString(auditLog.getDetails());
        } catch (JsonProcessingException e) {
          log.warn("Failed to serialize audit log details", e);
        }
      }

      jdbcTemplate.update(
          sql,
          auditLog.getEventType(),
          auditLog.getEventCategory(),
          auditLog.getUserId(),
          auditLog.getUsername(),
          auditLog.getClientId(),
          auditLog.getIpAddress(),
          auditLog.getUserAgent(),
          auditLog.getRequestUri(),
          auditLog.getRequestMethod(),
          auditLog.getStatus(),
          auditLog.getErrorMessage(),
          detailsJson,
          auditLog.getTenantId());

      log.debug("Audit log recorded: {}", auditLog.getEventType());
    } catch (Exception e) {
      log.error("Failed to log audit event", e);
      throw e;
    }
  }

  /**
   * Find audit logs by user ID.
   *
   * @param userId The user ID to search for
   * @param limit Maximum number of results to return
   * @return List of audit logs for the user
   */
  @Transactional(readOnly = true)
  public List<AuditLog> findByUserId(String userId, int limit) {
    String sql =
        """
        SELECT id, event_type, event_category, user_id, username, client_id,
               ip_address, user_agent, request_uri, request_method, status,
               error_message, details, created_at, tenant_id
        FROM oauth2.audit_logs
        WHERE user_id = ?
        ORDER BY created_at DESC
        LIMIT ?
        """;

    return jdbcTemplate.query(sql, this::mapRowToAuditLog, userId, limit);
  }

  /**
   * Find audit logs by client ID.
   *
   * @param clientId The client ID to search for
   * @param limit Maximum number of results to return
   * @return List of audit logs for the client
   */
  @Transactional(readOnly = true)
  public List<AuditLog> findByClientId(String clientId, int limit) {
    String sql =
        """
        SELECT id, event_type, event_category, user_id, username, client_id,
               ip_address, user_agent, request_uri, request_method, status,
               error_message, details, created_at, tenant_id
        FROM oauth2.audit_logs
        WHERE client_id = ?
        ORDER BY created_at DESC
        LIMIT ?
        """;

    return jdbcTemplate.query(sql, this::mapRowToAuditLog, clientId, limit);
  }

  /**
   * Find audit logs by IP address.
   *
   * @param ipAddress The IP address to search for
   * @param limit Maximum number of results to return
   * @return List of audit logs for the IP address
   */
  @Transactional(readOnly = true)
  public List<AuditLog> findByIpAddress(String ipAddress, int limit) {
    String sql =
        """
        SELECT id, event_type, event_category, user_id, username, client_id,
               ip_address, user_agent, request_uri, request_method, status,
               error_message, details, created_at, tenant_id
        FROM oauth2.audit_logs
        WHERE ip_address = ?
        ORDER BY created_at DESC
        LIMIT ?
        """;

    return jdbcTemplate.query(sql, this::mapRowToAuditLog, ipAddress, limit);
  }

  /**
   * Find audit logs by event type.
   *
   * @param eventType The event type to search for
   * @param limit Maximum number of results to return
   * @return List of audit logs for the event type
   */
  @Transactional(readOnly = true)
  public List<AuditLog> findByEventType(String eventType, int limit) {
    String sql =
        """
        SELECT id, event_type, event_category, user_id, username, client_id,
               ip_address, user_agent, request_uri, request_method, status,
               error_message, details, created_at, tenant_id
        FROM oauth2.audit_logs
        WHERE event_type = ?
        ORDER BY created_at DESC
        LIMIT ?
        """;

    return jdbcTemplate.query(sql, this::mapRowToAuditLog, eventType, limit);
  }

  /**
   * Find failed login attempts for a user or IP.
   *
   * @param userId Optional user ID to filter by
   * @param ipAddress Optional IP address to filter by
   * @param since Only return attempts after this timestamp
   * @return Number of failed login attempts
   */
  @Transactional(readOnly = true)
  public int countFailedLoginAttempts(String userId, String ipAddress, Instant since) {
    String sql =
        """
        SELECT COUNT(*)
        FROM oauth2.audit_logs
        WHERE event_type = ?
          AND status = ?
          AND created_at > ?
          """;

    LocalDateTime sinceDateTime = LocalDateTime.ofInstant(since, ZoneId.systemDefault());

    if (userId != null && !userId.isEmpty()) {
      sql += " AND user_id = ?";
      return jdbcTemplate.queryForObject(
          sql,
          Integer.class,
          AuditLog.EventTypes.LOGIN_FAILED,
          AuditLog.Status.FAILURE,
          sinceDateTime,
          userId);
    } else if (ipAddress != null && !ipAddress.isEmpty()) {
      sql += " AND ip_address = ?";
      return jdbcTemplate.queryForObject(
          sql,
          Integer.class,
          AuditLog.EventTypes.LOGIN_FAILED,
          AuditLog.Status.FAILURE,
          sinceDateTime,
          ipAddress);
    } else {
      return jdbcTemplate.queryForObject(
          sql,
          Integer.class,
          AuditLog.EventTypes.LOGIN_FAILED,
          AuditLog.Status.FAILURE,
          sinceDateTime);
    }
  }

  /**
   * Find recent audit logs for a tenant.
   *
   * @param tenantId The tenant ID to search for
   * @param limit Maximum number of results to return
   * @return List of recent audit logs for the tenant
   */
  @Transactional(readOnly = true)
  public List<AuditLog> findRecentByTenant(String tenantId, int limit) {
    String sql =
        """
        SELECT id, event_type, event_category, user_id, username, client_id,
               ip_address, user_agent, request_uri, request_method, status,
               error_message, details, created_at, tenant_id
        FROM oauth2.audit_logs
        WHERE tenant_id = ?
        ORDER BY created_at DESC
        LIMIT ?
        """;

    return jdbcTemplate.query(sql, this::mapRowToAuditLog, tenantId, limit);
  }

  /**
   * Delete old audit logs.
   *
   * @param olderThan Delete logs older than this timestamp
   * @return Number of logs deleted
   */
  @Transactional
  public int deleteOldLogs(Instant olderThan) {
    String sql =
        """
        DELETE FROM oauth2.audit_logs
        WHERE created_at < ?
        """;

    LocalDateTime olderThanDateTime = LocalDateTime.ofInstant(olderThan, ZoneId.systemDefault());
    int deleted = jdbcTemplate.update(sql, olderThanDateTime);

    log.info("Deleted {} old audit logs older than {}", deleted, olderThanDateTime);
    return deleted;
  }

  /**
   * Get audit log statistics for a time period.
   *
   * @param since Start of the time period
   * @param until End of the time period
   * @return Map of event types to their counts
   */
  @Transactional(readOnly = true)
  public Map<String, Integer> getStatistics(Instant since, Instant until) {
    String sql =
        """
        SELECT event_type, COUNT(*) as count
        FROM oauth2.audit_logs
        WHERE created_at >= ? AND created_at <= ?
        GROUP BY event_type
        """;

    LocalDateTime sinceDateTime = LocalDateTime.ofInstant(since, ZoneId.systemDefault());
    LocalDateTime untilDateTime = LocalDateTime.ofInstant(until, ZoneId.systemDefault());

    return jdbcTemplate.query(
        sql,
        rs -> {
          Map<String, Integer> stats = new java.util.HashMap<>();
          while (rs.next()) {
            stats.put(rs.getString("event_type"), rs.getInt("count"));
          }
          return stats;
        },
        sinceDateTime,
        untilDateTime);
  }

  /** Map a database row to an AuditLog object. */
  private AuditLog mapRowToAuditLog(java.sql.ResultSet rs, int rowNum)
      throws java.sql.SQLException {
    AuditLog auditLog = new AuditLog();
    auditLog.setId(rs.getLong("id"));
    auditLog.setEventType(rs.getString("event_type"));
    auditLog.setEventCategory(rs.getString("event_category"));
    auditLog.setUserId(rs.getString("user_id"));
    auditLog.setUsername(rs.getString("username"));
    auditLog.setClientId(rs.getString("client_id"));
    auditLog.setIpAddress(rs.getString("ip_address"));
    auditLog.setUserAgent(rs.getString("user_agent"));
    auditLog.setRequestUri(rs.getString("request_uri"));
    auditLog.setRequestMethod(rs.getString("request_method"));
    auditLog.setStatus(rs.getString("status"));
    auditLog.setErrorMessage(rs.getString("error_message"));
    auditLog.setTenantId(rs.getString("tenant_id"));

    // Parse JSON details
    String detailsJson = rs.getString("details");
    if (detailsJson != null && !detailsJson.isEmpty()) {
      try {
        Map<String, Object> details =
            objectMapper.readValue(
                detailsJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        auditLog.setDetails(details);
      } catch (JsonProcessingException e) {
        log.warn("Failed to parse audit log details", e);
      }
    }

    // Convert timestamp to Instant
    LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
    if (createdAt != null) {
      auditLog.setCreatedAt(createdAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    return auditLog;
  }

  /**
   * Scheduled task to clean up old audit logs. Runs daily at 2 AM to delete logs older than 90
   * days.
   */
  @Scheduled(cron = "0 0 2 * * ?")
  @Transactional
  public void cleanupOldLogs() {
    // Delete logs older than 90 days
    Instant cutoff = Instant.now().minus(java.time.Duration.ofDays(90));
    int deleted = deleteOldLogs(cutoff);
    log.info("Cleanup completed: {} old audit logs deleted", deleted);
  }
}
