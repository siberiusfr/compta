package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Main DTO representing an El Fatoora electronic invoice.
 *
 * <p>This DTO contains all the information needed to generate a valid El Fatoora XML document
 * conforming to the Tunisian electronic invoice standard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElFatooraInvoiceDTO {

  /** Unique invoice number/identifier. */
  @NotBlank(message = "Invoice number is required")
  @Size(max = 70, message = "Invoice number must not exceed 70 characters")
  private String invoiceNumber;

  /** Invoice issue date. */
  @NotNull(message = "Invoice date is required")
  private LocalDate invoiceDate;

  /** Payment due date. */
  private LocalDate dueDate;

  /** Service period start date. */
  private LocalDate servicePeriodStart;

  /** Service period end date. */
  private LocalDate servicePeriodEnd;

  /**
   * Document type code. I-11: Invoice (Facture) I-12: Credit note (Avoir) I-13: Debit note (Note de
   * débit) I-14: Proforma invoice (Facture proforma) I-15: Self-billing invoice I-16: Other
   */
  @NotNull(message = "Document type is required")
  @Builder.Default
  private DocumentType documentType = DocumentType.INVOICE;

  /** Document type description. */
  @Size(max = 35, message = "Document type description must not exceed 35 characters")
  private String documentTypeDescription;

  /** Supplier (seller) information. */
  @Valid
  @NotNull(message = "Supplier is required")
  private SupplierDTO supplier;

  /** Customer (buyer) information. */
  @Valid
  @NotNull(message = "Customer is required")
  private CustomerDTO customer;

  /** Invoice line items. */
  @Valid
  @NotEmpty(message = "At least one invoice line is required")
  private List<InvoiceLineDTO> lines;

  /** Payment terms and conditions. */
  @Valid private List<PaymentTermsDTO> paymentTerms;

  /** Total amount excluding tax (HT). */
  @Digits(
      integer = 12,
      fraction = 3,
      message = "Total excluding tax must have at most 3 decimal places")
  private BigDecimal totalExcludingTax;

  /** Total tax amount. */
  @Digits(integer = 12, fraction = 3, message = "Total tax must have at most 3 decimal places")
  private BigDecimal totalTax;

  /** Total amount including tax (TTC). */
  @Digits(
      integer = 12,
      fraction = 3,
      message = "Total including tax must have at most 3 decimal places")
  private BigDecimal totalIncludingTax;

  /** Stamp duty amount (droit de timbre). */
  @Digits(integer = 12, fraction = 3, message = "Stamp duty must have at most 3 decimal places")
  private BigDecimal stampDuty;

  /** Invoice total amount in words. */
  @Size(max = 500, message = "Amount in words must not exceed 500 characters")
  private String totalAmountInWords;

  /** Currency code (ISO 4217). Defaults to TND. */
  @Builder.Default private String currency = "TND";

  /** TEIF schema version. */
  @Builder.Default private String schemaVersion = "1.8.8";

  /** Reference to original invoice (for credit notes). */
  private DocumentReferenceDTO originalInvoiceReference;

  /** Free text notes. */
  @Size(max = 500, message = "Notes must not exceed 500 characters")
  private String notes;

  /** Enum for document type codes. */
  public enum DocumentType {
    /** I-11: Invoice. */
    INVOICE("I-11", "Facture"),
    /** I-12: Credit note. */
    CREDIT_NOTE("I-12", "Avoir"),
    /** I-13: Debit note. */
    DEBIT_NOTE("I-13", "Note de débit"),
    /** I-14: Proforma invoice. */
    PROFORMA("I-14", "Facture proforma"),
    /** I-15: Self-billing invoice. */
    SELF_BILLING("I-15", "Auto-facturation"),
    /** I-16: Other document type. */
    OTHER("I-16", "Autre");

    private final String code;
    private final String description;

    DocumentType(String code, String description) {
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
