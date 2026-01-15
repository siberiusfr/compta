package tn.cyberious.compta.einvoicing.elfatoora.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;

/**
 * Implémentation du validateur pour {@link ValidCountryCode}.
 *
 * <p>Utilise les codes ISO 3166-1 alpha-2 de Java.
 */
public class ValidCountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

  /** Cache des codes pays valides (ISO 3166-1 alpha-2) */
  private static final Set<String> VALID_COUNTRY_CODES = Set.of(Locale.getISOCountries());

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Null est valide (utiliser @NotNull pour rendre obligatoire)
    if (value == null || value.isBlank()) {
      return true;
    }

    // Convertir en majuscules et vérifier
    String upperCode = value.toUpperCase();
    return VALID_COUNTRY_CODES.contains(upperCode);
  }
}
