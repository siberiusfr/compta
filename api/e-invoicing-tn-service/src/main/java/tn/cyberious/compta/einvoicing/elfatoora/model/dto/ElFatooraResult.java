package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Result DTO returned after generating an El Fatoora invoice. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElFatooraResult {

  /** Invoice number that was generated. */
  private String invoiceNumber;

  /** Unsigned XML content. */
  private String unsignedXml;

  /** Signed XML content (with XAdES signature). */
  private String signedXml;

  /** Timestamp when the invoice was generated. */
  private LocalDateTime generatedAt;

  /** Information about the certificate used for signing. */
  private CertificateInfo certificateUsed;

  /** Whether the XML was validated against XSD. */
  private boolean xsdValidated;

  /** Whether the signature was successfully applied. */
  private boolean signed;
}
