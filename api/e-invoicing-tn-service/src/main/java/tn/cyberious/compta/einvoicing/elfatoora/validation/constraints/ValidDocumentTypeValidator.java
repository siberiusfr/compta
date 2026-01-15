package tn.cyberious.compta.einvoicing.elfatoora.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.DocumentTypeCode;

/** Implémentation du validateur pour {@link ValidDocumentType}. */
public class ValidDocumentTypeValidator implements ConstraintValidator<ValidDocumentType, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Null est valide (utiliser @NotNull pour rendre obligatoire)
    if (value == null || value.isBlank()) {
      return true;
    }

    return DocumentTypeCode.isValidCode(value);
  }
}
