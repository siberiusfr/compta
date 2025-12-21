package tn.compta.commons.security.interceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;
import tn.compta.commons.security.annotation.Public;
import tn.compta.commons.security.annotation.RequireRole;
import tn.compta.commons.security.context.SecurityContext;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.exception.ForbiddenException;
import tn.compta.commons.security.exception.UnauthorizedException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

class RoleCheckInterceptorTest {

  private RoleCheckInterceptor interceptor;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private HandlerMethod handlerMethod;

  @BeforeEach
  void setUp() {
    interceptor = new RoleCheckInterceptor();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    handlerMethod = mock(HandlerMethod.class);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clear();
  }

  @Test
  void preHandle_whenNoAnnotation_allowsAccess() throws Exception {
    when(handlerMethod.getMethodAnnotation(RequireRole.class)).thenReturn(null);
    when(handlerMethod.getBeanType()).thenReturn((Class) Object.class);

    assertTrue(interceptor.preHandle(request, response, handlerMethod));
  }

  @Test
  void preHandle_whenPublicAnnotation_allowsAccess() throws Exception {
    when(handlerMethod.getMethodAnnotation(Public.class)).thenReturn(mock(Public.class));

    assertTrue(interceptor.preHandle(request, response, handlerMethod));
  }

  @Test
  void preHandle_whenUserHasRequiredRole_allowsAccess() throws Exception {
    RequireRole requireRole = mock(RequireRole.class);
    when(requireRole.value()).thenReturn(new String[]{"ADMIN"});
    when(requireRole.requireAll()).thenReturn(false);

    when(handlerMethod.getMethodAnnotation(RequireRole.class)).thenReturn(requireRole);
    when(handlerMethod.getMethodAnnotation(Public.class)).thenReturn(null);
    when(handlerMethod.getBeanType()).thenReturn((Class) Object.class);

    AuthenticatedUserDetails user =
        AuthenticatedUserDetails.builder()
            .userId(1L)
            .username("admin")
            .roles(List.of("ADMIN"))
            .build();

    SecurityContext context = SecurityContext.builder().user(user).authenticated(true).build();
    SecurityContextHolder.setContext(context);

    assertTrue(interceptor.preHandle(request, response, handlerMethod));
  }

  @Test
  void preHandle_whenUserLacksRequiredRole_throwsForbiddenException() {
    RequireRole requireRole = mock(RequireRole.class);
    when(requireRole.value()).thenReturn(new String[]{"ADMIN"});
    when(requireRole.requireAll()).thenReturn(false);

    when(handlerMethod.getMethodAnnotation(RequireRole.class)).thenReturn(requireRole);
    when(handlerMethod.getMethodAnnotation(Public.class)).thenReturn(null);
    when(handlerMethod.getBeanType()).thenReturn((Class) Object.class);

    AuthenticatedUserDetails user =
        AuthenticatedUserDetails.builder()
            .userId(1L)
            .username("user")
            .roles(List.of("USER"))
            .build();

    SecurityContext context = SecurityContext.builder().user(user).authenticated(true).build();
    SecurityContextHolder.setContext(context);

    assertThrows(
        ForbiddenException.class, () -> interceptor.preHandle(request, response, handlerMethod));
  }

  @Test
  void preHandle_whenNotAuthenticated_throwsUnauthorizedException() {
    RequireRole requireRole = mock(RequireRole.class);
    when(requireRole.value()).thenReturn(new String[]{"ADMIN"});

    when(handlerMethod.getMethodAnnotation(RequireRole.class)).thenReturn(requireRole);
    when(handlerMethod.getMethodAnnotation(Public.class)).thenReturn(null);
    when(handlerMethod.getBeanType()).thenReturn((Class) Object.class);

    SecurityContextHolder.clear();

    assertThrows(
        UnauthorizedException.class, () -> interceptor.preHandle(request, response, handlerMethod));
  }
}
