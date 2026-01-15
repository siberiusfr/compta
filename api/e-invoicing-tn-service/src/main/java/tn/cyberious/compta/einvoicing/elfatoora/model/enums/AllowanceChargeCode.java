package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes de remise/majoration selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération Alc/@allowanceCode
 */
@Getter
@RequiredArgsConstructor
public enum AllowanceChargeCode {
  /** Remise commerciale */
  COMMERCIAL_DISCOUNT("I-151", "Remise commerciale", true),

  /** Remise quantité */
  QUANTITY_DISCOUNT("I-152", "Remise quantité", true),

  /** Frais de transport */
  TRANSPORT_CHARGE("I-153", "Frais de transport", false),

  /** Frais d'emballage */
  PACKAGING_CHARGE("I-154", "Frais d'emballage", false),

  /** Autres frais/remises */
  OTHER("I-155", "Autres frais/remises", false);

  private final String code;
  private final String description;

  /** true = remise (allowance), false = majoration (charge) */
  private final boolean allowance;

  /**
   * Trouve le code par sa valeur.
   *
   * @param code Le code (ex: "I-151")
   * @return Le code correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static AllowanceChargeCode fromCode(String code) {
    for (AllowanceChargeCode ac : values()) {
      if (ac.code.equals(code)) {
        return ac;
      }
    }
    throw new IllegalArgumentException("Code remise/majoration inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (AllowanceChargeCode ac : values()) {
      if (ac.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
