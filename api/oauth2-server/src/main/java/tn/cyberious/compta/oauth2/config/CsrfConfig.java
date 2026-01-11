package tn.cyberious.compta.oauth2.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

/**
 * Configuration for CSRF (Cross-Site Request Forgery) protection.
 *
 * <p>OAuth2 endpoints are excluded from CSRF protection as they use other security mechanisms
 * (PKCE, client secrets, etc.). However, CSRF protection is enabled for form-based authentication
 * endpoints (login, logout, user management).
 */
@Configuration
public class CsrfConfig {

  /**
   * Custom CSRF token repository that stores tokens in cookies for SPA compatibility. This allows
   * Single Page Applications (SPAs) to include CSRF tokens in requests via the X-XSRF-TOKEN header.
   */
  @Bean
  public CsrfTokenRepository csrfTokenRepository() {
    return new CustomCsrfTokenRepository();
  }

  /**
   * Custom CSRF token repository implementation that stores tokens in cookies. This is more
   * suitable for SPAs and modern web applications.
   */
  public static class CustomCsrfTokenRepository implements CsrfTokenRepository {
    private static final String CSRF_PARAM_NAME = "_csrf";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    private final ConcurrentMap<String, CsrfToken> tokenStore = new ConcurrentHashMap<>();

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
      String tokenValue = UUID.randomUUID().toString();
      return new org.springframework.security.web.csrf.DefaultCsrfToken(
          CSRF_HEADER_NAME, CSRF_PARAM_NAME, tokenValue);
    }

    @Override
    public void saveToken(
        CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
      String sessionKey = getSessionKey(request);

      if (token == null) {
        tokenStore.remove(sessionKey);
        // Remove cookie
        Cookie cookie = new Cookie(CSRF_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      } else {
        tokenStore.put(sessionKey, token);
        // Set cookie with SameSite=Strict for security
        Cookie cookie = new Cookie(CSRF_COOKIE_NAME, token.getToken());
        cookie.setPath("/");
        cookie.setHttpOnly(false); // Must be accessible by JavaScript for SPAs
        cookie.setSecure(request.isSecure()); // Only send over HTTPS in production
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
      }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
      String sessionKey = getSessionKey(request);
      return tokenStore.get(sessionKey);
    }

    /**
     * Generate a session key for storing CSRF tokens. Uses the session ID if available, otherwise
     * uses the client IP address.
     */
    private String getSessionKey(HttpServletRequest request) {
      String sessionId = request.getSession(false) != null ? request.getSession().getId() : null;

      if (sessionId != null) {
        return "csrf:" + sessionId;
      }

      // Fallback to IP address for stateless requests
      return "csrf:ip:" + request.getRemoteAddr();
    }
  }

  /**
   * Alternative CSRF token repository using HTTP session. This is the traditional approach and is
   * suitable for server-rendered applications.
   */
  @Bean
  public CsrfTokenRepository sessionCsrfTokenRepository() {
    HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
    // Note: setHeaderName, setCookieName, setParameterName are not available in Spring Security 6
    // The defaults will be used: header name = "X-CSRF-TOKEN", cookie name = "XSRF-TOKEN",
    // parameter name = "_csrf"
    return repository;
  }

  /** CSRF token repository for server-side rendered applications. Uses HTTP session for storage. */
  public static class SessionBasedCsrfTokenRepository implements CsrfTokenRepository {
    private static final String CSRF_PARAM_NAME = "_csrf";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    private final HttpSessionCsrfTokenRepository delegate = new HttpSessionCsrfTokenRepository();

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
      String tokenValue = UUID.randomUUID().toString();
      return new org.springframework.security.web.csrf.DefaultCsrfToken(
          CSRF_HEADER_NAME, CSRF_PARAM_NAME, tokenValue);
    }

    @Override
    public void saveToken(
        CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
      delegate.saveToken(token, request, response);
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
      return delegate.loadToken(request);
    }
  }
}
