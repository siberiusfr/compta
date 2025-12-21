package tn.compta.commons.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.compta.commons.security.context.SecurityContext;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.exception.UnauthorizedException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;
import tn.compta.commons.security.model.UserPermission;

/**
 * Filter that extracts authentication information from API Gateway headers.
 *
 * <p>This filter extracts user information from X-User-* headers set by the API Gateway after JWT
 * validation.
 *
 * <p>The extracted user details are stored in SecurityContextHolder for the request lifecycle.
 */
@Slf4j
@RequiredArgsConstructor
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

  private static final String HEADER_USER_ID = "X-User-Id";
  private static final String HEADER_USERNAME = "X-User-Username";
  private static final String HEADER_EMAIL = "X-User-Email";
  private static final String HEADER_ROLES = "X-User-Roles";
  private static final String HEADER_SOCIETE_IDS = "X-User-Societe-Ids";
  private static final String HEADER_PRIMARY_SOCIETE_ID = "X-User-Primary-Societe-Id";
  private static final String HEADER_PERMISSIONS = "X-User-Permissions";
  private static final String HEADER_REQUEST_ID = "X-Request-Id";

  private final List<String> publicPaths;

  public GatewayAuthenticationFilter() {
    this(Arrays.asList("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String requestId = extractRequestId(request);
      AuthenticatedUserDetails user = null;
      boolean authenticated = false;

      // Extract from gateway headers
      if (hasGatewayHeaders(request)) {
        user = extractFromGatewayHeaders(request);
        authenticated = true;
        log.debug("Authenticated user from gateway headers: {}", user.getUsername());
      }

      // Build security context
      SecurityContext context =
          SecurityContext.builder()
              .user(user)
              .requestId(requestId)
              .authenticated(authenticated)
              .build();

      SecurityContextHolder.setContext(context);

      filterChain.doFilter(request, response);

    } catch (UnauthorizedException e) {
      log.warn("Authentication failed: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response
          .getWriter()
          .write(String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", e.getMessage()));
    } catch (Exception e) {
      log.error("Error in authentication filter", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Internal Server Error\"}");
    } finally {
      // Always clear context at the end of the request
      SecurityContextHolder.clear();
    }
  }

  /**
   * Check if the request should be filtered.
   *
   * @param request the HTTP request
   * @return true if the request should not be filtered
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return publicPaths.stream().anyMatch(pattern -> matchesPattern(path, pattern));
  }

  /**
   * Check if a path matches a pattern (supports ** wildcard).
   *
   * @param path the path
   * @param pattern the pattern
   * @return true if matches
   */
  private boolean matchesPattern(String path, String pattern) {
    if (pattern.endsWith("/**")) {
      String prefix = pattern.substring(0, pattern.length() - 3);
      return path.startsWith(prefix);
    }
    return path.equals(pattern);
  }

  /**
   * Extract request ID from header or generate new one.
   *
   * @param request the HTTP request
   * @return the request ID
   */
  private String extractRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(HEADER_REQUEST_ID);
    if (requestId == null || requestId.isEmpty()) {
      requestId = UUID.randomUUID().toString();
    }
    return requestId;
  }

  /**
   * Check if the request has gateway headers.
   *
   * @param request the HTTP request
   * @return true if gateway headers are present
   */
  private boolean hasGatewayHeaders(HttpServletRequest request) {
    return request.getHeader(HEADER_USER_ID) != null || request.getHeader(HEADER_USERNAME) != null;
  }

  /**
   * Extract user details from gateway headers.
   *
   * @param request the HTTP request
   * @return the authenticated user details
   */
  private AuthenticatedUserDetails extractFromGatewayHeaders(HttpServletRequest request) {
    Long userId = parseLong(request.getHeader(HEADER_USER_ID));
    String username = request.getHeader(HEADER_USERNAME);
    String email = request.getHeader(HEADER_EMAIL);

    List<String> roles = parseList(request.getHeader(HEADER_ROLES));
    List<Long> societeIds = parseLongList(request.getHeader(HEADER_SOCIETE_IDS));
    Long primarySocieteId = parseLong(request.getHeader(HEADER_PRIMARY_SOCIETE_ID));

    List<UserPermission> permissions = parsePermissions(request.getHeader(HEADER_PERMISSIONS));

    return AuthenticatedUserDetails.builder()
        .userId(userId)
        .username(username)
        .email(email)
        .roles(roles)
        .permissions(permissions)
        .societeIds(societeIds)
        .primarySocieteId(primarySocieteId)
        .active(true)
        .locked(false)
        .build();
  }

  /**
   * Parse a Long from a string.
   *
   * @param value the string value
   * @return the Long, or null if invalid
   */
  private Long parseLong(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Parse a comma-separated list.
   *
   * @param value the string value
   * @return the list
   */
  private List<String> parseList(String value) {
    if (value == null || value.isEmpty()) {
      return List.of();
    }
    return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  /**
   * Parse a comma-separated list of Longs.
   *
   * @param value the string value
   * @return the list of Longs
   */
  private List<Long> parseLongList(String value) {
    if (value == null || value.isEmpty()) {
      return List.of();
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Long::parseLong)
        .toList();
  }

  /**
   * Parse permissions from a comma-separated list.
   *
   * @param value the string value
   * @return the list of permissions
   */
  private List<UserPermission> parsePermissions(String value) {
    if (value == null || value.isEmpty()) {
      return List.of();
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(UserPermission::of)
        .toList();
  }
}
