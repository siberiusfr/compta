package tn.cyberious.compta.einvoicing.elfatoora.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CertificateInfo;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CustomerDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraResult;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.SupplierDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ValidationResult;

/**
 * Facade service for El Fatoora invoice generation.
 *
 * <p>This service orchestrates the XML generation and signature services to produce complete El
 * Fatoora electronic invoices.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElFatooraService {

  private static final Pattern MATRICULE_FISCAL_PATTERN =
      Pattern.compile("[0-9]{7}[ABCDEFGHJKLMNPQRSTVWXYZ][ABDNP][CMNP][0-9]{3}");
  private static final Pattern CIN_PATTERN = Pattern.compile("[0-9]{8}");
  private static final Pattern CARTE_SEJOUR_PATTERN = Pattern.compile("[0-9]{9}");
  private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("[0-9]{4}");

  private final ElFatooraXmlGeneratorService xmlGeneratorService;
  private final XadesSignatureService signatureService;

  /**
   * Generates a complete El Fatoora invoice with XML and signature.
   *
   * @param invoice the invoice DTO
   * @return result containing unsigned and signed XML
   */
  public ElFatooraResult generateInvoice(ElFatooraInvoiceDTO invoice) {
    log.info("Generating El Fatoora invoice: {}", invoice.getInvoiceNumber());

    // Validate the invoice
    ValidationResult validation = validateInvoice(invoice);
    if (!validation.isValid()) {
      log.warn("Invoice validation failed: {}", validation.getErrors());
      throw new IllegalArgumentException("Invoice validation failed: " + validation.getErrors());
    }

    // Generate unsigned XML
    String unsignedXml = xmlGeneratorService.generateXml(invoice);
    log.debug("Generated unsigned XML for invoice: {}", invoice.getInvoiceNumber());

    // Validate against XSD
    xmlGeneratorService.validateXml(unsignedXml);
    log.debug("XML validated against XSD");

    // Sign the XML
    String signedXml = null;
    boolean signed = false;
    CertificateInfo certInfo = null;

    if (signatureService.isCertificateAvailable()) {
      signedXml = signatureService.signXml(unsignedXml);
      signed = true;
      certInfo = signatureService.getCertificateInfo();
      log.info("Invoice signed successfully: {}", invoice.getInvoiceNumber());
    } else {
      log.warn("No certificate available, invoice will not be signed");
      signedXml = unsignedXml;
    }

    return ElFatooraResult.builder()
        .invoiceNumber(invoice.getInvoiceNumber())
        .unsignedXml(unsignedXml)
        .signedXml(signedXml)
        .generatedAt(LocalDateTime.now())
        .certificateUsed(certInfo)
        .xsdValidated(true)
        .signed(signed)
        .build();
  }

  /**
   * Generates unsigned XML only (for testing purposes).
   *
   * @param invoice the invoice DTO
   * @return unsigned XML string
   */
  public String generateUnsignedXml(ElFatooraInvoiceDTO invoice) {
    log.info("Generating unsigned El Fatoora XML: {}", invoice.getInvoiceNumber());

    ValidationResult validation = validateInvoice(invoice);
    if (!validation.isValid()) {
      log.warn("Invoice validation failed: {}", validation.getErrors());
      throw new IllegalArgumentException("Invoice validation failed: " + validation.getErrors());
    }

    String xml = xmlGeneratorService.generateXml(invoice);
    xmlGeneratorService.validateXml(xml);

    return xml;
  }

  /**
   * Validates an invoice before generation.
   *
   * @param invoice the invoice DTO
   * @return validation result
   */
  public ValidationResult validateInvoice(ElFatooraInvoiceDTO invoice) {
    ValidationResult result = new ValidationResult();
    result.setValid(true);

    // Required fields
    if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
      result.addError("invoiceNumber", "Invoice number is required");
    }

    if (invoice.getInvoiceDate() == null) {
      result.addError("invoiceDate", "Invoice date is required");
    }

    if (invoice.getDocumentType() == null) {
      result.addError("documentType", "Document type is required");
    }

    // Validate supplier
    validateSupplier(invoice.getSupplier(), result);

    // Validate customer
    validateCustomer(invoice.getCustomer(), result);

    // Validate lines
    if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
      result.addError("lines", "At least one invoice line is required");
    } else {
      for (int i = 0; i < invoice.getLines().size(); i++) {
        validateLine(invoice.getLines().get(i), i, result);
      }
    }

    // Currency validation
    if (invoice.getCurrency() != null && !invoice.getCurrency().equals("TND")) {
      result.addWarning("currency", "Only TND currency is fully supported for Tunisian invoices");
    }

    result.setValid(!result.hasErrors());
    return result;
  }

  /**
   * Parses an El Fatoora XML document into a DTO.
   *
   * @param xml the XML string
   * @return parsed invoice DTO
   */
  public ElFatooraInvoiceDTO parseInvoiceXml(String xml) {
    return xmlGeneratorService.parseXml(xml);
  }

  /**
   * Verifies the signature on a signed El Fatoora XML document.
   *
   * @param signedXml the signed XML string
   * @return true if signature is valid
   */
  public boolean verifySignature(String signedXml) {
    return signatureService.verifySignature(signedXml);
  }

  /**
   * Gets information about the loaded signing certificate.
   *
   * @return certificate info
   */
  public CertificateInfo getCertificateInfo() {
    return signatureService.getCertificateInfo();
  }

  private void validateSupplier(SupplierDTO supplier, ValidationResult result) {
    if (supplier == null) {
      result.addError("supplier", "Supplier is required");
      return;
    }

    if (supplier.getTaxIdentifier() == null || supplier.getTaxIdentifier().isBlank()) {
      result.addError("supplier.taxIdentifier", "Supplier tax identifier is required");
    } else {
      validateTaxIdentifier(
          supplier.getTaxIdentifier(), supplier.getIdentifierType(), "supplier", result);
    }

    if (supplier.getCompanyName() == null || supplier.getCompanyName().isBlank()) {
      result.addError("supplier.companyName", "Supplier company name is required");
    }

    if (supplier.getAddress() == null) {
      result.addError("supplier.address", "Supplier address is required");
    } else {
      if (supplier.getAddress().getPostalCode() != null
          && !POSTAL_CODE_PATTERN.matcher(supplier.getAddress().getPostalCode()).matches()) {
        result.addError("supplier.address.postalCode", "Postal code must be exactly 4 digits");
      }
    }
  }

  private void validateCustomer(CustomerDTO customer, ValidationResult result) {
    if (customer == null) {
      result.addError("customer", "Customer is required");
      return;
    }

    if (customer.getTaxIdentifier() == null || customer.getTaxIdentifier().isBlank()) {
      result.addError("customer.taxIdentifier", "Customer tax identifier is required");
    } else {
      validateTaxIdentifier(
          customer.getTaxIdentifier(), customer.getIdentifierType(), "customer", result);
    }

    if (customer.getCompanyName() == null || customer.getCompanyName().isBlank()) {
      result.addError("customer.companyName", "Customer name is required");
    }

    if (customer.getCustomerType() == null || customer.getCustomerType().isBlank()) {
      result.addError("customer.customerType", "Customer type (SMTP or SMPP) is required");
    } else if (!customer.getCustomerType().equals("SMTP")
        && !customer.getCustomerType().equals("SMPP")) {
      result.addError("customer.customerType", "Customer type must be SMTP or SMPP");
    }

    if (customer.getAddress() == null) {
      result.addError("customer.address", "Customer address is required");
    } else {
      if (customer.getAddress().getPostalCode() != null
          && !POSTAL_CODE_PATTERN.matcher(customer.getAddress().getPostalCode()).matches()) {
        result.addError("customer.address.postalCode", "Postal code must be exactly 4 digits");
      }
    }
  }

  private void validateTaxIdentifier(
      String identifier, Enum<?> type, String prefix, ValidationResult result) {

    String typeCode = null;
    if (type instanceof SupplierDTO.IdentifierType) {
      typeCode = ((SupplierDTO.IdentifierType) type).getCode();
    } else if (type instanceof CustomerDTO.IdentifierType) {
      typeCode = ((CustomerDTO.IdentifierType) type).getCode();
    }

    if (typeCode == null) {
      return;
    }

    switch (typeCode) {
      case "I-01": // Matricule Fiscal
        if (!MATRICULE_FISCAL_PATTERN.matcher(identifier).matches()) {
          result.addError(
              prefix + ".taxIdentifier",
              "Invalid Matricule Fiscal format. Expected: 7 digits + 1 letter + AM + 3 digits (e.g., 0736202XAM000)");
        }
        break;
      case "I-02": // CIN
        if (!CIN_PATTERN.matcher(identifier).matches()) {
          result.addError(prefix + ".taxIdentifier", "Invalid CIN format. Expected: 8 digits");
        }
        break;
      case "I-03": // Carte de Séjour
        if (!CARTE_SEJOUR_PATTERN.matcher(identifier).matches()) {
          result.addError(
              prefix + ".taxIdentifier", "Invalid Carte de Séjour format. Expected: 9 digits");
        }
        break;
      default:
        // I-04 (Other) - no specific validation
        break;
    }
  }

  private void validateLine(InvoiceLineDTO line, int index, ValidationResult result) {
    String prefix = "lines[" + index + "]";

    if (line.getLineNumber() == null || line.getLineNumber() <= 0) {
      result.addError(prefix + ".lineNumber", "Line number must be a positive integer");
    }

    if (line.getItemCode() == null || line.getItemCode().isBlank()) {
      result.addError(prefix + ".itemCode", "Item code is required");
    }

    if (line.getQuantity() == null || line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
      result.addError(prefix + ".quantity", "Quantity must be greater than 0");
    }

    if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
      result.addError(prefix + ".unitPrice", "Unit price must be non-negative");
    }

    if (line.getTaxRate() == null) {
      result.addError(prefix + ".taxRate", "Tax rate is required");
    } else {
      // Validate Tunisian VAT rates
      BigDecimal rate = line.getTaxRate();
      if (!isValidTaxRate(rate)) {
        result.addWarning(
            prefix + ".taxRate",
            "Non-standard tax rate. Standard Tunisian rates are: 0%, 7%, 13%, 19%");
      }
    }

    if (line.getUnitType() == null || line.getUnitType().isBlank()) {
      result.addError(prefix + ".unitType", "Unit type is required");
    }
  }

  private boolean isValidTaxRate(BigDecimal rate) {
    // Standard Tunisian VAT rates
    return rate.compareTo(BigDecimal.ZERO) == 0
        || rate.compareTo(BigDecimal.valueOf(7)) == 0
        || rate.compareTo(BigDecimal.valueOf(13)) == 0
        || rate.compareTo(BigDecimal.valueOf(19)) == 0;
  }
}
