package tn.cyberious.compta.einvoicing.elfatoora.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Error codes for El Fatoora invoice generation and validation. */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  INVALID_XML_STRUCTURE("ELF001", "Invalid XML structure"),
  XSD_VALIDATION_FAILED("ELF002", "XML does not conform to El Fatoora XSD schema"),
  INVALID_TAX_IDENTIFIER("ELF003", "Invalid tax identifier format (expected: NNNNNNNXAMZZZ)"),
  INVALID_DATE_FORMAT("ELF004", "Invalid date format"),
  CERTIFICATE_ERROR("ELF005", "Certificate loading or validation error"),
  SIGNATURE_FAILED("ELF006", "XML signature creation failed"),
  SIGNATURE_VERIFICATION_FAILED("ELF007", "XML signature verification failed"),
  TAX_CALCULATION_ERROR("ELF008", "Tax calculation error"),
  MISSING_REQUIRED_FIELD("ELF009", "Missing required field"),
  INVALID_DOCUMENT_TYPE("ELF010", "Invalid document type code"),
  INVALID_AMOUNT_FORMAT("ELF011", "Invalid amount format (expected 3 decimal places)"),
  INVALID_POSTAL_CODE("ELF012", "Invalid postal code format (expected 4 digits)"),
  INVALID_TAX_RATE("ELF013", "Invalid tax rate (expected: 0, 7, 13, or 19)"),
  INVALID_CURRENCY("ELF014", "Invalid currency code (only TND supported)"),
  XML_PARSING_ERROR("ELF015", "Error parsing XML document"),
  MARSHALLING_ERROR("ELF016", "Error marshalling object to XML"),
  UNMARSHALLING_ERROR("ELF017", "Error unmarshalling XML to object"),
  VALIDATION_FAILED("ELF018", "Invoice validation failed");

  private final String code;
  private final String message;
}
