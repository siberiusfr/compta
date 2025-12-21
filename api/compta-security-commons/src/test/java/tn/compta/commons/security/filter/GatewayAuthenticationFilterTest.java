package tn.compta.commons.security.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.model.AuthenticatedUserDetails;
import tn.compta.commons.security.util.JwtValidator;

class GatewayAuthenticationFilterTest {

  private GatewayAuthenticationFilter filter;
  private JwtValidator jwtValidator;
  private ObjectMapper objectMapper;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    jwtValidator = mock(JwtValidator.class);
    objectMapper = new ObjectMapper();
    filter = new GatewayAuthenticationFilter(jwtValidator, objectMapper, List.of("/actuator/**"));

    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);
  }

  @Test
  void doFilterInternal_whenGatewayHeadersPresent_extractsUserFromHeaders() throws Exception {
    when(request.getHeader("X-User-Id")).thenReturn("1");
    when(request.getHeader("X-User-Username")).thenReturn("testuser");
    when(request.getHeader("X-User-Email")).thenReturn("test@example.com");
    when(request.getHeader("X-User-Roles")).thenReturn("ADMIN,USER");
    when(request.getRequestURI()).thenReturn("/api/test");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    // Context should be cleared after filter
    assertNull(SecurityContextHolder.getContext());
  }

  @Test
  void doFilterInternal_whenJwtTokenPresent_extractsUserFromToken() throws Exception {
    String token = "test.jwt.token";
    AuthenticatedUserDetails user =
        AuthenticatedUserDetails.builder()
            .userId(1L)
            .username("testuser")
            .email("test@example.com")
            .roles(List.of("USER"))
            .build();

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(request.getRequestURI()).thenReturn("/api/test");
    when(jwtValidator.validateAndExtract(token)).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    verify(jwtValidator).validateAndExtract(token);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotFilter_whenPathIsPublic_returnsTrue() throws Exception {
    when(request.getRequestURI()).thenReturn("/actuator/health");

    assertTrue(filter.shouldNotFilter(request));
  }

  @Test
  void shouldNotFilter_whenPathIsNotPublic_returnsFalse() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/users");

    assertFalse(filter.shouldNotFilter(request));
  }
}
