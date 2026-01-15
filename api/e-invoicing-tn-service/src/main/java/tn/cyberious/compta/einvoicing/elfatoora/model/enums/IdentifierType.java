package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Types d'identifiants fiscaux tunisiens selon le XSD El Fatoora.
 *
 * <p>Basé sur les assertions XSD 1.1 (xs:assert) du schéma TEIF v1.8.8
 */
@Getter
@RequiredArgsConstructor
public enum IdentifierType {
  /**
   * Matricule Fiscal Tunisien.
   *
   * <p>Format: NNNNNNNLPCE99 (13 caractères)
   *
   * <ul>
   *   <li>N: 7 chiffres (0-9)
   *   <li>L: 1 lettre majuscule (sauf I et O)
   *   <li>P: Position fiscale (A, B, D, N, P)
   *   <li>C: Catégorie (C, M, N, P, E)
   *   <li>E: 3 chiffres établissement (000-999)
   * </ul>
   *
   * <p>Exemple valide: 0736202XAM000
   */
  I_01(
      "I-01",
      "Matricule fiscal tunisien",
      "^[0-9]{7}[ABCDEFGHJKLMNPQRSTVWXYZ][ABDNP][CMNPE][0-9]{3}$",
      13,
      13),

  /**
   * Carte d'Identité Nationale (CIN).
   *
   * <p>Format: 8 chiffres exactement
   *
   * <p>Exemple valide: 12345678
   */
  I_02("I-02", "Carte d'Identité Nationale (CIN)", "^[0-9]{8}$", 8, 8),

  /**
   * Carte de Séjour / Passeport.
   *
   * <p>Format: 9 chiffres exactement
   *
   * <p>Exemple valide: 123456789
   */
  I_03("I-03", "Carte de Séjour / Passeport", "^[0-9]{9}$", 9, 9),

  /**
   * Autre type d'identifiant.
   *
   * <p>Pas de contrainte de format spécifique
   */
  I_04("I-04", "Autre identifiant", null, 1, 35);

  private final String code;
  private final String description;
  private final String regex;
  private final int minLength;
  private final int maxLength;

  /** Pattern compilé pour performance (lazy init) */
  private transient Pattern compiledPattern;

  /**
   * Vérifie si la valeur correspond au pattern de ce type.
   *
   * @param value La valeur à valider
   * @return true si la valeur est valide pour ce type
   */
  public boolean matches(String value) {
    if (value == null) {
      return false;
    }
    if (regex == null) {
      // I-04: juste vérifier la longueur
      return value.length() >= minLength && value.length() <= maxLength;
    }
    return getCompiledPattern().matcher(value).matches();
  }

  /**
   * Obtient le pattern compilé (lazy init thread-safe).
   *
   * @return Le pattern compilé
   */
  public Pattern getCompiledPattern() {
    if (compiledPattern == null && regex != null) {
      compiledPattern = Pattern.compile(regex);
    }
    return compiledPattern;
  }

  /**
   * Trouve le type par son code.
   *
   * @param code Le code (ex: "I-01")
   * @return Le type correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static IdentifierType fromCode(String code) {
    for (IdentifierType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Code identifiant inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (IdentifierType type : values()) {
      if (type.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
