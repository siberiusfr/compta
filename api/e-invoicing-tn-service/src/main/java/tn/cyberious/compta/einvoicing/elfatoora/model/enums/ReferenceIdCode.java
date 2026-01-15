package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes d'identifiant de référence selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération Reference/@refID
 */
@Getter
@RequiredArgsConstructor
public enum ReferenceIdCode {
  /** Numéro de bon de commande */
  PURCHASE_ORDER("I-81", "Bon de commande"),

  /** Numéro de contrat */
  CONTRACT("I-82", "Contrat"),

  /** Numéro de bon de livraison */
  DELIVERY_NOTE("I-83", "Bon de livraison"),

  /** Référence client */
  CUSTOMER_REFERENCE("I-84", "Référence client"),

  /** Référence fournisseur */
  SUPPLIER_REFERENCE("I-85", "Référence fournisseur"),

  /** Numéro de projet */
  PROJECT("I-86", "Projet"),

  /** Numéro de facture d'origine (pour avoirs) */
  ORIGINAL_INVOICE("I-87", "Facture d'origine"),

  /** Numéro de lot */
  LOT_NUMBER("I-88", "Numéro de lot"),

  /** Numéro de série */
  SERIAL_NUMBER("I-89", "Numéro de série"),

  /** Autre référence */
  OTHER("I-80", "Autre référence"),

  /** Numéro TVA fournisseur */
  SUPPLIER_VAT_NUMBER("I-811", "TVA fournisseur"),

  /** Numéro TVA client */
  CUSTOMER_VAT_NUMBER("I-812", "TVA client"),

  /** Code SIRET */
  SIRET("I-813", "SIRET"),

  /** Code NAF/APE */
  NAF("I-814", "NAF/APE"),

  /** Numéro d'agrément */
  APPROVAL_NUMBER("I-815", "Numéro d'agrément"),

  /** Identifiant unique */
  UNIQUE_ID("I-816", "Identifiant unique"),

  /** Référence TTN */
  TTN_REFERENCE("I-817", "Référence TTN");

  private final String code;
  private final String description;

  /**
   * Trouve le code par sa valeur.
   *
   * @param code Le code (ex: "I-81")
   * @return Le code correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static ReferenceIdCode fromCode(String code) {
    for (ReferenceIdCode rc : values()) {
      if (rc.code.equals(code)) {
        return rc;
      }
    }
    throw new IllegalArgumentException("Code référence inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (ReferenceIdCode rc : values()) {
      if (rc.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
