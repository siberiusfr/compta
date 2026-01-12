package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO containing information about the signing certificate. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateInfo {

  /** Certificate subject DN (Distinguished Name). */
  private String subjectDN;

  /** Certificate issuer DN. */
  private String issuerDN;

  /** Certificate serial number. */
  private String serialNumber;

  /** Certificate validity start date. */
  private LocalDateTime validFrom;

  /** Certificate validity end date. */
  private LocalDateTime validTo;

  /** Whether the certificate is currently valid. */
  private boolean valid;

  /** Certificate alias in the keystore. */
  private String alias;

  /** Certificate SHA-1 fingerprint. */
  private String sha1Fingerprint;

  /** Certificate SHA-256 fingerprint. */
  private String sha256Fingerprint;

  /** Public key algorithm. */
  private String keyAlgorithm;

  /** Key size in bits. */
  private int keySize;
}
