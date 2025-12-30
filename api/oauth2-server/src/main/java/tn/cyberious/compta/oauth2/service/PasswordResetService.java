package tn.cyberious.compta.oauth2.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.oauth2.dto.PasswordResetConfirmRequest;

/**
 * Service for handling password reset functionality.
 *
 * <p>Provides email-based password reset with secure token generation and validation.
 */
@Service
public class PasswordResetService {

  private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

  private final JdbcTemplate jdbcTemplate;
  private final PasswordEncoder passwordEncoder;
  private final AuditLogService auditLogService;
  private final EmailService emailService;

  // Token expiration time (1 hour)
  private static final long TOKEN_EXPIRATION_MINUTES = 60;

  public PasswordResetService(
      JdbcTemplate jdbcTemplate,
      PasswordEncoder passwordEncoder,
      AuditLogService auditLogService,
      EmailService emailService) {
    this.jdbcTemplate = jdbcTemplate;
    this.passwordEncoder = passwordEncoder;
    this.auditLogService = auditLogService;
    this.emailService = emailService;
  }

  /**
   * Initiate password reset for a user by email. Generates a secure token and sends it via email.
   *
   * @param email The user's email address
   * @param ipAddress The IP address of the request
   * @param userAgent The user agent string
   * @return true if reset initiated, false if user not found
   */
  @Transactional
  public boolean initiatePasswordReset(String email, String ipAddress, String userAgent) {
    // Find user by email
    String userId = findUserIdByEmail(email);

    if (userId == null) {
      log.warn("Password reset requested for non-existent email: {}", email);
      // Don't reveal that email doesn't exist
      return false;
    }

    // Get username for logging
    String username = findUsernameById(userId);

    // Generate secure reset token
    String token = generateResetToken();

    // Calculate expiration time
    LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);

