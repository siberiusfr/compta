package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing postal account (CCP) information for payment. Used when payment method is I-115
 * (postal transfer).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostalAccountDTO {

  /** Postal account number (CCP). */
  @NotBlank(message = "Account number is required")
  @Size(max = 20, message = "Account number must not exceed 20 characters")
  private String accountNumber;

  /** Account holder identifier (key). */
  @Size(max = 70, message = "Owner identifier must not exceed 70 characters")
  private String ownerIdentifier;

  /** Postal center code. */
  @Size(max = 11, message = "Center code must not exceed 11 characters")
  private String centerCode;

  /** Branch identifier. */
  @Size(max = 17, message = "Branch identifier must not exceed 17 characters")
  private String branchIdentifier;

  /** Institution name (typically "La poste"). */
  @Size(max = 70, message = "Institution name must not exceed 70 characters")
  @Builder.Default
  private String institutionName = "La poste";

  /** Country code (ISO 3166-1). */
  @Builder.Default private String country = "TN";
}
