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

/** DTO representing an invoice line item in El Fatoora invoice. */
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

  /** Tax rate percentage. Valid values for Tunisia: 0.00, 7.00, 13.00, 19.00 */
  @NotNull(message = "Tax rate is required")
  @DecimalMin(value = "0", message = "Tax rate must be non-negative")
  @Digits(integer = 2, fraction = 2, message = "Tax rate must have at most 2 decimal places")
  private BigDecimal taxRate;

  /** Tax type code. I-1601: Droit de timbre (Stamp duty) I-1602: TVA (VAT) I-1603: FODEC */
  @Builder.Default private TaxTypeCode taxTypeCode = TaxTypeCode.TVA;

  /** Line amount excluding tax (calculated: quantity × unitPrice). */
  @Digits(integer = 12, fraction = 3, message = "Line amount must have at most 3 decimal places")
  private BigDecimal lineAmountExclTax;

  /** Line amount including tax (calculated). */
  @Digits(integer = 12, fraction = 3, message = "Line amount must have at most 3 decimal places")
  private BigDecimal lineAmountInclTax;

  /** Language for item description. */
  @Builder.Default private String language = "fr";

  /** Enum for tax type codes. */
  public enum TaxTypeCode {
    /** I-1601: Stamp duty (Droit de timbre). */
    DROIT_TIMBRE("I-1601", "droit de timbre"),
    /** I-1602: VAT (TVA). */
    TVA("I-1602", "TVA"),
    /** I-1603: FODEC. */
    FODEC("I-1603", "FODEC");

    private final String code;
    private final String description;

    TaxTypeCode(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }
}
