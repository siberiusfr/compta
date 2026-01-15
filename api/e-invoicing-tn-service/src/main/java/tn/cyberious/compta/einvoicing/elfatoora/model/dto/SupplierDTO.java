package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;
import tn.cyberious.compta.einvoicing.elfatoora.validation.constraints.ValidTaxIdentifier;

/**
 * DTO representing the supplier (fournisseur) in El Fatoora invoice.
 *
 * <p>The supplier is identified by partner function code I-62.
 *
 * <p>Validation rules from XSD:
 *
 * <ul>
 *   <li>Tax identifier: Matricule fiscal format (13 chars)
 *   <li>Company name: Max 200 characters
 *   <li>Address: Required
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

  /**
   * Tax identifier (Matricule Fiscal).
   *
   * <p>Format: NNNNNNNLPCE99 (13 caractères)
   *
   * <ul>
   *   <li>N: 7 chiffres (0-9)
   *   <li>L: 1 lettre majuscule (A-Z sauf I et O)
   *   <li>P: Position fiscale (A, B, D, N, P)
   *   <li>C: Catégorie (C, M, N, P, E)
   *   <li>E: 3 chiffres établissement (000-999)
   * </ul>
   *
   * <p>Exemple valide: 0736202XAM000
   */
  @NotBlank(message = "Le matricule fiscal est obligatoire")
  @ValidTaxIdentifier(type = IdentifierType.I_01, message = "Matricule fiscal invalide")
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

  /**
   * Type d'identifiant du partenaire.
   *
   * <p>Défaut: I-01 (Matricule Fiscal)
   */
  @Builder.Default private IdentifierType identifierType = IdentifierType.I_01;
}
