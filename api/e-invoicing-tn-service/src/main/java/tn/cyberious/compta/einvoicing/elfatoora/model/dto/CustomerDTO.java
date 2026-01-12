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
 * DTO representing the customer (client/destinataire) in El Fatoora invoice.
 *
 * <p>The customer is identified by partner function code I-64.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

  /**
   * Tax identifier (Matricule Fiscal) or other identifier. For Matricule Fiscal: NNNNNNNXAMZZZ (7
   * digits + 1 letter + AM + 3 digits) For CIN: 8 digits For Carte de Séjour: 9 digits
   */
  @NotBlank(message = "Tax identifier is required")
  @Size(max = 35, message = "Tax identifier must not exceed 35 characters")
  private String taxIdentifier;

  /** Company name or individual name. */
  @NotBlank(message = "Customer name is required")
  @Size(max = 200, message = "Customer name must not exceed 200 characters")
  private String companyName;

  /** Commercial register number (reference I-81). */
  @Size(max = 200, message = "Registration number must not exceed 200 characters")
  private String registrationNumber;

  /**
   * Customer type (reference I-811). SMTP = Sujet Moral Tunisien Passible (Professional entity
   * subject to tax) SMPP = Sujet Moral ou Physique Particulier (Individual or non-taxable entity)
   */
  @NotBlank(message = "Customer type is required")
  @Pattern(regexp = "SMTP|SMPP", message = "Customer type must be SMTP or SMPP")
  private String customerType;

  /** Establishment name (reference I-813). */
  @Size(max = 200, message = "Establishment name must not exceed 200 characters")
  private String establishmentName;

  /**
   * Tax regime (reference I-812). P = Passible (Subject to VAT) NP = Non Passible (Not subject to
   * VAT)
   */
  @Pattern(regexp = "P|NP", message = "Tax regime must be P or NP")
  private String taxRegime;

  /** VAT number if applicable (reference I-814). */
  @Size(max = 200, message = "VAT number must not exceed 200 characters")
  private String vatNumber;

  /** Customer address. */
  @Valid
  @NotNull(message = "Address is required")
  private AddressDTO address;

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
