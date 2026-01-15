package tn.cyberious.compta.einvoicing.elfatoora.model.enums;

import java.math.BigDecimal;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Types de taxes tunisiennes selon le XSD El Fatoora.
 *
 * <p>Correspond aux valeurs de l'énumération TaxTypeName/@code
 */
@Getter
@RequiredArgsConstructor
public enum TaxTypeCode {
  /** TVA - Taxe sur la Valeur Ajoutée */
  TVA("I-161", "TVA", true),

  /** Droit de timbre */
  STAMP_DUTY("I-162", "Droit de timbre", false),

  /** TCL - Taxe sur les établissements à caractère industriel, commercial ou professionnel */
  TCL("I-163", "TCL", false),

  /** FODEC - Fonds de Développement de la Compétitivité */
  FODEC("I-164", "FODEC", false),

  /** Contribution au FOPROLOS */
  FOPROLOS("I-165", "FOPROLOS", false),

  /** Droit de consommation */
  CONSUMPTION_DUTY("I-166", "Droit de consommation", false),

  /** Retenue à la source */
  WITHHOLDING_TAX("I-167", "Retenue à la source", false),

  /** Taxe additionnelle */
  ADDITIONAL_TAX("I-168", "Taxe additionnelle", false),

  /** Autres taxes */
  OTHER_TAX("I-169", "Autres taxes", false),

  /** Exonéré de TVA */
  VAT_EXEMPT("I-160", "Exonéré de TVA", true),

  /** Suspension TVA */
  VAT_SUSPENSION("I-1601", "Suspension TVA", true),

  /** Régime forfaitaire */
  FLAT_RATE("I-1602", "Régime forfaitaire", true),

  /** Exportation (taux 0%) */
  EXPORT("I-1603", "Exportation", true);

  private final String code;
  private final String description;

  /** Indique si c'est un type de TVA */
  private final boolean vatRelated;

  /** Taux de TVA tunisiens autorisés */
  public static final BigDecimal[] VALID_VAT_RATES = {
    new BigDecimal("0.00"), new BigDecimal("7.00"), new BigDecimal("13.00"), new BigDecimal("19.00")
  };

  /**
   * Vérifie si un taux de TVA est valide en Tunisie.
   *
   * @param rate Le taux à vérifier
   * @return true si le taux est valide (0%, 7%, 13%, ou 19%)
   */
  public static boolean isValidVatRate(BigDecimal rate) {
    if (rate == null) {
      return false;
    }
    return Arrays.stream(VALID_VAT_RATES).anyMatch(r -> r.compareTo(rate) == 0);
  }

  /**
   * Trouve le type par son code.
   *
   * @param code Le code (ex: "I-161")
   * @return Le type correspondant
   * @throws IllegalArgumentException si le code est inconnu
   */
  public static TaxTypeCode fromCode(String code) {
    for (TaxTypeCode type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Code taxe inconnu: " + code);
  }

  /**
   * Vérifie si un code est valide.
   *
   * @param code Le code à vérifier
   * @return true si le code est valide
   */
  public static boolean isValidCode(String code) {
    for (TaxTypeCode type : values()) {
      if (type.code.equals(code)) {
        return true;
      }
    }
    return false;
  }
}
