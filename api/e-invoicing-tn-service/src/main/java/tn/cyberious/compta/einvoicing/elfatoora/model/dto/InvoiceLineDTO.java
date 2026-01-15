package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.TaxTypeCode;
import tn.cyberious.compta.einvoicing.elfatoora.validation.constraints.ValidTaxRate;

/**
 * DTO representing an invoice line item in El Fatoora invoice.
 *
 * <p>Validation rules from XSD:
 *
 * <ul>
 *   <li>Item code: Max 35 characters, required
 *   <li>Item description: Max 500 characters
 *   <li>Quantity: 3 decimal places precision
 *   <li>Unit price: 3 decimal places precision
 *   <li>Tax rate: Valid Tunisian VAT rates (0%, 7%, 13%, 19%)
 *   <li>Amounts: 3 decimal places precision
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineDTO {

  /** Line number (item identifier). */
  @NotNull(message = "Line number is required")
  @Positive(message = "Line number must be positive")
  private Integer lineNumber;

  /** Item/product code. */
  @NotBlank(message = "Item code is required")
  @Size(max = 35, message = "Item code must not exceed 35 characters")
  private String itemCode;

  /** Item description. */
  @Size(max = 500, message = "Item description must not exceed 500 characters")
  private String itemDescription;

  /**
   * Unit of measurement type. Common values: UNIT, KGM (kilogram), LTR (liter), MTR (meter), PCE
   * (piece)
   */
  @NotBlank(message = "Unit type is required")
  @Size(max = 8, message = "Unit type must not exceed 8 characters")
  @Builder.Default
  private String unitType = "UNIT";

  /** Quantity (precision 3 decimals). */
  @NotNull(message = "Quantity is required")
  @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
  @Digits(integer = 12, fraction = 3, message = "Quantity must have at most 3 decimal places")
  private BigDecimal quantity;

  /** Unit price excluding tax (precision 3 decimals). */
  @NotNull(message = "Unit price is required")
  @DecimalMin(value = "0", message = "Unit price must be non-negative")
  @Digits(integer = 12, fraction = 3, message = "Unit price must have at most 3 decimal places")
  private BigDecimal unitPrice;

  /**
   * Taux de TVA en pourcentage.
   *
   * <p>Taux autorisés en Tunisie: 0.00%, 7.00%, 13.00%, 19.00%
   */
  @NotNull(message = "Le taux de TVA est obligatoire")
  @ValidTaxRate
  @Digits(integer = 2, fraction = 2, message = "Le taux doit avoir au maximum 2 décimales")
  private BigDecimal taxRate;

  /**
   * Code type de taxe.
   *
   * <p>Défaut: I-161 (TVA)
   */
  @Builder.Default private TaxTypeCode taxTypeCode = TaxTypeCode.TVA;

  /**
   * Montant ligne HT (calculé: quantité × prixUnitaire).
   *
   * <p>Précision: 3 décimales (millimes)
   */
  @Digits(integer = 12, fraction = 3, message = "Le montant HT doit avoir au maximum 3 décimales")
  private BigDecimal lineAmountExclTax;

  /**
   * Montant de taxe ligne.
   *
   * <p>Calculé: montantHT × tauxTVA / 100
   */
  @Digits(integer = 12, fraction = 3, message = "Le montant taxe doit avoir au maximum 3 décimales")
  private BigDecimal taxAmount;

  /**
   * Montant ligne TTC (calculé).
   *
   * <p>Calculé: montantHT + montantTaxe
   */
  @Digits(integer = 12, fraction = 3, message = "Le montant TTC doit avoir au maximum 3 décimales")
  private BigDecimal lineAmountInclTax;

  /** Langue pour la description. */
  @Builder.Default private String language = "fr";
}
