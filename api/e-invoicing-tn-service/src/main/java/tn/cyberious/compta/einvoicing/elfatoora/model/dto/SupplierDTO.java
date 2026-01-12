package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the supplier (fournisseur) in El Fatoora invoice.
 *
 * <p>The supplier is identified by partner function code I-62.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

  /**
   * Tax identifier (Matricule Fiscal). Format: NNNNNNNXAMZZZ (7 digits + 1 letter + AM + 3 digits)
   * Example: 0736202XAM000
   */
  @NotBlank(message = "Tax identifier is required")
  @Pattern(
      regexp = "[0-9]{7}[ABCDEFGHJKLMNPQRSTVWXYZ][ABDNP][CMNP][0-9]{3}",
      message = "Invalid tax identifier format (expected: NNNNNNNXAMZZZ)")
  private String taxIdentifier;

  /** Company name (Raison Sociale). */
  @NotBlank(message = "Company name is required")
  @Size(max = 200, message = "Company name must not exceed 200 characters")
  private String companyName;

  /** Commercial register number (RNE/RNI). Reference I-815. */
  @Size(max = 200, message = "Registration number must not exceed 200 characters")
  private String registrationNumber;

  /** Legal form (SA, SARL, etc.). Reference I-816. */
  @Size(max = 200, message = "Legal form must not exceed 200 characters")
  private String legalForm;

  /** Supplier address. */
  @Valid
  @NotNull(message = "Address is required")
  private AddressDTO address;

  /** Contact information. */
  @Valid private ContactDTO contact;

  /** Identifier type for the partner. Defaults to I-01 (Matricule Fiscal). */
  @Builder.Default private IdentifierType identifierType = IdentifierType.MATRICULE_FISCAL;

  /** Enum for partner identifier types. */
  public enum IdentifierType {
    /** I-01: Matricule Fiscal (Tax ID). */
    MATRICULE_FISCAL("I-01"),
    /** I-02: CIN (National ID Card). */
    CIN("I-02"),
    /** I-03: Carte de Séjour (Residence Card). */
    CARTE_SEJOUR("I-03"),
    /** I-04: Other identifier. */
    OTHER("I-04");

    private final String code;

    IdentifierType(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }
}
