package tn.compta.commons.security.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import tn.compta.commons.security.context.SecurityContext;
import tn.compta.commons.security.context.SecurityContextHolder;

/**
 * Argument resolver that injects the security context into controller methods.
 *
 * <p>This resolver supports method parameters of type {@link SecurityContext}.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;GetMapping("/info")
 * public RequestInfo getInfo(SecurityContext context) {
 *     return RequestInfo.builder()
 *         .userId(context.getUserId())
 *         .requestId(context.getRequestId())
 *         .build();
 * }
 * </pre>
 */
public class SecurityContextArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(SecurityContext.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {

    SecurityContext context = SecurityContextHolder.getContext();

    // Return an empty context if not authenticated (for optional injection)
    if (context == null) {
      return SecurityContext.builder().authenticated(false).build();
    }

    return context;
  }
}
