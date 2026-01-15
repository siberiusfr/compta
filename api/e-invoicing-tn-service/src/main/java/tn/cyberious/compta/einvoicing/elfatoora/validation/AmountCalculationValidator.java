package tn.cyberious.compta.einvoicing.elfatoora.validation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.DocumentTypeCode;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.TaxTypeCode;

/**
 * Validateur pour les calculs de montants El Fatoora.
 *
 * <p>Règles validées:
 *
 * <ul>
 *   <li>Précision: 3 décimales (millimes)
 *   <li>Arrondi: HALF_UP
 *   <li>Calculs de ligne: montantHT = quantité × prixUnitaire
 *   <li>Calculs de taxe: montantTaxe = montantHT × taux/100
 *   <li>Totaux cohérents avec somme des lignes
 *   <li>Taux TVA valides (0%, 7%, 13%, 19%)
 * </ul>
 */
@Component
public class AmountCalculationValidator {

  /** Précision des montants (3 décimales = millimes) */
  public static final int AMOUNT_SCALE = 3;

  /** Mode d'arrondi */
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  /** Tolérance d'arrondi (1 millime) */
  public static final BigDecimal ROUNDING_TOLERANCE = new BigDecimal("0.001");

  /** Montant zéro avec précision */
  public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(AMOUNT_SCALE, ROUNDING_MODE);

  /**
   * Valide les calculs d'une ligne de facture.
   *
   * @param line La ligne à valider
   * @param lineNumber Numéro de ligne (pour les messages d'erreur)
   * @return Résultat de validation
   */
  public ValidationResult validateLineCalculations(InvoiceLineDTO line, int lineNumber) {
    ValidationResult result = ValidationResult.valid();
    String fieldPrefix = "lines[" + lineNumber + "]";

    // Valeurs obligatoires
    if (line.getQuantity() == null) {
      result.addError(
          ValidationError.of(
              fieldPrefix + ".quantity", "ELF_MISSING_QUANTITY", "La quantité est obligatoire"));
    }

    if (line.getUnitPrice() == null) {
      result.addError(
          ValidationError.of(
              fieldPrefix + ".unitPrice",
              "ELF_MISSING_UNIT_PRICE",
              "Le prix unitaire est obligatoire"));
    }

    if (line.getTaxRate() == null) {
      result.addError(
          ValidationError.of(
              fieldPrefix + ".taxRate", "ELF_MISSING_TAX_RATE", "Le taux de TVA est obligatoire"));
    }

    // Si données manquantes, arrêter la validation
    if (result.hasErrors()) {
      return result;
    }

    // Valider le taux de TVA
    if (!TaxTypeCode.isValidVatRate(line.getTaxRate())) {
      result.addError(
          ValidationError.builder()
              .field(fieldPrefix + ".taxRate")
              .code("ELF_INVALID_TAX_RATE")
              .message("Taux de TVA invalide (autorisés: 0%, 7%, 13%, 19%)")
              .invalidValue(line.getTaxRate())
              .build());
    }

    // Calculer le montant HT attendu
    BigDecimal expectedExclTax =
        line.getQuantity().multiply(line.getUnitPrice()).setScale(AMOUNT_SCALE, ROUNDING_MODE);

    // Vérifier le montant HT
    if (line.getLineAmountExclTax() != null) {
      if (!isWithinTolerance(line.getLineAmountExclTax(), expectedExclTax)) {
        result.addError(
            ValidationError.builder()
                .field(fieldPrefix + ".lineAmountExclTax")
                .code("ELF_INCORRECT_LINE_AMOUNT")
                .message("Montant HT ligne incorrect")
                .detail("Attendu: " + expectedExclTax + ", Fourni: " + line.getLineAmountExclTax())
                .invalidValue(line.getLineAmountExclTax())
                .build());
      }
    }

    // Calculer le montant de taxe attendu
    BigDecimal expectedTaxAmount =
        expectedExclTax
            .multiply(line.getTaxRate())
            .divide(new BigDecimal("100"), AMOUNT_SCALE, ROUNDING_MODE);

    // Vérifier le montant de taxe
    if (line.getTaxAmount() != null) {
      if (!isWithinTolerance(line.getTaxAmount(), expectedTaxAmount)) {
        result.addError(
            ValidationError.builder()
                .field(fieldPrefix + ".taxAmount")
                .code("ELF_INCORRECT_TAX_AMOUNT")
                .message("Montant taxe ligne incorrect")
                .detail("Attendu: " + expectedTaxAmount + ", Fourni: " + line.getTaxAmount())
                .invalidValue(line.getTaxAmount())
                .build());
      }
    }

    // Calculer le montant TTC attendu
    BigDecimal expectedInclTax = expectedExclTax.add(expectedTaxAmount);

    // Vérifier le montant TTC
    if (line.getLineAmountInclTax() != null) {
      if (!isWithinTolerance(line.getLineAmountInclTax(), expectedInclTax)) {
        result.addError(
            ValidationError.builder()
                .field(fieldPrefix + ".lineAmountInclTax")
                .code("ELF_INCORRECT_LINE_TOTAL")
                .message("Montant TTC ligne incorrect")
                .detail("Attendu: " + expectedInclTax + ", Fourni: " + line.getLineAmountInclTax())
                .invalidValue(line.getLineAmountInclTax())
                .build());
      }
    }

    // Valider les montants positifs (sauf avoirs)
    if (line.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
      result.addWarning(
          ValidationWarning.of(
              fieldPrefix + ".quantity",
              "ELF_WARN_NEGATIVE_QUANTITY",
              "Quantité négative (normal pour un avoir)"));
    }

    if (line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
      result.addWarning(
          ValidationWarning.of(
              fieldPrefix + ".unitPrice", "ELF_WARN_NEGATIVE_PRICE", "Prix unitaire négatif"));
    }

    return result;
  }

