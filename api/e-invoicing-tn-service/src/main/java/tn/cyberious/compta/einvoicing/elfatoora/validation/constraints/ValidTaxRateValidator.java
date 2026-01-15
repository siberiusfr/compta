package tn.cyberious.compta.einvoicing.elfatoora.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.TaxTypeCode;

/** Implémentation du validateur pour {@link ValidTaxRate}. */
public class ValidTaxRateValidator implements ConstraintValidator<ValidTaxRate, BigDecimal> {

  @Override
  public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
    // Null est valide (utiliser @NotNull pour rendre obligatoire)
    if (value == null) {
      return true;
    }

    return TaxTypeCode.isValidVatRate(value);
  }
}
