package tn.cyberious.compta.oauth2.jti;

import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing token blacklisting using JTI (JWT ID).
 *
 * <p>Tracks revoked tokens by their JTI claim to prevent their reuse. Uses an in-memory cache with
 * automatic expiration for performance.
 */
@Service
public class TokenBlacklistService {

  private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

  // Cache for blacklisted JTIs with expiration time
  private final ConcurrentHashMap<String, Instant> blacklistedJtis = new ConcurrentHashMap<>();

  // Set of currently active JTIs for quick lookup
  private final ConcurrentSkipListSet<String> activeJtis = new ConcurrentSkipListSet<>();

  // Default blacklist duration (24 hours)
  private static final long DEFAULT_BLACKLIST_DURATION_HOURS = 24;

  /**
   * Add a token to the blacklist by its JTI.
   *
   * @param jti The JWT ID to blacklist
   * @param expirationTime The expiration time of the token
   */
  @Transactional
  public void addToBlacklist(String jti, Instant expirationTime) {
    if (jti == null || jti.isEmpty()) {
      log.warn("Attempted to blacklist token with null or empty JTI");
      return;
    }

    // Add to blacklist with expiration time
    blacklistedJtis.put(jti, expirationTime);

    // Remove from active set
    activeJtis.remove(jti);

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

    Instant expirationTime = blacklistedJtis.get(jti);

    // If not in blacklist, not blacklisted
    if (expirationTime == null) {
      return false;
    }

    // Check if blacklist entry has expired
    if (Instant.now().isAfter(expirationTime)) {
      // Clean up expired entry
      blacklistedJtis.remove(jti);
      log.debug("Expired blacklist entry for JTI {}", jti);
      return false;
    }

    return true;
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
   * Add a token to the active set.
   *
   * @param jti The JWT ID to mark as active
   */
  public void addToActive(String jti) {
    if (jti != null && !jti.isEmpty()) {
      activeJtis.add(jti);
    }
  }

  /**
   * Remove a token from the active set.
   *
   * @param jti The JWT ID to remove from active set
   */
  public void removeFromActive(String jti) {
    if (jti != null && !jti.isEmpty()) {
      activeJtis.remove(jti);
    }
  }

  /**
   * Check if a token is currently active.
   *
   * @param jti The JWT ID to check
   * @return true if the token is active, false otherwise
   */
  public boolean isActive(String jti) {
    if (jti == null || jti.isEmpty()) {
      return false;
    }
    return activeJtis.contains(jti);
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
    String jti = extractJti(token);

    if (jti == null) {
      log.warn("Cannot revoke token without JTI claim");
      return false;
    }

    addToBlacklist(jti, expirationTime);
    return true;
  }

  /**
   * Get the number of blacklisted tokens.
   *
   * @return The count of blacklisted tokens
   */
  public int getBlacklistedCount() {
    return blacklistedJtis.size();
  }

  /**
   * Get the number of active tokens.
   *
   * @return The count of active tokens
   */
  public int getActiveCount() {
    return activeJtis.size();
  }

  /**
   * Clear all blacklisted tokens.
   *
   * @return The number of tokens cleared
   */
  @Transactional
  public int clearBlacklist() {
    int count = blacklistedJtis.size();
    blacklistedJtis.clear();
    log.info("Cleared {} blacklisted tokens", count);
    return count;
  }

  /**
   * Clear all active tokens.
   *
   * @return The number of tokens cleared
   */
  @Transactional
  public int clearActiveTokens() {
    int count = activeJtis.size();
    activeJtis.clear();
    log.info("Cleared {} active tokens", count);
    return count;
  }

  /**
   * Scheduled task to clean up expired blacklist entries. Runs every hour to remove entries that
   * have expired.
   */
  @Scheduled(fixedRate = 3600000) // Every hour (in milliseconds)
  @Transactional
  public void cleanupExpiredEntries() {
    AtomicInteger removed = new AtomicInteger(0);
    Instant now = Instant.now();

    blacklistedJtis
        .entrySet()
        .removeIf(
            entry -> {
              if (now.isAfter(entry.getValue())) {
                removed.incrementAndGet();
                return true;
              }
              return false;
            });

    int removedCount = removed.get();
    if (removedCount > 0) {
      log.info("Cleaned up {} expired blacklist entries", removedCount);
    }
  }

  /**
   * Get all blacklisted JTIs.
   *
   * @return Set of blacklisted JWT IDs
   */
  public Set<String> getBlacklistedJtis() {
    return Set.copyOf(blacklistedJtis.keySet());
  }

  /**
   * Get all active JTIs.
   *
   * @return Set of active JWT IDs
   */
  public Set<String> getActiveJtis() {
    return Set.copyOf(activeJtis);
  }

  /**
   * Check and remove expired entries from blacklist.
   *
   * @return The number of expired entries removed
   */
  @Transactional
  public int removeExpiredEntries() {
    AtomicInteger removed = new AtomicInteger(0);
    Instant now = Instant.now();

    blacklistedJtis
        .entrySet()
        .removeIf(
            entry -> {
              if (now.isAfter(entry.getValue())) {
                removed.incrementAndGet();
                return true;
              }
              return false;
            });

    int removedCount = removed.get();
    log.info("Removed {} expired blacklist entries", removedCount);
    return removedCount;
  }
}
