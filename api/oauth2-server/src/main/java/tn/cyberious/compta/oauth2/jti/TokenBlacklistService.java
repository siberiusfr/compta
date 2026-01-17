package tn.cyberious.compta.oauth2.jti;

import com.nimbusds.jwt.SignedJWT;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing token blacklisting using JTI (JWT ID).
 *
 * <p>Tracks revoked tokens by their JTI claim to prevent their reuse. Uses database persistence
 * with an in-memory cache for performance.
 */
@Service
public class TokenBlacklistService {

  private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

  private final JdbcTemplate jdbcTemplate;

  // In-memory cache for fast lookups (synchronized with database)
  private final ConcurrentHashMap<String, Instant> blacklistCache = new ConcurrentHashMap<>();

  // Cache initialized flag
  private volatile boolean cacheInitialized;

  public TokenBlacklistService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /** Initialize the cache from database on first access. */
  private void ensureCacheInitialized() {
    if (!cacheInitialized) {
      synchronized (this) {
        if (!cacheInitialized) {
          loadCacheFromDatabase();
          cacheInitialized = true;
        }
      }
    }
  }

  /** Load blacklist entries from database into cache. */
  private void loadCacheFromDatabase() {
    String sql = "SELECT jti, expires_at FROM oauth2.token_blacklist WHERE expires_at > ?";
    LocalDateTime now = LocalDateTime.now();

    List<Object[]> entries =
        jdbcTemplate.query(
            sql,
            (rs, rowNum) ->
                new Object[] {rs.getString("jti"), rs.getTimestamp("expires_at").toInstant()},
            Timestamp.valueOf(now));

    blacklistCache.clear();
    for (Object[] entry : entries) {
      blacklistCache.put((String) entry[0], (Instant) entry[1]);
    }

    log.info("Loaded {} blacklist entries from database into cache", entries.size());
  }

  /**
   * Add a token to the blacklist by its JTI.
   *
   * @param jti The JWT ID to blacklist
   * @param expirationTime The expiration time of the token
   */
  @Transactional
  public void addToBlacklist(String jti, Instant expirationTime) {
    addToBlacklist(jti, expirationTime, null, null);
  }

  /**
   * Add a token to the blacklist by its JTI with additional metadata.
   *
   * @param jti The JWT ID to blacklist
   * @param expirationTime The expiration time of the token
   * @param revokedBy The user or client that revoked the token
   * @param reason The reason for revocation
   */
  @Transactional
  public void addToBlacklist(String jti, Instant expirationTime, String revokedBy, String reason) {
    if (jti == null || jti.isEmpty()) {
      log.warn("Attempted to blacklist token with null or empty JTI");
      return;
    }

    ensureCacheInitialized();

    // Insert into database (ignore if already exists)
    String sql =
        """
        INSERT INTO oauth2.token_blacklist (jti, expires_at, revoked_by, reason)
        VALUES (?, ?, ?, ?)
        ON CONFLICT (jti) DO NOTHING
        """;

    LocalDateTime expiresAt = LocalDateTime.ofInstant(expirationTime, ZoneId.systemDefault());
    jdbcTemplate.update(sql, jti, Timestamp.valueOf(expiresAt), revokedBy, reason);

    // Update cache
    blacklistCache.put(jti, expirationTime);

    log.debug("Token with JTI {} added to blacklist, expires at {}", jti, expirationTime);
  }

  /**
   * Check if a token is blacklisted by its JTI.
   *
   * @param jti The JWT ID to check
   * @return true if the token is blacklisted, false otherwise
   */
  public boolean isBlacklisted(String jti) {
    if (jti == null || jti.isEmpty()) {
      return false;
    }

    ensureCacheInitialized();

    // Check cache first for performance
    Instant expirationTime = blacklistCache.get(jti);

    if (expirationTime != null) {
      // Check if expired
      if (Instant.now().isAfter(expirationTime)) {
        // Clean up expired entry from cache
        blacklistCache.remove(jti);
        return false;
      }
      return true;
    }

    // If not in cache, check database (in case another instance added it)
    String sql = "SELECT expires_at FROM oauth2.token_blacklist WHERE jti = ?";
    List<Timestamp> results = jdbcTemplate.queryForList(sql, Timestamp.class, jti);

    if (!results.isEmpty() && results.get(0) != null) {
      Instant dbExpirationTime = results.get(0).toInstant();
      if (Instant.now().isBefore(dbExpirationTime)) {
        // Add to cache and return true
        blacklistCache.put(jti, dbExpirationTime);
        return true;
      }
    }

    return false;
  }

