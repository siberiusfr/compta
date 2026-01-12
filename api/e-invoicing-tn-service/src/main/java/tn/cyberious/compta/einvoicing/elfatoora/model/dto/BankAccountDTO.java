package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing bank account information for payment. Used when payment method is I-114 (bank
 * transfer).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDTO {

  /** Bank account number (RIB format for Tunisia: 20 characters). */
  @NotBlank(message = "Account number is required")
  @Size(max = 20, message = "Account number must not exceed 20 characters")
  private String accountNumber;

  /** Account holder identifier. */
  @Size(max = 70, message = "Owner identifier must not exceed 70 characters")
  private String ownerIdentifier;

  /** Bank code (BIC/SWIFT or local code). */
  @Size(max = 11, message = "Bank code must not exceed 11 characters")
  private String bankCode;

  /** Branch identifier. */
  @Size(max = 17, message = "Branch identifier must not exceed 17 characters")
  private String branchIdentifier;

  /** Bank/Institution name. */
  @Size(max = 70, message = "Institution name must not exceed 70 characters")
  private String institutionName;

  /** Country code (ISO 3166-1). */
  @Builder.Default private String country = "TN";
}
