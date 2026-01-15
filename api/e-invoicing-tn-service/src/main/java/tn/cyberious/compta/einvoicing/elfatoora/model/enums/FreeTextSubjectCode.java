package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes sujet de texte libre selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération FreeTextDetail/@subjectCode
 */
@Getter
@RequiredArgsConstructor
public enum FreeTextSubjectCode {
  /** Conditions générales de vente */
  GENERAL_CONDITIONS("I-41", "Conditions générales de vente"),

  /** Instructions de paiement */
  PAYMENT_INSTRUCTIONS("I-42", "Instructions de paiement"),

  /** Note interne */
  INTERNAL_NOTE("I-43", "Note interne"),

  /** Remarques sur la livraison */
  DELIVERY_REMARKS("I-44", "Remarques livraison"),

  /** Mention légale */
  LEGAL_MENTION("I-45", "Mention légale"),

  /** Motif d'exonération TVA */
  VAT_EXEMPTION_REASON("I-46", "Motif exonération TVA"),

  /** Description du service */
  SERVICE_DESCRIPTION("I-47", "Description service"),

  /** Autre commentaire */
  OTHER("I-48", "Autre commentaire");

  private final String code;
  private final String description;

  /**
   * Trouve le code par sa valeur.
   *
   * @param code Le code (ex: "I-41")
   * @return Le code correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static FreeTextSubjectCode fromCode(String code) {
    for (FreeTextSubjectCode fc : values()) {
      if (fc.code.equals(code)) {
        return fc;
      }
    }
    throw new IllegalArgumentException("Code texte libre inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (FreeTextSubjectCode fc : values()) {
      if (fc.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
