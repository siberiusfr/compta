package tn.cyberious.compta.einvoicing.elfatoora.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un avertissement de validation El Fatoora.
 *
 * <p>Les avertissements n'empêchent pas la génération mais signalent des problèmes potentiels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationWarning {

  /** Chemin du champ concerné (ex: "customer.address.postalCode") */
  private String field;

  /** Code d'avertissement technique (ex: "ELF_WARN_MISSING_POSTAL_CODE") */
  private String code;

  /** Message d'avertissement lisible en français */
  private String message;

  /**
   * Crée un avertissement simple.
   *
   * @param field Le champ concerné
   * @param code Le code d'avertissement
   * @param message Le message d'avertissement
   * @return L'avertissement créé
   */
  public static ValidationWarning of(String field, String code, String message) {
    return ValidationWarning.builder().field(field).code(code).message(message).build();
  }

  /**
   * Formate l'avertissement pour affichage.
   *
   * @return Le message formaté
   */
  public String toFormattedString() {
    return "[" + code + "] " + field + ": " + message;
  }
}
