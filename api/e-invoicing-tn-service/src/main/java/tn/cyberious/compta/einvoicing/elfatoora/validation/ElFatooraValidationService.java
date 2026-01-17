package tn.cyberious.compta.einvoicing.elfatoora.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CustomerDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.SupplierDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.DocumentTypeCode;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;

/**
 * Service principal de validation El Fatoora.
 *
 * <p>Valide une facture AVANT la génération XML en vérifiant toutes les règles métier définies dans
 * le XSD TEIF v1.8.8.
 *
 * <p>Types de validation:
 *
 * <ul>
 *   <li>Bean Validation (Jakarta Validation API)
 *   <li>Identifiants fiscaux (Matricule Fiscal, CIN, Passeport)
 *   <li>Dates (format, cohérence)
 *   <li>Calculs (montants, totaux, TVA)
 *   <li>Règles métier spécifiques
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElFatooraValidationService {

  private final Validator validator;
  private final TaxIdentifierValidator taxIdentifierValidator;
  private final DateValidator dateValidator;
  private final AmountCalculationValidator amountCalculationValidator;

  /**
   * Valide une facture complète AVANT génération XML.
   *
   * <p>Exécute toutes les validations:
   *
   * <ol>
   *   <li>Bean Validation (annotations Jakarta)
   *   <li>Identifiants fiscaux
   *   <li>Dates et cohérence temporelle
   *   <li>Calculs et montants
   *   <li>Règles métier spécifiques
   * </ol>
   *
   * @param invoice La facture à valider
   * @return Résultat de validation avec erreurs et avertissements
   */
  public ValidationResult validate(ElFatooraInvoiceDTO invoice) {
    log.debug("Validation complète de la facture {}", invoice.getInvoiceNumber());

    ValidationResult result = ValidationResult.valid();

    // 1. Validation nulle
    if (invoice == null) {
      return ValidationResult.invalid(
          ValidationError.of("invoice", "ELF_NULL_INVOICE", "La facture ne peut pas être nulle"));
    }

    // 2. Bean Validation (annotations Jakarta)
    result.merge(validateBeanConstraints(invoice));

    // 3. Validation des identifiants fiscaux
    result.merge(validateTaxIdentifiers(invoice));

    // 4. Validation des dates
    result.merge(validateDates(invoice));

    // 5. Validation des calculs
    result.merge(validateCalculations(invoice));

    // 6. Règles métier spécifiques
    result.merge(validateBusinessRules(invoice));

    log.debug(
        "Validation terminée - {} erreurs, {} avertissements",
        result.getErrorCount(),
        result.getWarningCount());

    return result;
  }

  /**
   * Valide uniquement les identifiants fiscaux.
   *
   * @param invoice La facture à valider
   * @return Résultat de validation
   */
  public ValidationResult validateTaxIdentifiers(ElFatooraInvoiceDTO invoice) {
    ValidationResult result = ValidationResult.valid();

    // Validation fournisseur
    if (invoice.getSupplier() != null) {
      result.merge(validateSupplierIdentifier(invoice.getSupplier()));
    }

    // Validation client
    if (invoice.getCustomer() != null) {
      result.merge(validateCustomerIdentifier(invoice.getCustomer()));
    }

    return result;
  }

  /**
   * Valide uniquement les dates.
   *
   * @param invoice La facture à valider
   * @return Résultat de validation
   */
  public ValidationResult validateDates(ElFatooraInvoiceDTO invoice) {
    ValidationResult result = ValidationResult.valid();

    // Validation cohérence des dates
    result.merge(
        dateValidator.validateDateCoherence(
            invoice.getInvoiceDate(),
            invoice.getDueDate(),
            invoice.getServicePeriodStart(),
            invoice.getServicePeriodEnd()));

    // Date facture ne peut pas être dans le futur
    if (invoice.getInvoiceDate() != null && invoice.getInvoiceDate().isAfter(LocalDate.now())) {
      result.addWarning(
          ValidationWarning.of(
              "invoiceDate", "ELF_WARN_FUTURE_DATE", "La date de facture est dans le futur"));
    }

    return result;
  }

  /**
   * Valide uniquement les calculs (montants, totaux, TVA).
   *
   * @param invoice La facture à valider
   * @return Résultat de validation
   */
  public ValidationResult validateCalculations(ElFatooraInvoiceDTO invoice) {
    ValidationResult result = ValidationResult.valid();

    // Validation des lignes
    if (invoice.getLines() != null) {
      for (int i = 0; i < invoice.getLines().size(); i++) {
        InvoiceLineDTO line = invoice.getLines().get(i);
        result.merge(amountCalculationValidator.validateLineCalculations(line, i));
      }
    }

    // Validation des totaux
    result.merge(amountCalculationValidator.validateTotals(invoice));

    // Validation de la ventilation TVA
    result.merge(amountCalculationValidator.validateTaxBreakdown(invoice));

    return result;
  }

  /**
   * Exécute la Bean Validation (annotations Jakarta).
   *
   * @param invoice La facture à valider
   * @return Résultat de validation
   */
  private ValidationResult validateBeanConstraints(ElFatooraInvoiceDTO invoice) {
    ValidationResult result = ValidationResult.valid();

    Set<ConstraintViolation<ElFatooraInvoiceDTO>> violations = validator.validate(invoice);

    for (ConstraintViolation<ElFatooraInvoiceDTO> violation : violations) {
      result.addError(
          ValidationError.builder()
              .field(violation.getPropertyPath().toString())
              .code("ELF_BEAN_VALIDATION")
              .message(violation.getMessage())
              .invalidValue(violation.getInvalidValue())
              .build());
    }

    return result;
  }

  /** Valide l'identifiant du fournisseur. */
  private ValidationResult validateSupplierIdentifier(SupplierDTO supplier) {
    ValidationResult result = ValidationResult.valid();

    if (supplier.getTaxIdentifier() == null || supplier.getTaxIdentifier().isBlank()) {
      result.addError(
          ValidationError.of(
              "supplier.taxIdentifier",
              "ELF_MISSING_SUPPLIER_TAX_ID",
              "Le matricule fiscal fournisseur est obligatoire"));
      return result;
    }

    IdentifierType type = supplier.getIdentifierType();
    if (type == null) {
      type = IdentifierType.I_01; // Défaut: Matricule fiscal
    }

    List<ValidationError> errors =
        taxIdentifierValidator.getValidationErrors(
            supplier.getTaxIdentifier(), type, "supplier.taxIdentifier");
    errors.forEach(result::addError);

    return result;
  }

  /** Valide l'identifiant du client. */
  private ValidationResult validateCustomerIdentifier(CustomerDTO customer) {
    ValidationResult result = ValidationResult.valid();

    if (customer.getTaxIdentifier() == null || customer.getTaxIdentifier().isBlank()) {
      // Client peut ne pas avoir de matricule fiscal (particulier)
      result.addWarning(
          ValidationWarning.of(
              "customer.taxIdentifier",
              "ELF_WARN_MISSING_CUSTOMER_TAX_ID",
              "Le client n'a pas d'identifiant fiscal"));
      return result;
    }

    IdentifierType type = customer.getIdentifierType();
    if (type == null) {
      // Essayer de détecter le type
      type = taxIdentifierValidator.detectType(customer.getTaxIdentifier());
    }

    List<ValidationError> errors =
        taxIdentifierValidator.getValidationErrors(
            customer.getTaxIdentifier(), type, "customer.taxIdentifier");
    errors.forEach(result::addError);

    return result;
  }

  /** Valide les règles métier spécifiques El Fatoora. */
  private ValidationResult validateBusinessRules(ElFatooraInvoiceDTO invoice) {
    ValidationResult result = ValidationResult.valid();

    // Règle 1: Avoir doit référencer une facture d'origine
    if (invoice.getDocumentType() != null
        && invoice.getDocumentType().getCode().equals(DocumentTypeCode.CREDIT_NOTE.getCode())) {
      if (invoice.getOriginalInvoiceReference() == null
          || invoice.getOriginalInvoiceReference().getReferenceValue() == null
          || invoice.getOriginalInvoiceReference().getReferenceValue().isBlank()) {
        result.addError(
            ValidationError.of(
                "originalInvoiceReference",
                "ELF_CREDIT_NOTE_MISSING_REFERENCE",
                "Un avoir (I-12) doit référencer la facture d'origine"));
      }
    }

    // Règle 2: Facture rectificative doit référencer une facture d'origine
    if (invoice.getDocumentType() != null
        && invoice
            .getDocumentType()
            .getCode()
            .equals(DocumentTypeCode.CORRECTIVE_INVOICE.getCode())) {
      if (invoice.getOriginalInvoiceReference() == null) {
        result.addError(
            ValidationError.of(
                "originalInvoiceReference",
                "ELF_CORRECTIVE_MISSING_REFERENCE",
                "Une facture rectificative (I-16) doit référencer la facture d'origine"));
      }
    }

    // Règle 3: Au moins une ligne requise
    if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
      result.addError(
          ValidationError.of(
              "lines", "ELF_NO_LINES", "La facture doit contenir au moins une ligne"));
    }

    // Règle 4: Numéro de facture unique (format recommandé)
    if (invoice.getInvoiceNumber() != null) {
      if (invoice.getInvoiceNumber().length() > 70) {
        result.addError(
            ValidationError.of(
                "invoiceNumber",
                "ELF_INVOICE_NUMBER_TOO_LONG",
                "Le numéro de facture ne peut pas dépasser 70 caractères"));
      }
    }

    // Règle 5: Devise supportée
    if (invoice.getCurrency() != null && !"TND".equals(invoice.getCurrency())) {
      result.addWarning(
          ValidationWarning.of(
              "currency",
              "ELF_WARN_NON_TND_CURRENCY",
              "La devise n'est pas TND - vérifiez la conformité"));
    }

    // Règle 6: Version du schéma
    if (invoice.getSchemaVersion() != null && !invoice.getSchemaVersion().startsWith("1.8")) {
      result.addWarning(
          ValidationWarning.of(
              "schemaVersion",
              "ELF_WARN_SCHEMA_VERSION",
              "Version du schéma non standard - recommandé: 1.8.8"));
    }

    return result;
  }
}
