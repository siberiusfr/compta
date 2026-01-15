package tn.cyberious.compta.einvoicing.elfatoora.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente une erreur de validation El Fatoora.
 *
 * <p>Contient toutes les informations nécessaires pour diagnostiquer et corriger l'erreur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

  /** Chemin du champ en erreur (ex: "supplier.taxIdentifier") */
  private String field;

  /** Code d'erreur technique (ex: "ELF_INVALID_TAX_ID") */
  private String code;

  /** Message d'erreur lisible en français */
  private String message;

  /** Détails supplémentaires (ex: format attendu) */
  private String detail;

  /** La valeur qui a été rejetée */
  private Object invalidValue;

  /**
   * Crée une erreur simple sans valeur invalide.
   *
   * @param field Le champ en erreur
   * @param code Le code d'erreur
   * @param message Le message d'erreur
   * @return L'erreur créée
   */
  public static ValidationError of(String field, String code, String message) {
    return ValidationError.builder().field(field).code(code).message(message).build();
  }

  /**
   * Crée une erreur avec valeur invalide.
   *
   * @param field Le champ en erreur
   * @param code Le code d'erreur
   * @param message Le message d'erreur
   * @param invalidValue La valeur rejetée
   * @return L'erreur créée
   */
  public static ValidationError of(String field, String code, String message, Object invalidValue) {
    return ValidationError.builder()
        .field(field)
        .code(code)
        .message(message)
        .invalidValue(invalidValue)
        .build();
  }

  /**
   * Crée une erreur complète avec détails.
   *
   * @param field Le champ en erreur
   * @param code Le code d'erreur
   * @param message Le message d'erreur
   * @param detail Les détails supplémentaires
   * @param invalidValue La valeur rejetée
   * @return L'erreur créée
   */
  public static ValidationError of(
      String field, String code, String message, String detail, Object invalidValue) {
    return ValidationError.builder()
        .field(field)
        .code(code)
        .message(message)
        .detail(detail)
        .invalidValue(invalidValue)
        .build();
  }

  /**
   * Formate l'erreur pour affichage.
   *
   * @return Le message formaté
   */
  public String toFormattedString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[").append(code).append("] ");
    sb.append(field).append(": ").append(message);
    if (detail != null) {
      sb.append(" (").append(detail).append(")");
    }
    if (invalidValue != null) {
      sb.append(" - Valeur: '").append(invalidValue).append("'");
    }
    return sb.toString();
  }
}
