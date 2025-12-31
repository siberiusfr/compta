package tn.compta.commons.security.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import tn.compta.commons.security.annotation.AuthenticatedUser;

/**
 * Customizer for Swagger/OpenAPI to hide {@link AuthenticatedUser} parameters from documentation.
 *
 * <p>This customizer removes parameters annotated with {@link AuthenticatedUser} from the OpenAPI
 * documentation because they are automatically injected by the {@link
 * tn.compta.commons.security.resolver.AuthenticatedUserArgumentResolver} and should not be exposed
 * as request parameters in Swagger UI.
 *
 * <p>This component is only active when SpringDoc OpenAPI is available on the classpath.
 */
@Order(1)
public class AuthenticatedUserParameterCustomizer implements OperationCustomizer {

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    if (operation.getParameters() == null) {
      return operation;
    }

    // Filter out parameters that are annotated with @AuthenticatedUser
    List<Parameter> filteredParameters =
        operation.getParameters().stream()
            .filter(
                param -> {
                  // Check if the parameter name corresponds to a method parameter annotated with
                  // @AuthenticatedUser
                  return !isAuthenticatedUserParameter(handlerMethod, param.getName());
                })
            .collect(Collectors.toList());

    operation.setParameters(filteredParameters.isEmpty() ? null : filteredParameters);
    return operation;
  }

  /**
   * Checks if a parameter with the given name is annotated with {@link AuthenticatedUser}.
   *
   * @param handlerMethod the controller method handler
   * @param parameterName the parameter name to check
   * @return true if the parameter is annotated with @AuthenticatedUser, false otherwise
   */
  private boolean isAuthenticatedUserParameter(HandlerMethod handlerMethod, String parameterName) {
    for (java.lang.reflect.Parameter param : handlerMethod.getMethod().getParameters()) {
      if (param.isAnnotationPresent(AuthenticatedUser.class)) {
        // Spring MVC parameter names are typically the same as the method parameter names
        // If they match, this is an @AuthenticatedUser parameter
        return param.getName().equals(parameterName);
      }
    }
    return false;
  }
}
