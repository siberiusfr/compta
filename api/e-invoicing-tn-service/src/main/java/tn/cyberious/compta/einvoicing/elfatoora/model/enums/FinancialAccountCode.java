package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes de compte financier selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération Fii/@functionCode
 */
@Getter
@RequiredArgsConstructor
public enum FinancialAccountCode {
  /** Compte bancaire payeur */
  PAYER_BANK_ACCOUNT("I-141", "Compte bancaire payeur"),

  /** Compte bancaire bénéficiaire */
  PAYEE_BANK_ACCOUNT("I-142", "Compte bancaire bénéficiaire"),

  /** Compte postal (CCP) */
  POSTAL_ACCOUNT("I-143", "Compte postal CCP");

  private final String code;
  private final String description;

  /**
   * Trouve le code par sa valeur.
   *
   * @param code Le code (ex: "I-141")
   * @return Le code correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static FinancialAccountCode fromCode(String code) {
    for (FinancialAccountCode fc : values()) {
      if (fc.code.equals(code)) {
        return fc;
      }
    }
    throw new IllegalArgumentException("Code compte financier inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (FinancialAccountCode fc : values()) {
      if (fc.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
