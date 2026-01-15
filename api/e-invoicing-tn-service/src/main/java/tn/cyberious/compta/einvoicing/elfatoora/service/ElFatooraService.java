package tn.cyberious.compta.einvoicing.elfatoora.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.einvoicing.elfatoora.exception.ElFatooraException;
import tn.cyberious.compta.einvoicing.elfatoora.exception.ErrorCode;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CertificateInfo;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraResult;
import tn.cyberious.compta.einvoicing.elfatoora.validation.ElFatooraValidationService;
import tn.cyberious.compta.einvoicing.elfatoora.validation.ValidationResult;

/**
 * Facade service for El Fatoora invoice generation.
 *
 * <p>This service orchestrates:
 *
 * <ul>
 *   <li>Validation métier (via ElFatooraValidationService)
 *   <li>Generation XML (via ElFatooraXmlGeneratorService)
 *   <li>Signature XAdES (via XadesSignatureService)
 * </ul>
 *
 * <p>Workflow:
 *
 * <ol>
 *   <li>Validation complète AVANT génération
 *   <li>Génération du XML non signé
 *   <li>Validation XSD du XML
 *   <li>Signature XAdES-EPES
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElFatooraService {

  private final ElFatooraValidationService validationService;
  private final ElFatooraXmlGeneratorService xmlGeneratorService;
  private final XadesSignatureService signatureService;

  /**
   * Generates a complete El Fatoora invoice with XML and signature.
   *
   * <p>Workflow:
   *
   * <ol>
   *   <li>Validation métier complète
   *   <li>Génération XML non signé
   *   <li>Validation XSD
   *   <li>Signature XAdES-EPES (si certificat disponible)
   * </ol>
   *
   * @param invoice the invoice DTO
   * @return result containing unsigned and signed XML
   * @throws ElFatooraException si la validation échoue
   */
  public ElFatooraResult generateInvoice(ElFatooraInvoiceDTO invoice) {
    log.info("Generating El Fatoora invoice: {}", invoice.getInvoiceNumber());

    // ═══════════════════════════════════════════════════════
    // PHASE 0: VALIDATION MÉTIER COMPLÈTE
    // ═══════════════════════════════════════════════════════
    ValidationResult validation = validationService.validate(invoice);

    if (!validation.isValid()) {
      log.warn("Invoice validation failed with {} errors", validation.getErrorCount());
      log.debug("Validation errors:\n{}", validation.getFormattedErrorMessage());
      throw new ElFatooraException(
          ErrorCode.VALIDATION_FAILED,
          "Validation de la facture échouée",
          validation.getErrors().stream().map(e -> e.getField() + ": " + e.getMessage()).toList());
    }

    if (validation.hasWarnings()) {
      log.info("Invoice validation passed with {} warnings", validation.getWarningCount());
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 1: GÉNÉRATION XML NON SIGNÉ
    // ═══════════════════════════════════════════════════════
    String unsignedXml = xmlGeneratorService.generateXml(invoice);
    log.debug("Generated unsigned XML for invoice: {}", invoice.getInvoiceNumber());

    // ═══════════════════════════════════════════════════════
    // PHASE 2: VALIDATION XSD
    // ═══════════════════════════════════════════════════════
    xmlGeneratorService.validateXml(unsignedXml);
    log.debug("XML validated against XSD");

    // ═══════════════════════════════════════════════════════
    // PHASE 3: SIGNATURE XAdES-EPES
    // ═══════════════════════════════════════════════════════
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
   * @throws ElFatooraException si la validation échoue
   */
  public String generateUnsignedXml(ElFatooraInvoiceDTO invoice) {
    log.info("Generating unsigned El Fatoora XML: {}", invoice.getInvoiceNumber());

    ValidationResult validation = validationService.validate(invoice);
    if (!validation.isValid()) {
      log.warn("Invoice validation failed: {}", validation.getFormattedErrorMessage());
      throw new ElFatooraException(
          ErrorCode.VALIDATION_FAILED,
          "Validation de la facture échouée",
          validation.getErrors().stream().map(e -> e.getField() + ": " + e.getMessage()).toList());
    }

    String xml = xmlGeneratorService.generateXml(invoice);
    xmlGeneratorService.validateXml(xml);

    return xml;
  }

  /**
   * Validates an invoice before generation.
   *
   * <p>Délègue la validation au service spécialisé.
   *
   * @param invoice the invoice DTO
   * @return validation result
   */
  public ValidationResult validateInvoice(ElFatooraInvoiceDTO invoice) {
    return validationService.validate(invoice);
  }

  /**
   * Validates only tax identifiers.
   *
   * @param invoice the invoice DTO
   * @return validation result
   */
  public ValidationResult validateTaxIdentifiers(ElFatooraInvoiceDTO invoice) {
    return validationService.validateTaxIdentifiers(invoice);
  }

  /**
   * Validates only dates.
   *
   * @param invoice the invoice DTO
   * @return validation result
   */
  public ValidationResult validateDates(ElFatooraInvoiceDTO invoice) {
    return validationService.validateDates(invoice);
  }

  /**
   * Validates only calculations.
   *
   * @param invoice the invoice DTO
   * @return validation result
   */
  public ValidationResult validateCalculations(ElFatooraInvoiceDTO invoice) {
    return validationService.validateCalculations(invoice);
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
}