  /**
   * Extract JTI from a JWT token string.
   *
   * @param token The JWT token string
   * @return The JTI claim, or null if not present
   */
  public String extractJti(String token) {
    try {
      SignedJWT jwt = SignedJWT.parse(token);
      return jwt.getJWTClaimsSet().getJWTID();
    } catch (Exception e) {
      log.error("Failed to extract JTI from token", e);
      return null;
    }
  }

  /**
   * Revoke a token by adding it to the blacklist.
   *
   * @param token The JWT token string to revoke
   * @param expirationTime The expiration time of the token
   * @return true if successfully revoked, false otherwise
   */
  @Transactional
  public boolean revokeToken(String token, Instant expirationTime) {
    return revokeToken(token, expirationTime, null, null);
  }

  /**
   * Revoke a token by adding it to the blacklist with metadata.
   *
   * @param token The JWT token string to revoke
   * @param expirationTime The expiration time of the token
   * @param revokedBy The user or client that revoked the token
   * @param reason The reason for revocation
   * @return true if successfully revoked, false otherwise
   */
  @Transactional
  public boolean revokeToken(
      String token, Instant expirationTime, String revokedBy, String reason) {
    String jti = extractJti(token);

    if (jti == null) {
      log.warn("Cannot revoke token without JTI claim");
      return false;
    }

    addToBlacklist(jti, expirationTime, revokedBy, reason);
    return true;
  }

  /**
   * Get the number of blacklisted tokens.
   *
   * @return The count of blacklisted tokens
   */
  public int getBlacklistedCount() {
    String sql = "SELECT COUNT(*) FROM oauth2.token_blacklist WHERE expires_at > ?";
    Integer count =
        jdbcTemplate.queryForObject(sql, Integer.class, Timestamp.valueOf(LocalDateTime.now()));
    return count != null ? count : 0;
  }

  /**
   * Clear all blacklisted tokens.
   *
   * @return The number of tokens cleared
   */
  @Transactional
  public int clearBlacklist() {
    String sql = "DELETE FROM oauth2.token_blacklist";
    int count = jdbcTemplate.update(sql);
    blacklistCache.clear();
    log.info("Cleared {} blacklisted tokens", count);
    return count;
  }

  /**
   * Scheduled task to clean up expired blacklist entries. Runs every hour to remove entries that
   * have expired.
   */
  @Scheduled(fixedRate = 3600000) // Every hour
  @Transactional
  public void cleanupExpiredEntries() {
    String sql = "DELETE FROM oauth2.token_blacklist WHERE expires_at < ?";
    int removed = jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()));

    // Clean up cache as well
    Instant now = Instant.now();
    blacklistCache.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));

    if (removed > 0) {
      log.info("Cleaned up {} expired blacklist entries from database", removed);
    }
  }

  /**
   * Get all blacklisted JTIs.
   *
   * @return Set of blacklisted JWT IDs
   */
  public Set<String> getBlacklistedJtis() {
    String sql = "SELECT jti FROM oauth2.token_blacklist WHERE expires_at > ?";
    List<String> jtis =
        jdbcTemplate.queryForList(sql, String.class, Timestamp.valueOf(LocalDateTime.now()));
    return new HashSet<>(jtis);
  }

  /**
   * Remove expired entries from blacklist.
   *
   * @return The number of expired entries removed
   */
  @Transactional
  public int removeExpiredEntries() {
    String sql = "DELETE FROM oauth2.token_blacklist WHERE expires_at < ?";
    int removed = jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()));

    // Clean up cache
    Instant now = Instant.now();
    blacklistCache.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));

    log.info("Removed {} expired blacklist entries", removed);
    return removed;
  }

  /** Refresh cache from database. Useful after database changes from other instances. */
  @Transactional(readOnly = true)
  public void refreshCache() {
    loadCacheFromDatabase();
  }
}
