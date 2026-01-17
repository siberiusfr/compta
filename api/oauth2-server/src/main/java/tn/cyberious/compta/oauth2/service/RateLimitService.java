package tn.cyberious.compta.oauth2.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.oauth2.config.RateLimitConfig.RateLimit;

/**
 * Service for rate limiting operations.
 *
 * <p>Provides methods to: - Check rate limits for specific endpoints - Block/unblock IP addresses -
 * Get current rate limit status
 */
@Slf4j
@Service
public class RateLimitService {

  private final Map<String, RateLimit> rateLimits;
  private final Map<String, BlockedIp> blockedIps = new ConcurrentHashMap<>();
  private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();

  public RateLimitService(Map<String, RateLimit> rateLimits) {
    this.rateLimits = rateLimits;
  }

  /**
   * Check if a request from the given IP to the given path should be allowed.
   *
   * @param clientIp the client IP address
   * @param path the request path
   * @return true if request should be allowed, false otherwise
   */
  public boolean isRequestAllowed(String clientIp, String path) {
    // Check if IP is blocked
    if (isIpBlocked(clientIp)) {
      log.warn("Blocked IP {} attempted to access {}", clientIp, path);
      return false;
    }

    RateLimit limit = findRateLimit(path);
    if (limit == null) {
      return true; // No rate limit configured
    }

    // Check rate limit
    return !isRateLimitExceeded(clientIp, path, limit);
  }

  /**
   * Check if the rate limit has been exceeded for the given IP and path.
   *
   * @param clientIp the client IP address
   * @param path the request path
   * @param limit the rate limit configuration
   * @return true if rate limit exceeded, false otherwise
   */
  public boolean isRateLimitExceeded(String clientIp, String path, RateLimit limit) {
    RequestCounter counter = getOrCreateCounter(clientIp);
    long windowSizeMillis = limit.timeUnit().toMillis(limit.limit());

    // Clean old entries
    counter.cleanup(windowSizeMillis);

    // Check if limit exceeded
    boolean exceeded = counter.increment() > limit.limit();

    if (exceeded) {
      log.warn(
          "Rate limit exceeded for IP {} on path {}: {} requests in {}",
          clientIp,
          path,
          counter.getCount(),
          limit.timeUnit());
    }

    return exceeded;
  }

  /**
   * Block an IP address for the specified duration.
   *
   * @param clientIp the IP address to block
   * @param blockDurationMillis duration to block in milliseconds
   */
  public void blockIp(String clientIp, long blockDurationMillis) {
    blockedIps.put(
        clientIp,
        new BlockedIp(System.currentTimeMillis() + blockDurationMillis, blockDurationMillis));
    log.warn("Blocked IP {} for {} milliseconds", clientIp, blockDurationMillis);
  }

  /**
   * Unblock an IP address.
   *
   * @param clientIp the IP address to unblock
   */
  public void unblockIp(String clientIp) {
    blockedIps.remove(clientIp);
    log.info("Unblocked IP {}", clientIp);
  }

  /**
   * Check if an IP address is currently blocked.
   *
   * @param clientIp the IP address to check
   * @return true if blocked, false otherwise
   */
  public boolean isIpBlocked(String clientIp) {
    BlockedIp blocked = blockedIps.get(clientIp);
    if (blocked == null) {
      return false;
    }
    return System.currentTimeMillis() < blocked.blockUntil;
  }

  /**
   * Get the remaining time until the IP is unblocked.
   *
   * @param clientIp the IP address to check
   * @return remaining milliseconds, or 0 if not blocked
   */
  public long getRemainingBlockTime(String clientIp) {
    BlockedIp blocked = blockedIps.get(clientIp);
    if (blocked == null) {
      return 0;
    }
    long remaining = blocked.blockUntil - System.currentTimeMillis();
    return remaining > 0 ? remaining : 0;
  }

  /**
   * Get current request count for an IP.
   *
   * @param clientIp the IP address
   * @return current request count
   */
  public int getRequestCount(String clientIp) {
    RequestCounter counter = getOrCreateCounter(clientIp);
    return counter.getCount();
  }

  /**
   * Reset request counter for an IP.
   *
   * @param clientIp the IP address to reset
   */
  public void resetRequestCount(String clientIp) {
    requestCounters.remove(clientIp);
    log.info("Reset request count for IP {}", clientIp);
  }

  /** Cleanup expired blocked IPs. Scheduled to run every hour. */
  @Scheduled(fixedDelay = 3600000) // 1 hour
  public void cleanupExpiredBlocks() {
    long now = System.currentTimeMillis();
    blockedIps
        .entrySet()
        .removeIf(
            entry -> {
              if (now >= entry.getValue().blockUntil) {
                log.info("Removed expired block for IP {}", entry.getKey());
                return true;
              }
              return false;
            });
  }

  /**
   * Find the rate limit configuration for a given path. Matches the path against configured rate
   * limits.
   *
   * @param path the request path
   * @return the rate limit configuration, or null if not found
   */
  private RateLimit findRateLimit(String path) {
    // Exact match first
    if (rateLimits.containsKey(path)) {
      return rateLimits.get(path);
    }
    // Try to find a prefix match
    for (Map.Entry<String, RateLimit> entry : rateLimits.entrySet()) {
      if (path.startsWith(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private RequestCounter getOrCreateCounter(String clientIp) {
    return requestCounters.computeIfAbsent(clientIp, k -> new RequestCounter());
  }

  private static class BlockedIp {
    private final long blockUntil;
    private final long duration;

    public BlockedIp(long blockUntil, long duration) {
      this.blockUntil = blockUntil;
      this.duration = duration;
    }
  }

  private static class RequestCounter {
    private final Map<Instant, AtomicInteger> timestamps = new ConcurrentHashMap<>();

    public void cleanup(long windowSizeMillis) {
      Instant now = Instant.now();
      timestamps
          .entrySet()
          .removeIf(entry -> ChronoUnit.MILLIS.between(entry.getKey(), now) > windowSizeMillis);
    }

    public int increment() {
      Instant now = Instant.now();
      timestamps.put(now, new AtomicInteger(1));

      // Count requests in the current window (1 minute default)
      long windowSizeMillis = 60000;
      timestamps
          .entrySet()
          .removeIf(entry -> ChronoUnit.MILLIS.between(entry.getKey(), now) > windowSizeMillis);

      return (int) timestamps.values().stream().mapToInt(AtomicInteger::get).sum();
    }

    public int getCount() {
      return (int) timestamps.values().stream().mapToInt(AtomicInteger::get).sum();
    }
  }
}
