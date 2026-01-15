package tn.cyberious.compta.einvoicing.elfatoora.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;

/**
 * Validateur pour les identifiants fiscaux tunisiens.
 *
 * <p>Implémente les règles de validation définies dans les assertions XSD 1.1 du schéma TEIF.
 *
 * <p>Règles validées:
 *
 * <ul>
 *   <li>I-01 (Matricule fiscal): 7 chiffres + lettre + position + catégorie + établissement
 *   <li>I-02 (CIN): 8 chiffres exactement
 *   <li>I-03 (Passeport/Carte séjour): 9 chiffres exactement
 *   <li>I-04 (Autre): Pas de contrainte spécifique
 * </ul>
 */
@Component
public class TaxIdentifierValidator {

  /**
   * Pattern pour Matricule Fiscal Tunisien (I-01).
   *
   * <p>Format: NNNNNNNLPCE99
   *
   * <ul>
   *   <li>N: 7 chiffres (0-9)
   *   <li>L: 1 lettre majuscule (A-Z sauf I et O)
   *   <li>P: Position fiscale (A, B, D, N, P)
   *   <li>C: Catégorie (C, M, N, P, E)
   *   <li>E: 3 chiffres établissement (000-999)
   * </ul>
   */
  private static final Pattern MATRICULE_FISCAL_PATTERN =
      Pattern.compile("^[0-9]{7}[ABCDEFGHJKLMNPQRSTVWXYZ][ABDNP][CMNPE][0-9]{3}$");

  /** Pattern pour CIN (I-02): 8 chiffres */
  private static final Pattern CIN_PATTERN = Pattern.compile("^[0-9]{8}$");

  /** Pattern pour Carte de Séjour/Passeport (I-03): 9 chiffres */
  private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[0-9]{9}$");

  /** Lettres interdites dans le matricule fiscal (I et O) */
  private static final String FORBIDDEN_LETTERS = "IO";

  /** Lettres valides pour la position fiscale (8ème caractère) */
  private static final String VALID_POSITION_LETTERS = "ABDNP";

  /** Lettres valides pour la catégorie (9ème caractère) */
  private static final String VALID_CATEGORY_LETTERS = "CMNPE";

  /**
   * Valide un identifiant fiscal selon son type.
   *
   * @param identifier L'identifiant à valider
   * @param type Le type d'identifiant
   * @return true si valide
   */
  public boolean isValid(String identifier, IdentifierType type) {
    if (identifier == null || identifier.isBlank()) {
      return false;
    }

    return switch (type) {
      case I_01 -> isValidTunisianTaxId(identifier);
      case I_02 -> isValidCIN(identifier);
      case I_03 -> isValidPassport(identifier);
      case I_04 -> isValidOther(identifier);
    };
  }

  /**
   * Valide un Matricule Fiscal Tunisien (type I-01).
   *
   * @param taxId Le matricule fiscal
   * @return true si valide
   */
  public boolean isValidTunisianTaxId(String taxId) {
    if (taxId == null || taxId.length() != 13) {
      return false;
    }
    return MATRICULE_FISCAL_PATTERN.matcher(taxId).matches();
  }

  /**
   * Valide une Carte d'Identité Nationale (type I-02).
   *
   * @param cin Le numéro CIN
   * @return true si valide
   */
  public boolean isValidCIN(String cin) {
    if (cin == null || cin.length() != 8) {
      return false;
    }
    return CIN_PATTERN.matcher(cin).matches();
  }

  /**
   * Valide une Carte de Séjour ou Passeport (type I-03).
   *
   * @param passport Le numéro
   * @return true si valide
   */
  public boolean isValidPassport(String passport) {
    if (passport == null || passport.length() != 9) {
      return false;
    }
    return PASSPORT_PATTERN.matcher(passport).matches();
  }

  /**
   * Valide un identifiant de type autre (I-04).
   *
   * @param identifier L'identifiant
   * @return true si non vide et longueur <= 35
   */
  public boolean isValidOther(String identifier) {
    return identifier != null && !identifier.isBlank() && identifier.length() <= 35;
  }

  /**
   * Obtient les erreurs de validation détaillées pour un identifiant.
   *
   * @param identifier L'identifiant à valider
   * @param type Le type d'identifiant
   * @param fieldPath Le chemin du champ (ex: "supplier.taxIdentifier")
   * @return Liste des erreurs (vide si valide)
   */
  public List<ValidationError> getValidationErrors(
      String identifier, IdentifierType type, String fieldPath) {
    List<ValidationError> errors = new ArrayList<>();

    if (identifier == null || identifier.isBlank()) {
      errors.add(
          ValidationError.of(
              fieldPath, "ELF_EMPTY_TAX_ID", "L'identifiant fiscal est obligatoire"));
      return errors;
    }

    switch (type) {
      case I_01 -> errors.addAll(validateTunisianTaxId(identifier, fieldPath));
      case I_02 -> errors.addAll(validateCIN(identifier, fieldPath));
      case I_03 -> errors.addAll(validatePassport(identifier, fieldPath));
      case I_04 -> errors.addAll(validateOther(identifier, fieldPath));
    }

    return errors;
  }

