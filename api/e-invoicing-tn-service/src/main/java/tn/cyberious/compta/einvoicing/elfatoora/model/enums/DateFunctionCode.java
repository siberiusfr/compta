package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes fonctionnels des dates selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération DateText/@functionCode
 */
@Getter
@RequiredArgsConstructor
public enum DateFunctionCode {
  /** Date de la facture - OBLIGATOIRE */
  INVOICE_DATE("I-31", "Date de la facture", DateFormat.DDMMYY, true),

  /** Date d'échéance de paiement */
  DUE_DATE("I-32", "Date d'échéance", DateFormat.DDMMYY, false),

  /** Date de livraison */
  DELIVERY_DATE("I-33", "Date de livraison", DateFormat.DDMMYY, false),

  /** Date de commande */
  ORDER_DATE("I-34", "Date de commande", DateFormat.DDMMYY, false),

  /** Date de contrat */
  CONTRACT_DATE("I-35", "Date de contrat", DateFormat.DDMMYY, false),

  /** Période de service/facturation */
  SERVICE_PERIOD("I-36", "Période de service", DateFormat.PERIOD, false),

  /** Date de validation TTN */
  TTN_VALIDATION_DATE("I-37", "Date validation TTN", DateFormat.DDMMYYHHMM, false),

  /** Date personnalisée */
  CUSTOM_DATE("I-38", "Date personnalisée", DateFormat.DDMMYY, false);

  private final String code;
  private final String description;
  private final DateFormat expectedFormat;
  private final boolean required;

  /** Formats de date supportés par El Fatoora. */
  @Getter
  @RequiredArgsConstructor
  public enum DateFormat {
    /** Format ddMMyy (6 caractères) - Ex: 070624 */
    DDMMYY("ddMMyy", "^\\d{6}$", 6),

    /** Format ddMMyyHHmm (10 caractères) - Ex: 0706241230 */
    DDMMYYHHMM("ddMMyyHHmm", "^\\d{10}$", 10),

    /** Format période ddMMyy-ddMMyy (13 caractères) - Ex: 010624-300624 */
    PERIOD("ddMMyy-ddMMyy", "^\\d{6}-\\d{6}$", 13);

    private final String pattern;
    private final String regex;
    private final int expectedLength;

    /**
     * Vérifie si la valeur correspond au format.
     *
     * @param value La valeur à vérifier
     * @return true si le format est correct
     */
    public boolean matches(String value) {
      if (value == null) {
        return false;
      }
      return value.matches(regex);
    }
  }

  /**
   * Trouve le code par sa valeur.
   *
   * @param code Le code (ex: "I-31")
   * @return Le code correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static DateFunctionCode fromCode(String code) {
    for (DateFunctionCode fc : values()) {
      if (fc.code.equals(code)) {
        return fc;
      }
    }
    throw new IllegalArgumentException("Code date inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (DateFunctionCode fc : values()) {
      if (fc.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