  /**
   * Valide les totaux de la facture.
   *
   * @param invoice La facture à valider
   * @return Résultat de validation
   */
  public ValidationResult validateTotals(ElFatooraInvoiceDTO invoice) {
    ValidationResult result = ValidationResult.valid();

    if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
      result.addError(
          ValidationError.of(
              "lines", "ELF_NO_LINES", "La facture doit contenir au moins une ligne"));
      return result;
    }

    // Calculer les totaux attendus
    BigDecimal expectedTotalExclTax = ZERO;
    BigDecimal expectedTotalTax = ZERO;

    for (InvoiceLineDTO line : invoice.getLines()) {
      BigDecimal lineExclTax =
          line.getLineAmountExclTax() != null
              ? line.getLineAmountExclTax()
              : calculateLineAmountExclTax(line);
      BigDecimal lineTax =
          line.getTaxAmount() != null ? line.getTaxAmount() : calculateLineTaxAmount(line);

      expectedTotalExclTax = expectedTotalExclTax.add(lineExclTax);
      expectedTotalTax = expectedTotalTax.add(lineTax);
    }

    expectedTotalExclTax = expectedTotalExclTax.setScale(AMOUNT_SCALE, ROUNDING_MODE);
    expectedTotalTax = expectedTotalTax.setScale(AMOUNT_SCALE, ROUNDING_MODE);
    BigDecimal expectedTotalInclTax = expectedTotalExclTax.add(expectedTotalTax);

    // Vérifier le total HT
    if (invoice.getTotalExcludingTax() != null) {
      if (!isWithinTolerance(invoice.getTotalExcludingTax(), expectedTotalExclTax)) {
        result.addError(
            ValidationError.builder()
                .field("totalExcludingTax")
                .code("ELF_INCORRECT_TOTAL_EXCL")
                .message("Total HT incorrect")
                .detail(
                    "Attendu: "
                        + expectedTotalExclTax
                        + ", Fourni: "
                        + invoice.getTotalExcludingTax())
                .invalidValue(invoice.getTotalExcludingTax())
                .build());
      }
    }

    // Vérifier le total TVA
    if (invoice.getTotalTax() != null) {
      if (!isWithinTolerance(invoice.getTotalTax(), expectedTotalTax)) {
        result.addError(
            ValidationError.builder()
                .field("totalTax")
                .code("ELF_INCORRECT_TOTAL_TAX")
                .message("Total TVA incorrect")
                .detail("Attendu: " + expectedTotalTax + ", Fourni: " + invoice.getTotalTax())
                .invalidValue(invoice.getTotalTax())
                .build());
      }
    }

    // Vérifier le total TTC
    if (invoice.getTotalIncludingTax() != null) {
      if (!isWithinTolerance(invoice.getTotalIncludingTax(), expectedTotalInclTax)) {
        result.addError(
            ValidationError.builder()
                .field("totalIncludingTax")
                .code("ELF_INCORRECT_TOTAL_INCL")
                .message("Total TTC incorrect")
                .detail(
                    "Attendu: "
                        + expectedTotalInclTax
                        + ", Fourni: "
                        + invoice.getTotalIncludingTax())
                .invalidValue(invoice.getTotalIncludingTax())
                .build());
      }
    }

    // Vérifier cohérence : Total TTC = Total HT + Total TVA
    if (invoice.getTotalExcludingTax() != null
        && invoice.getTotalTax() != null
        && invoice.getTotalIncludingTax() != null) {
      BigDecimal calculatedInclTax = invoice.getTotalExcludingTax().add(invoice.getTotalTax());
      if (!isWithinTolerance(invoice.getTotalIncludingTax(), calculatedInclTax)) {
        result.addError(
            ValidationError.builder()
                .field("totalIncludingTax")
                .code("ELF_INCONSISTENT_TOTALS")
                .message("Incohérence: Total TTC ≠ Total HT + Total TVA")
                .detail(
                    "HT ("
                        + invoice.getTotalExcludingTax()
                        + ") + TVA ("
                        + invoice.getTotalTax()
                        + ") = "
                        + calculatedInclTax
                        + ", mais TTC = "
                        + invoice.getTotalIncludingTax())
                .build());
      }
    }