    // Store reset token in database
    String sql =
        """
        INSERT INTO oauth2.password_reset_tokens (
            user_id, username, email, token, expires_at, ip_address, user_agent
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    jdbcTemplate.update(sql, userId, username, email, token, expiresAt, ipAddress, userAgent);

    // Send email with reset link
    String resetLink = buildResetLink(token);
    emailService.sendPasswordResetEmail(email, username, resetLink);

    // Log the event
    auditLogService.logAsync(
        tn.cyberious.compta.oauth2.dto.AuditLog.builder()
            .eventType(tn.cyberious.compta.oauth2.dto.AuditLog.EventTypes.PASSWORD_RESET_REQUESTED)
            .eventCategory(tn.cyberious.compta.oauth2.dto.AuditLog.EventCategories.USER)
            .userId(userId)
            .username(username)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .requestUri("/api/users/password/reset")
            .requestMethod("POST")
            .status(tn.cyberious.compta.oauth2.dto.AuditLog.Status.SUCCESS)
            .build());

    log.info("Password reset initiated for user: {}", username);
    return true;
  }

  /**
   * Confirm password reset with token and new password.
   *
   * @param request The password reset confirmation request
   * @param ipAddress The IP address of the request
   * @param userAgent The user agent string
   * @return true if password reset successful, false otherwise
   */
  @Transactional
  public boolean confirmPasswordReset(
      PasswordResetConfirmRequest request, String ipAddress, String userAgent) {
    // Validate token
    String[] tokenInfo = validateResetToken(request.token());

    if (tokenInfo == null) {
      log.warn("Invalid password reset token: {}", request.token());
      return false;
    }

    String userId = tokenInfo[0];
    String username = tokenInfo[1];

    // Check if token is already used
    if (isTokenUsed(request.token())) {
      log.warn("Password reset token already used: {}", request.token());
      return false;
    }

    // Update user password
    String updatePasswordSql =
        """
        UPDATE oauth2.users
        SET password = ?, updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """;

    int updated =
        jdbcTemplate.update(updatePasswordSql, passwordEncoder.encode(request.password()), userId);

    if (updated == 0) {
      log.warn("Failed to update password for user: {}", userId);
      return false;
    }

    // Mark token as used
    markTokenAsUsed(request.token());

    // Log the event
    auditLogService.logAsync(
        tn.cyberious.compta.oauth2.dto.AuditLog.builder()
            .eventType(tn.cyberious.compta.oauth2.dto.AuditLog.EventTypes.PASSWORD_RESET_COMPLETED)
            .eventCategory(tn.cyberious.compta.oauth2.dto.AuditLog.EventCategories.USER)
            .userId(userId)
            .username(username)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .requestUri("/api/users/password/reset/confirm")
            .requestMethod("POST")
            .status(tn.cyberious.compta.oauth2.dto.AuditLog.Status.SUCCESS)
            .build());

    log.info("Password reset completed for user: {}", username);
    return true;
  }

  /**
   * Validate a password reset token.
   *
   * @param token The reset token to validate
   * @return Array containing [userId, username] if valid, null otherwise
   */
  private String[] validateResetToken(String token) {
    String sql =
        """
        SELECT user_id, username, expires_at, used
        FROM oauth2.password_reset_tokens
        WHERE token = ?
        """;

    return jdbcTemplate.query(
        sql,
        (rs) -> {
          if (rs.next()) {
            LocalDateTime expiresAt = rs.getObject("expires_at", LocalDateTime.class);
            boolean used = rs.getBoolean("used");

            // Check if token is expired
            if (LocalDateTime.now().isAfter(expiresAt)) {
              log.warn("Password reset token expired: {}", token);
              return null;
            }

            // Check if token is already used
            if (used) {
              log.warn("Password reset token already used: {}", token);
              return null;
            }

            // Token is valid
            return new String[] {rs.getString("user_id"), rs.getString("username")};
          }

          return null;
        },
        token);
  }

  /**
   * Check if a reset token has been used.
   *
   * @param token The reset token to check
   * @return true if token is used, false otherwise
   */
  private boolean isTokenUsed(String token) {
    String sql = "SELECT used FROM oauth2.password_reset_tokens WHERE token = ?";
    Boolean used = jdbcTemplate.queryForObject(sql, Boolean.class, token);
    return used != null && used;
  }

  /**
   * Mark a reset token as used.
   *
   * @param token The reset token to mark as used
   */
  @Transactional
  private void markTokenAsUsed(String token) {
    String sql = "UPDATE oauth2.password_reset_tokens SET used = TRUE WHERE token = ?";
    jdbcTemplate.update(sql, token);
  }

  /**
   * Generate a secure random reset token.
   *
   * @return A 36-character UUID-based token
   */
  private String generateResetToken() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * Build the password reset link.
   *
   * @param token The reset token
   * @return The full reset URL
   */
  private String buildResetLink(String token) {
    // In production, this should come from configuration
    String baseUrl = "http://localhost:3000";
    return baseUrl + "/reset-password?token=" + token;
  }

  /**
   * Find user ID by email.
   *
   * @param email The email address
   * @return The user ID, or null if not found
   */
  private String findUserIdByEmail(String email) {
    String sql = "SELECT id FROM oauth2.users WHERE email = ?";
    return jdbcTemplate.queryForObject(sql, String.class, email);
  }

  /**
   * Find username by user ID.
   *
   * @param userId The user ID
   * @return The username, or null if not found
   */
  private String findUsernameById(String userId) {
    String sql = "SELECT username FROM oauth2.users WHERE id = ?";
    return jdbcTemplate.queryForObject(sql, String.class, userId);
  }

  /**
   * Clean up expired password reset tokens.
   *
   * @return The number of tokens cleaned up
   */
  @Transactional
  public int cleanupExpiredTokens() {
    String sql = "DELETE FROM oauth2.password_reset_tokens WHERE expires_at < CURRENT_TIMESTAMP";
    int deleted = jdbcTemplate.update(sql);
    log.info("Cleaned up {} expired password reset tokens", deleted);
    return deleted;
  }

  /**
   * Clean up used password reset tokens older than 24 hours.
   *
   * @return The number of tokens cleaned up
   */
  @Transactional
  public int cleanupUsedTokens() {
    String sql =
        """
        DELETE FROM oauth2.password_reset_tokens
        WHERE used = TRUE
          AND created_at < CURRENT_TIMESTAMP - INTERVAL '24 hours'
        """;
    int deleted = jdbcTemplate.update(sql);
    log.info("Cleaned up {} used password reset tokens", deleted);
    return deleted;
  }
}
