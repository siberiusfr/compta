package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes fonctionnels des partenaires selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération PartnerDetails/@functionCode
 */
@Getter
@RequiredArgsConstructor
public enum PartnerFunctionCode {
  /** Débiteur */
  DEBTOR("I-61", "Débiteur"),

  /** Fournisseur (vendeur) - OBLIGATOIRE */
  SUPPLIER("I-62", "Fournisseur"),

  /** Créditeur */
  CREDITOR("I-63", "Créditeur"),

  /** Client (acheteur) - OBLIGATOIRE */
  CUSTOMER("I-64", "Client"),

  /** Destinataire final */
  FINAL_RECIPIENT("I-65", "Destinataire final"),

  /** Payeur */
  PAYER("I-66", "Payeur"),

  /** Agent */
  AGENT("I-67", "Agent"),

  /** Autre partenaire */
  OTHER("I-68", "Autre partenaire");

  private final String code;
  private final String description;

  /**
   * Trouve le code par sa valeur.
   *
   * @param code Le code (ex: "I-62")
   * @return Le code correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static PartnerFunctionCode fromCode(String code) {
    for (PartnerFunctionCode fc : values()) {
      if (fc.code.equals(code)) {
        return fc;
      }
    }
    throw new IllegalArgumentException("Code partenaire inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (PartnerFunctionCode fc : values()) {
      if (fc.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
