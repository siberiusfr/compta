package tn.cyberious.compta.einvoicing.elfatoora.validation.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation de validation pour les codes pays ISO 3166-1.
 *
 * <p>Valide que le code est un code pays ISO 3166-1 alpha-2 valide.
 */
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidCountryCodeValidator.class)
@Documented
public @interface ValidCountryCode {

  /**
   * Message d'erreur par défaut.
   *
   * @return Le message d'erreur
   */
  String message() default "Code pays invalide. Format attendu: ISO 3166-1 alpha-2 (ex: TN, FR)";

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
