package tn.cyberious.compta.einvoicing.elfatoora.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;

/**
 * Implémentation du validateur pour {@link ValidTaxIdentifier}.
 *
 * <p>Utilise les patterns définis dans {@link IdentifierType} pour valider les identifiants.
 */
public class ValidTaxIdentifierValidator
    implements ConstraintValidator<ValidTaxIdentifier, String> {

  private IdentifierType type;

  @Override
  public void initialize(ValidTaxIdentifier annotation) {
    this.type = annotation.type();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Null est valide (utiliser @NotNull pour rendre obligatoire)
    if (value == null || value.isBlank()) {
      return true;
    }

    boolean valid = type.matches(value);

    if (!valid) {
      // Personnaliser le message d'erreur
      context.disableDefaultConstraintViolation();
      String message = buildErrorMessage(value);
      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    return valid;
  }

  private String buildErrorMessage(String value) {
    return switch (type) {
      case I_01 ->
          String.format(
              "Matricule fiscal invalide '%s'. Format attendu: 7 chiffres + lettre"
                  + " (sauf I,O) + position (A,B,D,N,P) + catégorie (C,M,N,P,E) +"
                  + " 3 chiffres. Exemple: 0736202XAM000",
              value);
      case I_02 ->
          "CIN invalide '%s'. Format attendu: 8 chiffres. Exemple: 12345678".formatted(value);
      case I_03 ->
          String.format(
              "Carte de séjour/Passeport invalide '%s'. Format attendu: 9 chiffres."
                  + " Exemple: 123456789",
              value);
      case I_04 -> "Identifiant trop long '%s'. Maximum 35 caractères autorisés.".formatted(value);
    };
  }
}
