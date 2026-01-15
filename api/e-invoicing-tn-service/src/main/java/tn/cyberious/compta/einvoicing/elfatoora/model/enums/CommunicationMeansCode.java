package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes de moyens de communication selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération ComMeansType
 */
@Getter
@RequiredArgsConstructor
public enum CommunicationMeansCode {
  /** Téléphone */
  PHONE("I-101", "Téléphone"),

  /** Fax */
  FAX("I-102", "Fax"),

  /** Email */
  EMAIL("I-103", "Email"),

  /** Site web */
  WEBSITE("I-104", "Site web");

  private final String code;
  private final String description;

  /**
   * Trouve le code par sa valeur.
   *
   * @param code Le code (ex: "I-101")
   * @return Le code correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static CommunicationMeansCode fromCode(String code) {
    for (CommunicationMeansCode cc : values()) {
      if (cc.code.equals(code)) {
        return cc;
      }
    }
    throw new IllegalArgumentException("Code communication inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (CommunicationMeansCode cc : values()) {
      if (cc.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
