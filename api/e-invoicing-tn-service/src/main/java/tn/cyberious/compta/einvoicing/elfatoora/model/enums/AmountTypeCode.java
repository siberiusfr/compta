package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes de type de montant selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération Moa/@amountTypeCode
 */
@Getter
@RequiredArgsConstructor
public enum AmountTypeCode {
  /** Montant ligne HT */
  LINE_AMOUNT_EXCL_TAX("I-171", "Montant ligne HT", true),

  /** Montant ligne TTC */
  LINE_AMOUNT_INCL_TAX("I-172", "Montant ligne TTC", true),

  /** Prix unitaire */
  UNIT_PRICE("I-173", "Prix unitaire", true),

  /** Montant taxe ligne */
  LINE_TAX_AMOUNT("I-174", "Montant taxe ligne", true),

  /** Montant remise ligne */
  LINE_DISCOUNT("I-175", "Montant remise ligne", true),

  /** Montant majoration ligne */
  LINE_CHARGE("I-176", "Montant majoration ligne", true),

  /** Total HT facture */
  TOTAL_EXCL_TAX("I-177", "Total HT", false),

  /** Total TTC facture */
  TOTAL_INCL_TAX("I-178", "Total TTC", false),

  /** Total TVA */
  TOTAL_VAT("I-179", "Total TVA", false),

  /** Total autres taxes */
  TOTAL_OTHER_TAXES("I-180", "Total autres taxes", false),

  /** Base imposable TVA */
  VAT_BASE_AMOUNT("I-181", "Base imposable TVA", false),

  /** Montant TVA par taux */
  VAT_AMOUNT("I-182", "Montant TVA", false),

  /** Montant à payer */
  AMOUNT_DUE("I-183", "Montant à payer", false),

  /** Acompte déjà versé */
  PREPAID_AMOUNT("I-184", "Acompte versé", false),

  /** Montant remise globale */
  GLOBAL_DISCOUNT("I-185", "Remise globale", false),

  /** Montant majoration globale */
  GLOBAL_CHARGE("I-186", "Majoration globale", false),

  /** Arrondi */
  ROUNDING("I-187", "Arrondi", false),

  /** Autre montant */
  OTHER_AMOUNT("I-188", "Autre montant", false);

  private final String code;
  private final String description;

  /** Indique si c'est un montant au niveau ligne */
  private final boolean lineLevel;

  /**
   * Trouve le code par sa valeur.
   *
   * @param code Le code (ex: "I-177")
   * @return Le code correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static AmountTypeCode fromCode(String code) {
    for (AmountTypeCode ac : values()) {
      if (ac.code.equals(code)) {
        return ac;
      }
    }
    throw new IllegalArgumentException("Code montant inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (AmountTypeCode ac : values()) {
      if (ac.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
