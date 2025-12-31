package tn.cyberious.compta.oauth2.service;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.cyberious.compta.contracts.notification.SendVerificationEmailPayload;
import tn.cyberious.compta.oauth2.queue.EmailVerificationQueuePublisher;

/**
 * Service for handling email verification functionality.
 *
 * <p>Provides email-based verification for new user registration.
 */
@Service
public class EmailVerificationService {

  private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);

  private final JdbcTemplate jdbcTemplate;
  private final PasswordEncoder passwordEncoder;
  private final AuditLogService auditLogService;
  private final EmailVerificationQueuePublisher emailVerificationQueuePublisher;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  // Token expiration time (24 hours)
  private static final long TOKEN_EXPIRATION_HOURS = 24;

  public EmailVerificationService(
      JdbcTemplate jdbcTemplate,
      PasswordEncoder passwordEncoder,
      AuditLogService auditLogService,
      EmailVerificationQueuePublisher emailVerificationQueuePublisher) {
    this.jdbcTemplate = jdbcTemplate;
    this.passwordEncoder = passwordEncoder;
    this.auditLogService = auditLogService;
    this.emailVerificationQueuePublisher = emailVerificationQueuePublisher;
  }

  /**
   * Initiate email verification for a user by email. Generates a secure token and sends it via
   * email.
   *
   * @param email The user's email address
   * @param ipAddress The IP address of the request
   * @param userAgent The user agent string
   * @return true if verification initiated, false if user not found
   */
  @Transactional
  public boolean initiateEmailVerification(String email, String ipAddress, String userAgent) {
    // Find user by email
    String userId = findUserIdByEmail(email);

    if (userId == null) {
      log.warn("Email verification requested for non-existent email: {}", email);
      // Don't reveal that email doesn't exist
      return false;
    }

    // Get username for logging
    String username = findUsernameById(userId);

    // Generate secure verification token
    String token = generateVerificationToken();

    // Calculate expiration time
    LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);

    // Store verification token in database
    String sql =
        """
        INSERT INTO oauth2.email_verification_tokens (
            user_id, username, email, token, expires_at, ip_address, user_agent
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    jdbcTemplate.update(sql, userId, username, email, token, expiresAt, ipAddress, userAgent);

    // Build verification link and publish to async queue
    String verificationLink = buildVerificationLink(token);
    SendVerificationEmailPayload payload = new SendVerificationEmailPayload()
        .withUserId(UUID.fromString(userId))
        .withEmail(email)
        .withUsername(username)
        .withToken(token)
        .withVerificationLink(URI.create(verificationLink))
        .withExpiresAt(expiresAt.toInstant(ZoneOffset.UTC))
        .withLocale(SendVerificationEmailPayload.Locale.FR);
    emailVerificationQueuePublisher.publishEmailVerificationRequested(payload);

    // Log the event
    auditLogService.logAsync(
        tn.cyberious.compta.oauth2.dto.AuditLog.builder()
            .eventType(tn.cyberious.compta.oauth2.dto.AuditLog.EventTypes.EMAIL_VERIFIED)
            .eventCategory(tn.cyberious.compta.oauth2.dto.AuditLog.EventCategories.USER)
            .userId(userId)
            .username(username)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .requestUri("/api/users/email/verify")
            .requestMethod("POST")
            .status(tn.cyberious.compta.oauth2.dto.AuditLog.Status.SUCCESS)
            .build());

    log.info("Email verification initiated for user: {}", username);
    return true;
  }

  /**
   * Verify email with token.
   *
   * @param token The verification token
   * @param ipAddress The IP address of the request
   * @param userAgent The user agent string
   * @return true if email verified successfully, false otherwise
   */
  @Transactional
  public boolean verifyEmail(String token, String ipAddress, String userAgent) {
    // Validate token
    String[] tokenInfo = validateVerificationToken(token);

    if (tokenInfo == null) {
      log.warn("Invalid email verification token: {}", token);
      return false;
    }

    String userId = tokenInfo[0];
    String username = tokenInfo[1];

    // Check if token is already verified
    if (isTokenVerified(token)) {
      log.warn("Email verification token already verified: {}", token);
      return false;
    }

    // Mark token as verified and enable user account
    markTokenAsVerified(token);
    enableUserAccount(userId);

    // Log the event
    auditLogService.logAsync(
        tn.cyberious.compta.oauth2.dto.AuditLog.builder()
            .eventType(tn.cyberious.compta.oauth2.dto.AuditLog.EventTypes.EMAIL_VERIFIED)
            .eventCategory(tn.cyberious.compta.oauth2.dto.AuditLog.EventCategories.USER)
            .userId(userId)
            .username(username)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .requestUri("/api/users/email/verify")
            .requestMethod("POST")
            .status(tn.cyberious.compta.oauth2.dto.AuditLog.Status.SUCCESS)
            .build());

    log.info("Email verified successfully for user: {}", username);
    return true;
  }

  /**
   * Validate an email verification token.
   *
   * @param token The verification token to validate
   * @return Array containing [userId, username] if valid, null otherwise
   */
  private String[] validateVerificationToken(String token) {
    String sql =
        """
        SELECT user_id, username, expires_at, verified
        FROM oauth2.email_verification_tokens
        WHERE token = ?
        """;

    return jdbcTemplate.query(
        sql,
        (rs) -> {
          if (rs.next()) {
            LocalDateTime expiresAt = rs.getObject("expires_at", LocalDateTime.class);
            boolean verified = rs.getBoolean("verified");

            // Check if token is expired
            if (LocalDateTime.now().isAfter(expiresAt)) {
              log.warn("Email verification token expired: {}", token);
              return null;
            }

            // Check if token is already verified
            if (verified) {
              log.warn("Email verification token already verified: {}", token);
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
   * Check if a verification token has been verified.
   *
   * @param token The verification token to check
   * @return true if token is verified, false otherwise
   */
  private boolean isTokenVerified(String token) {
    String sql = "SELECT verified FROM oauth2.email_verification_tokens WHERE token = ?";
    Boolean verified = jdbcTemplate.queryForObject(sql, Boolean.class, token);
    return verified != null && verified;
  }

  /**
   * Mark a verification token as verified.
   *
   * @param token The verification token to mark as verified
   */
  @Transactional
  private void markTokenAsVerified(String token) {
    String sql = "UPDATE oauth2.email_verification_tokens SET verified = TRUE WHERE token = ?";
    jdbcTemplate.update(sql, token);
  }

  /**
   * Enable a user account after email verification.
   *
   * @param userId The user ID to enable
   */
  @Transactional
  private void enableUserAccount(String userId) {
    String sql = "UPDATE oauth2.users SET enabled = TRUE, email_verified = TRUE WHERE id = ?";
    jdbcTemplate.update(sql, userId);
  }

  /**
   * Generate a secure random verification token.
   *
   * @return A 36-character UUID-based token
   */
  private String generateVerificationToken() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * Build an email verification link.
   *
   * @param token The verification token
   * @return The full verification URL
   */
  private String buildVerificationLink(String token) {
    return frontendUrl + "/verify-email?token=" + token;
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
   * Clean up expired email verification tokens.
   *
   * @return The number of tokens cleaned up
   */
  @Transactional
  public int cleanupExpiredTokens() {
    String sql =
        "DELETE FROM oauth2.email_verification_tokens WHERE expires_at < CURRENT_TIMESTAMP";
    int deleted = jdbcTemplate.update(sql);
    log.info("Cleaned up {} expired email verification tokens", deleted);
    return deleted;
  }

  /**
   * Clean up verified email verification tokens older than 7 days.
   *
   * @return The number of tokens cleaned up
   */
  @Transactional
  public int cleanupVerifiedTokens() {
    String sql =
        """
        DELETE FROM oauth2.email_verification_tokens
        WHERE verified = TRUE
          AND created_at < CURRENT_TIMESTAMP - INTERVAL '7 days'
        """;
    int deleted = jdbcTemplate.update(sql);
    log.info("Cleaned up {} verified email verification tokens", deleted);
    return deleted;
  }
}
