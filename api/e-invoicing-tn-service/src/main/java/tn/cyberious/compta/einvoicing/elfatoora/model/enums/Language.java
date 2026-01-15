package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes de langue selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération LangEnumType
 */
@Getter
@RequiredArgsConstructor
public enum Language {
  /** Français */
  FRENCH("fr", "Français"),

  /** Anglais */
  ENGLISH("en", "English"),

  /** Arabe */
  ARABIC("ar", "العربية"),

  /** Originale (langue du document) */
  ORIGINAL("or", "Originale");

  private final String code;
  private final String description;

  /**
   * Trouve la langue par son code.
   *
   * @param code Le code (ex: "fr")
   * @return La langue correspondante
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static Language fromCode(String code) {
    for (Language lang : values()) {
      if (lang.code.equals(code)) {
        return lang;
      }
    }
    throw new IllegalArgumentException("Code langue inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (Language lang : values()) {
      if (lang.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