    // Vérifier montants positifs (sauf pour avoirs)
    boolean isCreditNote =
        invoice.getDocumentType() != null
            && DocumentTypeCode.CREDIT_NOTE.getCode().equals(invoice.getDocumentType());

    if (!isCreditNote && invoice.getTotalIncludingTax() != null) {
      if (invoice.getTotalIncludingTax().compareTo(BigDecimal.ZERO) < 0) {
        result.addError(
            ValidationError.of(
                "totalIncludingTax",
                "ELF_NEGATIVE_TOTAL",
                "Le total TTC ne peut pas être négatif pour une facture"));
      }
    }

    return result;
  }

  /**
   * Valide la ventilation par taux de TVA.
   *
   * @param invoice La facture à valider
   * @return Résultat de validation
   */
  public ValidationResult validateTaxBreakdown(ElFatooraInvoiceDTO invoice) {
    ValidationResult result = ValidationResult.valid();

    if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
      return result;
    }

    // Grouper les lignes par taux de TVA
    Map<BigDecimal, List<InvoiceLineDTO>> linesByTaxRate =
        invoice.getLines().stream()
            .filter(line -> line.getTaxRate() != null)
            .collect(Collectors.groupingBy(InvoiceLineDTO::getTaxRate));

    // Calculer les totaux par taux
    for (Map.Entry<BigDecimal, List<InvoiceLineDTO>> entry : linesByTaxRate.entrySet()) {
      BigDecimal taxRate = entry.getKey();
      List<InvoiceLineDTO> lines = entry.getValue();

      // Vérifier que le taux est valide
      if (!TaxTypeCode.isValidVatRate(taxRate)) {
        result.addError(
            ValidationError.builder()
                .field("taxBreakdown")
                .code("ELF_INVALID_TAX_RATE_IN_BREAKDOWN")
                .message("Taux de TVA invalide dans la ventilation")
                .detail("Taux non autorisé: " + taxRate + "%")
                .invalidValue(taxRate)
                .build());
      }

      // Calculer la base imposable pour ce taux
      BigDecimal taxableBase =
          lines.stream()
              .map(
                  line ->
                      line.getLineAmountExclTax() != null
                          ? line.getLineAmountExclTax()
                          : calculateLineAmountExclTax(line))
              .reduce(ZERO, BigDecimal::add)
              .setScale(AMOUNT_SCALE, ROUNDING_MODE);

      // Calculer la TVA pour ce taux
      BigDecimal expectedTaxForRate =
          taxableBase.multiply(taxRate).divide(new BigDecimal("100"), AMOUNT_SCALE, ROUNDING_MODE);

      // Calculer la TVA réelle des lignes
      BigDecimal actualTaxForRate =
          lines.stream()
              .map(
                  line ->
                      line.getTaxAmount() != null
                          ? line.getTaxAmount()
                          : calculateLineTaxAmount(line))
              .reduce(ZERO, BigDecimal::add)
              .setScale(AMOUNT_SCALE, ROUNDING_MODE);

      // Vérifier la cohérence
      if (!isWithinTolerance(actualTaxForRate, expectedTaxForRate)) {
        result.addWarning(
            ValidationWarning.of(
                "taxBreakdown[" + taxRate + "]",
                "ELF_WARN_TAX_BREAKDOWN_MISMATCH",
                "Écart dans la ventilation TVA à "
                    + taxRate
                    + "%: attendu "
                    + expectedTaxForRate
                    + ", calculé "
                    + actualTaxForRate));
      }
    }

    return result;
  }

  /**
   * Calcule le montant HT d'une ligne.
   *
   * @param line La ligne
   * @return Le montant HT calculé
   */
  public BigDecimal calculateLineAmountExclTax(InvoiceLineDTO line) {
    if (line.getQuantity() == null || line.getUnitPrice() == null) {
      return ZERO;
    }
    return line.getQuantity().multiply(line.getUnitPrice()).setScale(AMOUNT_SCALE, ROUNDING_MODE);
  }

  /**
   * Calcule le montant de taxe d'une ligne.
   *
   * @param line La ligne
   * @return Le montant de taxe calculé
   */
  public BigDecimal calculateLineTaxAmount(InvoiceLineDTO line) {
    if (line.getTaxRate() == null) {
      return ZERO;
    }
    BigDecimal amountExclTax =
        line.getLineAmountExclTax() != null
            ? line.getLineAmountExclTax()
            : calculateLineAmountExclTax(line);
    return amountExclTax
        .multiply(line.getTaxRate())
        .divide(new BigDecimal("100"), AMOUNT_SCALE, ROUNDING_MODE);
  }

  /**
   * Vérifie si deux montants sont égaux à la tolérance près.
   *
   * @param actual Valeur réelle
   * @param expected Valeur attendue
   * @return true si égaux à ROUNDING_TOLERANCE près
   */
  private boolean isWithinTolerance(BigDecimal actual, BigDecimal expected) {
    if (actual == null || expected == null) {
      return actual == expected;
    }
    return actual.subtract(expected).abs().compareTo(ROUNDING_TOLERANCE) <= 0;
  }
}
