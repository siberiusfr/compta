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
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;

/**
 * DTO representing the customer (client/destinataire) in El Fatoora invoice.
 *
 * <p>The customer is identified by partner function code I-64.
 *
 * <p>Validation rules from XSD:
 *
 * <ul>
 *   <li>Tax identifier: Max 35 characters
 *   <li>Company name: Max 200 characters
 *   <li>Customer type: SMTP ou SMPP
 *   <li>Tax regime: P ou NP
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

  /**
   * Identifiant fiscal du client.
   *
   * <p>Selon le type:
   *
   * <ul>
   *   <li>I-01 (Matricule Fiscal): 13 caractères
   *   <li>I-02 (CIN): 8 chiffres
   *   <li>I-03 (Carte de Séjour): 9 chiffres
   *   <li>I-04 (Autre): Max 35 caractères
   * </ul>
   */
  @Size(max = 35, message = "L'identifiant fiscal ne peut pas dépasser 35 caractères")
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

  /**
   * Type d'identifiant du client.
   *
   * <p>Défaut: I-01 (Matricule Fiscal)
   */
  @Builder.Default private IdentifierType identifierType = IdentifierType.I_01;
}
