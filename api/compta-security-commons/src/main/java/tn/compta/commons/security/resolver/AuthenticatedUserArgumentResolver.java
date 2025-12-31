package tn.compta.commons.security.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import tn.compta.commons.security.annotation.AuthenticatedUser;
import tn.compta.commons.security.context.SecurityContextHolder;
import tn.compta.commons.security.exception.UnauthorizedException;
import tn.compta.commons.security.model.AuthenticatedUserDetails;

/**
 * Argument resolver that injects the authenticated user into controller methods.
 *
 * <p>This resolver supports method parameters annotated with {@link AuthenticatedUser}.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;GetMapping("/profile")
 * public UserProfile getProfile(&#64;AuthenticatedUser AuthenticatedUserDetails user) {
 *     return userService.getProfile(user.getUserId());
 * }
 * </pre>
 */
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(AuthenticatedUser.class)
        && parameter.getParameterType().equals(AuthenticatedUserDetails.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {

    AuthenticatedUserDetails user = SecurityContextHolder.getUser();

    if (user == null) {
      throw new UnauthorizedException("User not authenticated");
    }

    return user;
  }
}
