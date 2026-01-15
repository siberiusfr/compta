package tn.cyberious.compta.einvoicing.elfatoora.validation.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;

/**
 * Annotation de validation pour les identifiants fiscaux tunisiens.
 *
 * <p>Valide que la valeur correspond au format du type d'identifiant spécifié.
 *
 * <p>Exemple d'utilisation:
 *
 * <pre>{@code
 * @ValidTaxIdentifier(type = IdentifierType.I_01)
 * private String matriculeFiscal;
 *
 * @ValidTaxIdentifier(type = IdentifierType.I_02)
 * private String cin;
 * }</pre>
 */
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidTaxIdentifierValidator.class)
@Documented
public @interface ValidTaxIdentifier {

  /**
   * Le type d'identifiant attendu.
   *
   * @return Le type d'identifiant
   */
  IdentifierType type() default IdentifierType.I_01;

  /**
   * Message d'erreur par défaut.
   *
   * @return Le message d'erreur
   */
  String message() default "Identifiant fiscal invalide";

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

  /** Permet de définir plusieurs annotations sur le même élément. */
  @Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    ValidTaxIdentifier[] value();
  }
}
