package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO representing payment terms and conditions in El Fatoora invoice. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTermsDTO {

  /**
   * Payment method code. I-111: Cash (Espèces) I-112: Check (Chèque) I-113: Credit card (Carte de
   * crédit) I-114: Bank transfer (Virement bancaire) I-115: Postal transfer (Mandat postal / CCP)
   * I-116: Direct debit (Prélèvement) I-117: Letter of credit I-118: Other
   */
  @NotNull(message = "Payment method is required")
  private PaymentMethod paymentMethod;

  /** Payment terms type code (e.g., I-114 for immediate payment). */
  private String paymentTermsTypeCode;

  /** Payment description / instructions. */
  @Size(max = 500, message = "Payment description must not exceed 500 characters")
  private String description;

  /** Bank account details (for bank transfer). */
  @Valid private BankAccountDTO bankAccount;

  /** Postal account details (for postal transfer). */
  @Valid private PostalAccountDTO postalAccount;

  /** Enum for payment method codes. */
  public enum PaymentMethod {
    /** I-111: Cash. */
    CASH("I-111", "Espèces"),
    /** I-112: Check. */
    CHECK("I-112", "Chèque"),
    /** I-113: Credit card. */
    CREDIT_CARD("I-113", "Carte de crédit"),
    /** I-114: Bank transfer. */
    BANK_TRANSFER("I-114", "Virement bancaire"),
    /** I-115: Postal transfer. */
    POSTAL_TRANSFER("I-115", "Mandat postal / CCP"),
    /** I-116: Direct debit. */
    DIRECT_DEBIT("I-116", "Prélèvement"),
    /** I-117: Letter of credit. */
    LETTER_OF_CREDIT("I-117", "Lettre de crédit"),
    /** I-118: Other. */
    OTHER("I-118", "Autre");

    private final String code;
    private final String description;

    PaymentMethod(String code, String description) {
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
