package tn.cyberious.compta.einvoicing.elfatoora.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;

/**
 * Tests unitaires pour AmountCalculationValidator.
 *
 * <p>Vérifie les règles de calcul des montants El Fatoora.
 */
@DisplayName("AmountCalculationValidator")
class AmountCalculationValidatorTest {

  private AmountCalculationValidator validator;

  @BeforeEach
  void setUp() {
    validator = new AmountCalculationValidator();
  }

  @Nested
  @DisplayName("Calculs de ligne")
  class LineCalculationTests {

    @Test
    @DisplayName("Accepte une ligne avec calculs corrects")
    void shouldAcceptCorrectLineCalculations() {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("ITEM001")
              .quantity(new BigDecimal("2.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19.00"))
              .lineAmountExclTax(new BigDecimal("200.000"))
              .taxAmount(new BigDecimal("38.000"))
              .lineAmountInclTax(new BigDecimal("238.000"))
              .build();

      ValidationResult result = validator.validateLineCalculations(line, 0);

      assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Détecte un montant HT incorrect")
    void shouldDetectIncorrectExclTaxAmount() {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("ITEM001")
              .quantity(new BigDecimal("2.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19.00"))
              .lineAmountExclTax(new BigDecimal("199.000")) // Incorrect: devrait être 200
              .build();

      ValidationResult result = validator.validateLineCalculations(line, 0);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_INCORRECT_LINE_AMOUNT"));
    }

    @Test
    @DisplayName("Détecte un montant de taxe incorrect")
    void shouldDetectIncorrectTaxAmount() {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("ITEM001")
              .quantity(new BigDecimal("2.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19.00"))
              .lineAmountExclTax(new BigDecimal("200.000"))
              .taxAmount(new BigDecimal("40.000")) // Incorrect: devrait être 38
              .build();

      ValidationResult result = validator.validateLineCalculations(line, 0);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_INCORRECT_TAX_AMOUNT"));
    }

    @Test
    @DisplayName("Détecte un montant TTC incorrect")
    void shouldDetectIncorrectInclTaxAmount() {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("ITEM001")
              .quantity(new BigDecimal("2.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19.00"))
              .lineAmountExclTax(new BigDecimal("200.000"))
              .taxAmount(new BigDecimal("38.000"))
              .lineAmountInclTax(new BigDecimal("240.000")) // Incorrect: devrait être 238
              .build();

      ValidationResult result = validator.validateLineCalculations(line, 0);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_INCORRECT_LINE_TOTAL"));
    }

    @Test
    @DisplayName("Rejette un taux de TVA invalide")
    void shouldRejectInvalidTaxRate() {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("ITEM001")
              .quantity(new BigDecimal("1.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("18.00")) // Invalide: 18% n'existe pas en Tunisie
              .build();

      ValidationResult result = validator.validateLineCalculations(line, 0);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_INVALID_TAX_RATE"));
    }

    @Test
    @DisplayName("Accepte tous les taux de TVA valides")
    void shouldAcceptAllValidTaxRates() {
      BigDecimal[] validRates = {
        new BigDecimal("0.00"),
        new BigDecimal("7.00"),
        new BigDecimal("13.00"),
        new BigDecimal("19.00")
      };

      for (BigDecimal rate : validRates) {
        InvoiceLineDTO line =
            InvoiceLineDTO.builder()
                .lineNumber(1)
                .itemCode("ITEM001")
                .quantity(new BigDecimal("1.000"))
                .unitPrice(new BigDecimal("100.000"))
                .taxRate(rate)
                .build();

        ValidationResult result = validator.validateLineCalculations(line, 0);

        assertThat(result.getErrors()).noneMatch(e -> e.getCode().equals("ELF_INVALID_TAX_RATE"));
      }
    }

    @Test
    @DisplayName("Rejette une ligne sans quantité")
    void shouldRejectLineMissingQuantity() {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("ITEM001")
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19.00"))
              .build();

      ValidationResult result = validator.validateLineCalculations(line, 0);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_MISSING_QUANTITY"));
    }

    @Test
    @DisplayName("Tolère les arrondis (±0.001)")
    void shouldTolerateRounding() {
      // 200.000 × 19% = 38.000 exactement
      // Mais on tolère 37.999 ou 38.001
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("ITEM001")
              .quantity(new BigDecimal("2.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19.00"))
              .lineAmountExclTax(new BigDecimal("200.001")) // Tolérance
              .taxAmount(new BigDecimal("37.999")) // Tolérance
              .build();

      ValidationResult result = validator.validateLineCalculations(line, 0);

      // Les erreurs d'arrondi ne devraient pas être détectées
      assertThat(result.getErrors())
          .noneMatch(
              e ->
                  e.getCode().equals("ELF_INCORRECT_LINE_AMOUNT")
                      || e.getCode().equals("ELF_INCORRECT_TAX_AMOUNT"));
    }
  }

  @Nested
  @DisplayName("Calcul des totaux facture")
  class TotalsCalculationTests {

    @Test
    @DisplayName("Accepte des totaux corrects")
    void shouldAcceptCorrectTotals() {
      ElFatooraInvoiceDTO invoice =
          ElFatooraInvoiceDTO.builder()
              .invoiceNumber("FAC-001")
              .lines(
                  List.of(
                      createLine(
                          1,
                          new BigDecimal("100.000"),
                          new BigDecimal("19.00"),
                          new BigDecimal("100.000"),
                          new BigDecimal("19.000")),
                      createLine(
                          2,
                          new BigDecimal("200.000"),
                          new BigDecimal("7.00"),
                          new BigDecimal("200.000"),
                          new BigDecimal("14.000"))))
              .totalExcludingTax(new BigDecimal("300.000"))
              .totalTax(new BigDecimal("33.000"))
              .totalIncludingTax(new BigDecimal("333.000"))
              .build();

      ValidationResult result = validator.validateTotals(invoice);

      assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Détecte un total HT incorrect")
    void shouldDetectIncorrectTotalExcl() {
      ElFatooraInvoiceDTO invoice =
          ElFatooraInvoiceDTO.builder()
              .invoiceNumber("FAC-001")
              .lines(
                  List.of(
                      createLine(
                          1,
                          new BigDecimal("100.000"),
                          new BigDecimal("19.00"),
                          new BigDecimal("100.000"),
                          new BigDecimal("19.000"))))
              .totalExcludingTax(new BigDecimal("99.000")) // Incorrect
              .build();

      ValidationResult result = validator.validateTotals(invoice);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_INCORRECT_TOTAL_EXCL"));
    }

    @Test
    @DisplayName("Détecte une incohérence TTC ≠ HT + TVA")
    void shouldDetectInconsistentTotals() {
      ElFatooraInvoiceDTO invoice =
          ElFatooraInvoiceDTO.builder()
              .invoiceNumber("FAC-001")
              .lines(
                  List.of(
                      createLine(
                          1,
                          new BigDecimal("100.000"),
                          new BigDecimal("19.00"),
                          new BigDecimal("100.000"),
                          new BigDecimal("19.000"))))
              .totalExcludingTax(new BigDecimal("100.000"))
              .totalTax(new BigDecimal("19.000"))
              .totalIncludingTax(new BigDecimal("120.000")) // Devrait être 119.000
              .build();

      ValidationResult result = validator.validateTotals(invoice);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_INCONSISTENT_TOTALS"));
    }

    @Test
    @DisplayName("Rejette une facture sans lignes")
    void shouldRejectInvoiceWithNoLines() {
      ElFatooraInvoiceDTO invoice =
          ElFatooraInvoiceDTO.builder().invoiceNumber("FAC-001").lines(List.of()).build();

      ValidationResult result = validator.validateTotals(invoice);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_NO_LINES"));
    }
  }

  @Nested
  @DisplayName("Calculs automatiques")
  class AutoCalculationTests {

    @Test
    @DisplayName("Calcule le montant HT d'une ligne")
    void shouldCalculateLineAmountExclTax() {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .quantity(new BigDecimal("2.500"))
              .unitPrice(new BigDecimal("40.000"))
              .build();

      BigDecimal result = validator.calculateLineAmountExclTax(line);

      assertThat(result).isEqualByComparingTo(new BigDecimal("100.000"));
    }

    @Test
    @DisplayName("Calcule le montant de taxe d'une ligne")
    void shouldCalculateLineTaxAmount() {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .quantity(new BigDecimal("1.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19.00"))
              .lineAmountExclTax(new BigDecimal("100.000"))
              .build();

      BigDecimal result = validator.calculateLineTaxAmount(line);

      assertThat(result).isEqualByComparingTo(new BigDecimal("19.000"));
    }

    @Test
    @DisplayName("Arrondit correctement à 3 décimales")
    void shouldRoundToThreeDecimals() {
      // 33.333... devrait être arrondi à 33.333
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .quantity(new BigDecimal("1.000"))
              .unitPrice(new BigDecimal("33.333"))
              .build();

      BigDecimal result = validator.calculateLineAmountExclTax(line);

      assertThat(result.scale()).isEqualTo(3);
      assertThat(result).isEqualByComparingTo(new BigDecimal("33.333"));
    }
  }

  private InvoiceLineDTO createLine(
      int lineNumber,
      BigDecimal unitPrice,
      BigDecimal taxRate,
      BigDecimal amountExclTax,
      BigDecimal taxAmount) {
    return InvoiceLineDTO.builder()
        .lineNumber(lineNumber)
        .itemCode("ITEM" + lineNumber)
        .quantity(new BigDecimal("1.000"))
        .unitPrice(unitPrice)
        .taxRate(taxRate)
        .lineAmountExclTax(amountExclTax)
        .taxAmount(taxAmount)
        .lineAmountInclTax(amountExclTax.add(taxAmount))
        .build();
  }
}