  private List<ValidationError> validateTunisianTaxId(String taxId, String fieldPath) {
    List<ValidationError> errors = new ArrayList<>();

    // Vérifier la longueur
    if (taxId.length() != 13) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_TAX_ID_LENGTH")
              .message("Le matricule fiscal doit contenir exactement 13 caractères")
              .detail("Longueur actuelle: " + taxId.length())
              .invalidValue(taxId)
              .build());
      return errors;
    }

    // Vérifier les 7 premiers caractères (chiffres)
    String digits = taxId.substring(0, 7);
    if (!digits.matches("\\d{7}")) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_TAX_ID_DIGITS")
              .message("Les 7 premiers caractères doivent être des chiffres")
              .detail("Valeur actuelle: " + digits)
              .invalidValue(taxId)
              .build());
    }

    // Vérifier la 8ème lettre (pas I ou O)
    char letter = taxId.charAt(7);
    if (!Character.isLetter(letter) || FORBIDDEN_LETTERS.indexOf(letter) >= 0) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_TAX_ID_LETTER")
              .message("Le 8ème caractère doit être une lettre majuscule (sauf I et" + " O)")
              .detail("Caractère actuel: " + letter)
              .invalidValue(taxId)
              .build());
    }

    // Vérifier la position fiscale (9ème caractère)
    char position = taxId.charAt(8);
    if (VALID_POSITION_LETTERS.indexOf(position) < 0) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_TAX_ID_POSITION")
              .message("Le 9ème caractère (position fiscale) doit être A, B, D, N ou P")
              .detail("Caractère actuel: " + position)
              .invalidValue(taxId)
              .build());
    }

    // Vérifier la catégorie (10ème caractère)
    char category = taxId.charAt(9);
    if (VALID_CATEGORY_LETTERS.indexOf(category) < 0) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_TAX_ID_CATEGORY")
              .message("Le 10ème caractère (catégorie) doit être C, M, N, P ou E")
              .detail("Caractère actuel: " + category)
              .invalidValue(taxId)
              .build());
    }

    // Vérifier les 3 derniers caractères (chiffres établissement)
    String establishment = taxId.substring(10);
    if (!establishment.matches("\\d{3}")) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_TAX_ID_ESTABLISHMENT")
              .message("Les 3 derniers caractères doivent être des chiffres (000-999)")
              .detail("Valeur actuelle: " + establishment)
              .invalidValue(taxId)
              .build());
    }

    return errors;
  }

  private List<ValidationError> validateCIN(String cin, String fieldPath) {
    List<ValidationError> errors = new ArrayList<>();

    if (cin.length() != 8) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_CIN_LENGTH")
              .message("Le numéro CIN doit contenir exactement 8 chiffres")
              .detail("Longueur actuelle: " + cin.length())
              .invalidValue(cin)
              .build());
      return errors;
    }

    if (!cin.matches("\\d{8}")) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_CIN_FORMAT")
              .message("Le numéro CIN ne doit contenir que des chiffres")
              .invalidValue(cin)
              .build());
    }

    return errors;
  }

  private List<ValidationError> validatePassport(String passport, String fieldPath) {
    List<ValidationError> errors = new ArrayList<>();

    if (passport.length() != 9) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_PASSPORT_LENGTH")
              .message(
                  "Le numéro de carte de séjour/passeport doit contenir exactement" + " 9 chiffres")
              .detail("Longueur actuelle: " + passport.length())
              .invalidValue(passport)
              .build());
      return errors;
    }

    if (!passport.matches("\\d{9}")) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_PASSPORT_FORMAT")
              .message(
                  "Le numéro de carte de séjour/passeport ne doit contenir que" + " des chiffres")
              .invalidValue(passport)
              .build());
    }

    return errors;
  }

  private List<ValidationError> validateOther(String identifier, String fieldPath) {
    List<ValidationError> errors = new ArrayList<>();

    if (identifier.length() > 35) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_ID_TOO_LONG")
              .message("L'identifiant ne peut pas dépasser 35 caractères")
              .detail("Longueur actuelle: " + identifier.length())
              .invalidValue(identifier)
              .build());
    }

    return errors;
  }

  /**
   * Détermine le type d'identifiant en fonction de sa valeur.
   *
   * @param identifier L'identifiant
   * @return Le type probable (ou I_04 si non déterminable)
   */
  public IdentifierType detectType(String identifier) {
    if (identifier == null || identifier.isBlank()) {
      return IdentifierType.I_04;
    }

    // Matricule fiscal (13 caractères avec pattern spécifique)
    if (identifier.length() == 13 && MATRICULE_FISCAL_PATTERN.matcher(identifier).matches()) {
      return IdentifierType.I_01;
    }

    // CIN (8 chiffres)
    if (identifier.length() == 8 && identifier.matches("\\d{8}")) {
      return IdentifierType.I_02;
    }

    // Carte de séjour/Passeport (9 chiffres)
    if (identifier.length() == 9 && identifier.matches("\\d{9}")) {
      return IdentifierType.I_03;
    }

    return IdentifierType.I_04;
  }
}
