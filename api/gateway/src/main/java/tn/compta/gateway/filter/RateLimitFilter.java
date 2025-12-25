package tn.compta.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting filter using in-memory storage.
 * For production with multiple gateway instances, consider using Redis-based rate limiting.
 */
@Slf4j
@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    @Value("${gateway.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${gateway.rate-limit.requests-per-second:10}")
    private int requestsPerSecond;

    // In-memory storage for rate limiting (use Redis for distributed systems)
    private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String clientId = getClientIdentifier(request);
            String path = request.getURI().getPath();

            RateLimiter limiter = rateLimiters.computeIfAbsent(clientId, k -> new RateLimiter());

            if (limiter.tryAcquire(requestsPerMinute, requestsPerSecond)) {
                log.debug("Rate limit check passed for client: {}, path: {}", clientId, path);

                // Add rate limit headers
                exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(limiter.getRemainingRequests()));

                return chain.filter(exchange);
            } else {
                log.warn("Rate limit exceeded for client: {}, path: {}", clientId, path);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                exchange.getResponse().getHeaders().add("Retry-After", "60");

                String errorResponse = String.format(
                    "{\"error\":\"Rate limit exceeded\",\"status\":429,\"limit\":%d}",
                    requestsPerMinute
                );

                return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
                );
            }
        };
    }

    private String getClientIdentifier(ServerHttpRequest request) {
        // Try to get user ID from headers (set by JwtAuthenticationFilter)
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        // Fall back to IP address
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return "ip:" + ip.split(",")[0].trim();
        }

        ip = request.getHeaders().getFirst("X-Real-IP");
        if (ip != null && !ip.isEmpty()) {
            return "ip:" + ip;
        }

        return "ip:" + (request.getRemoteAddress() != null
            ? request.getRemoteAddress().getAddress().getHostAddress()
            : "unknown");
    }

    /**
     * Rate limiter using token bucket algorithm
     */
    private static class RateLimiter {
        private final AtomicInteger tokens;
        private final AtomicInteger requestCount;
        private long lastRefillTime;
        private final long refillInterval = 60_000; // 1 minute in milliseconds

        public RateLimiter() {
            this.tokens = new AtomicInteger(60); // Start with full bucket
            this.requestCount = new AtomicInteger(0);
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryAcquire(int requestsPerMinute, int requestsPerSecond) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRefill = currentTime - lastRefillTime;

            // Refill tokens based on time elapsed
            if (timeSinceLastRefill >= refillInterval) {
                tokens.set(requestsPerMinute);
                requestCount.set(0);
                lastRefillTime = currentTime;
            } else if (timeSinceLastRefill >= 1_000) {
                // Add tokens proportionally to time elapsed (for per-second limiting)
                int tokensToAdd = (int) (timeSinceLastRefill / 1_000);
                int newTokens = Math.min(requestsPerMinute, tokens.get() + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime = currentTime;
            }

            // Check if we have tokens available
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                requestCount.incrementAndGet();
                return true;
            }

            return false;
        }

        public int getRemainingRequests() {
            return tokens.get();
        }
    }

    public static class Config {
        // Configuration properties if needed
    }
}
