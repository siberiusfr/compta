package tn.cyberious.compta.einvoicing.elfatoora.validation.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation de validation pour les taux de TVA tunisiens.
 *
 * <p>Valide que le taux correspond aux taux autorisés: 0%, 7%, 13%, 19%.
 */
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidTaxRateValidator.class)
@Documented
public @interface ValidTaxRate {

  /**
   * Message d'erreur par défaut.
   *
   * @return Le message d'erreur
   */
  String message() default "Taux de TVA invalide. Taux autorisés: 0%, 7%, 13%, 19%";

  /**
   * Groupes de validation.
   *
   * @return Les groupes
   */
  Class<?>[] groups() default {};

  /**
   * Payload pour métadonnées.
   *
   * @return Le payload
   */
  Class<? extends Payload>[] payload() default {};
}
