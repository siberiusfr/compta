package tn.cyberious.compta.oauth2.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.cyberious.compta.oauth2.config.RateLimitConfig.RateLimit;
import tn.cyberious.compta.oauth2.metrics.OAuth2Metrics;

/**
 * Filter for rate limiting on OAuth2 endpoints.
 *
 * <p>Rate limits: - /oauth2/token: 10 requests per minute per IP - /oauth2/revoke: 20 requests per
 * minute per IP - /oauth2/introspect: 100 requests per minute per IP - /login: 5 requests per
 * minute per IP - /api/users/password/reset: 3 requests per hour per email
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

  private final Map<String, RateLimit> rateLimits;
  private final OAuth2Metrics oauth2Metrics;

  private static final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();

  private static final Map<String, Instant> blockedUntil = new ConcurrentHashMap<>();

  public RateLimitFilter(Map<String, RateLimit> rateLimits, OAuth2Metrics oauth2Metrics) {
    this.rateLimits = rateLimits;
    this.oauth2Metrics = oauth2Metrics;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    String clientIp = getClientIp(request);

    // Check if IP is blocked
    if (isIpBlocked(clientIp)) {
      sendRateLimitExceededResponse(response);
      return;
    }

    // Check rate limit
    RateLimit limit = findRateLimit(path);
    if (limit != null && !checkRateLimit(clientIp, path, limit)) {
      sendRateLimitExceededResponse(response);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean checkRateLimit(String clientIp, String path, RateLimit limit) {
    RequestCounter counter = requestCounters.computeIfAbsent(clientIp, k -> new RequestCounter());

    long windowSizeMillis = limit.timeUnit().toMillis(limit.limit());

    // Clean old entries
    counter.cleanup(windowSizeMillis);

    // Check if limit exceeded
    return counter.increment() > limit.limit();
  }

  private RateLimit findRateLimit(String path) {
    for (Map.Entry<String, RateLimit> entry : rateLimits.entrySet()) {
      if (path.startsWith(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty()) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty()) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  private boolean isIpBlocked(String clientIp) {
    Instant blocked = blockedUntil.get(clientIp);
    return blocked != null && Instant.now().isBefore(blocked);
  }

  private void sendRateLimitExceededResponse(HttpServletResponse response) throws IOException {
    // Record metric
    oauth2Metrics.recordRateLimitExceeded();

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType("application/json");
    response
        .getWriter()
        .write(
            "{\"error\":\"rate_limit_exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
  }

  private void blockIp(String clientIp, long blockDurationMillis) {
    blockedUntil.put(clientIp, Instant.now().plusMillis(blockDurationMillis));
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

      // Count requests in the current window
      long windowSizeMillis = 60000; // 1 minute default
      timestamps
          .entrySet()
          .removeIf(entry -> ChronoUnit.MILLIS.between(entry.getKey(), now) > windowSizeMillis);

      return (int) timestamps.values().stream().mapToInt(AtomicInteger::get).sum();
    }
  }
}
