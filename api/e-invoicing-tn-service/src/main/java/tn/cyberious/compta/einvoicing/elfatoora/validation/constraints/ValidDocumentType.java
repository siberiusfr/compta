package tn.cyberious.compta.einvoicing.elfatoora.validation.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation de validation pour les types de documents El Fatoora.
 *
 * <p>Valide que le code correspond à un type de document valide (I-11 à I-16).
 */
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidDocumentTypeValidator.class)
@Documented
public @interface ValidDocumentType {

  /**
   * Message d'erreur par défaut.
   *
   * @return Le message d'erreur
   */
  String message() default
      "Type de document invalide. Valeurs autorisées: I-11 (Facture), I-12 (Avoir), I-13"
          + " (Débit), I-14 (Simplifiée), I-15 (Auto-facturation), I-16 (Rectificative)";

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
