package tn.compta.commons.security.resolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import tn.compta.commons.security.annotation.AuthenticatedUser;
import tn.compta.commons.security.context.SecurityContext;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.exception.UnauthorizedException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

class AuthenticatedUserArgumentResolverTest {

  private AuthenticatedUserArgumentResolver resolver;
  private MethodParameter methodParameter;

  @BeforeEach
  void setUp() {
    resolver = new AuthenticatedUserArgumentResolver();
    methodParameter = mock(MethodParameter.class);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clear();
  }

  @Test
  void supportsParameter_whenParameterHasAuthenticatedUserAnnotationAndCorrectType_returnsTrue() {
    when(methodParameter.hasParameterAnnotation(AuthenticatedUser.class)).thenReturn(true);
    when(methodParameter.getParameterType()).thenReturn((Class) AuthenticatedUserDetails.class);

    assertTrue(resolver.supportsParameter(methodParameter));
  }

  @Test
  void supportsParameter_whenParameterLacksAnnotation_returnsFalse() {
    when(methodParameter.hasParameterAnnotation(AuthenticatedUser.class)).thenReturn(false);

    assertFalse(resolver.supportsParameter(methodParameter));
  }

  @Test
  void resolveArgument_whenUserIsAuthenticated_returnsUserDetails() {
    AuthenticatedUserDetails user =
        AuthenticatedUserDetails.builder()
            .userId(1L)
            .username("testuser")
            .email("test@example.com")
            .roles(List.of("USER"))
            .build();

    SecurityContext context = SecurityContext.builder().user(user).authenticated(true).build();

    SecurityContextHolder.setContext(context);

    Object result = resolver.resolveArgument(methodParameter, null, null, null);

    assertNotNull(result);
    assertEquals(user, result);
  }

  @Test
  void resolveArgument_whenUserIsNotAuthenticated_throwsUnauthorizedException() {
    SecurityContextHolder.clear();

    assertThrows(
        UnauthorizedException.class,
        () -> resolver.resolveArgument(methodParameter, null, null, null));
  }
}
