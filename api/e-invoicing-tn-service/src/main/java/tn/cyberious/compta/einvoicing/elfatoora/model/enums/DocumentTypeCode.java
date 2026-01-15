package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Types de documents El Fatoora selon le XSD.
 *
 * <p>Correspond aux valeurs de l'énumération DocumentType/@code
 */
@Getter
@RequiredArgsConstructor
public enum DocumentTypeCode {
  /** Facture standard */
  INVOICE("I-11", "Facture", false),

  /** Avoir (note de crédit) */
  CREDIT_NOTE("I-12", "Avoir", true),

  /** Facture de débit */
  DEBIT_NOTE("I-13", "Facture de débit", false),

  /** Facture simplifiée (petits montants) */
  SIMPLIFIED_INVOICE("I-14", "Facture simplifiée", false),

  /** Facture d'auto-facturation */
  SELF_BILLING_INVOICE("I-15", "Facture d'auto-facturation", false),

  /** Facture rectificative */
  CORRECTIVE_INVOICE("I-16", "Facture rectificative", false);

  private final String code;
  private final String description;

  /** Indique si les montants peuvent être négatifs */
  private final boolean allowsNegativeAmounts;

  /**
   * Trouve le type par son code.
   *
   * @param code Le code (ex: "I-11")
   * @return Le type correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static DocumentTypeCode fromCode(String code) {
    for (DocumentTypeCode type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Code document inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (DocumentTypeCode type : values()) {
      if (type.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
