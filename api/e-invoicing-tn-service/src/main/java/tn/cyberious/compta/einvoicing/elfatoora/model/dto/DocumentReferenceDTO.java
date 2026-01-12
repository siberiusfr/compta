package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a document reference in El Fatoora invoice. Used for referencing original
 * invoices in credit/debit notes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReferenceDTO {

  /**
   * Reference type code. I-81: Matricule fiscal client (Client tax ID) I-82: Reference commande
   * client (Client order reference) I-83: Reference commande fournisseur (Supplier order reference)
   * I-84: Reference facture originale (Original invoice reference) I-85: Reference contrat
   * (Contract reference) I-86: Reference projet (Project reference) I-87: Reference bon de
   * livraison (Delivery note reference) I-88: Reference TTN (TTN reference)
   */
  @NotBlank(message = "Reference type is required")
  private String referenceType;

  /** Reference value/number. */
  @NotBlank(message = "Reference value is required")
  @Size(max = 200, message = "Reference value must not exceed 200 characters")
  private String referenceValue;

  /** Reference date (if applicable). */
  private LocalDate referenceDate;

  /** Common reference type codes. */
  public static class ReferenceType {
    public static final String CLIENT_TAX_ID = "I-81";
    public static final String CLIENT_ORDER = "I-82";
    public static final String SUPPLIER_ORDER = "I-83";
    public static final String ORIGINAL_INVOICE = "I-84";
    public static final String CONTRACT = "I-85";
    public static final String PROJECT = "I-86";
    public static final String DELIVERY_NOTE = "I-87";
    public static final String TTN_REFERENCE = "I-88";

    private ReferenceType() {}
  }
}
