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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IpWhitelistFilter extends AbstractGatewayFilterFactory<IpWhitelistFilter.Config> {

    @Value("${gateway.ip.whitelist:}")
    private String ipWhitelistConfig;

    public IpWhitelistFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String clientIp = getClientIp(request);

            if (isIpAllowed(clientIp)) {
                log.debug("IP {} is allowed", clientIp);
                return chain.filter(exchange);
            } else {
                log.warn("IP {} is not in whitelist", clientIp);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                String errorResponse = "{\"error\":\"IP address not allowed\",\"status\":403}";
                return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
                );
            }
        };
    }

    private String getClientIp(ServerHttpRequest request) {
        // Check X-Forwarded-For header first (for proxy/load balancer scenarios)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return request.getRemoteAddress() != null
            ? request.getRemoteAddress().getAddress().getHostAddress()
            : "unknown";
    }

    private boolean isIpAllowed(String ip) {
        Set<String> allowedIps = getAllowedIps();

        // If whitelist is empty or contains "*", allow all IPs
        if (allowedIps.isEmpty() || allowedIps.contains("*")) {
            return true;
        }

        // Check exact match
        if (allowedIps.contains(ip)) {
            return true;
        }

        // Check CIDR ranges (simple implementation for common cases)
        for (String allowedIp : allowedIps) {
            if (allowedIp.contains("/")) {
                if (isIpInCidr(ip, allowedIp)) {
                    return true;
                }
            } else if (allowedIp.endsWith(".*")) {
                // Wildcard matching (e.g., 192.168.1.*)
                String prefix = allowedIp.substring(0, allowedIp.length() - 1);
                if (ip.startsWith(prefix)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<String> getAllowedIps() {
        if (ipWhitelistConfig == null || ipWhitelistConfig.trim().isEmpty()) {
            return Set.of("*"); // Allow all if not configured
        }
        return List.of(ipWhitelistConfig.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private boolean isIpInCidr(String ip, String cidr) {
        // Simple CIDR validation for IPv4
        // For production, consider using a proper CIDR library
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }

        String network = parts[0];
        int prefixLength;
        try {
            prefixLength = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        String[] ipParts = ip.split("\\.");
        String[] networkParts = network.split("\\.");

        if (ipParts.length != 4 || networkParts.length != 4) {
            return false;
        }

        int mask = 0xFFFFFFFF << (32 - prefixLength);
        int ipInt = (Integer.parseInt(ipParts[0]) << 24) |
                    (Integer.parseInt(ipParts[1]) << 16) |
                    (Integer.parseInt(ipParts[2]) << 8) |
                    Integer.parseInt(ipParts[3]);
        int networkInt = (Integer.parseInt(networkParts[0]) << 24) |
                         (Integer.parseInt(networkParts[1]) << 16) |
                         (Integer.parseInt(networkParts[2]) << 8) |
                         Integer.parseInt(networkParts[3]);

        return (ipInt & mask) == (networkInt & mask);
    }

    public static class Config {
        // Configuration properties if needed
    }
}
