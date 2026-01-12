package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Result DTO for invoice validation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

  /** Whether the validation passed. */
  private boolean valid;

  /** List of validation errors. */
  @Builder.Default private List<ValidationError> errors = new ArrayList<>();

  /** List of validation warnings. */
  @Builder.Default private List<ValidationWarning> warnings = new ArrayList<>();

  /** Add an error to the result. */
  public void addError(String field, String message) {
    errors.add(new ValidationError(field, message));
  }

  /** Add a warning to the result. */
  public void addWarning(String field, String message) {
    warnings.add(new ValidationWarning(field, message));
  }

  /** Check if there are any errors. */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  /** Check if there are any warnings. */
  public boolean hasWarnings() {
    return !warnings.isEmpty();
  }

  /** Create a valid result. */
  public static ValidationResult valid() {
    return ValidationResult.builder().valid(true).build();
  }

  /** Create an invalid result with a single error. */
  public static ValidationResult invalid(String field, String message) {
    ValidationResult result = new ValidationResult();
    result.setValid(false);
    result.addError(field, message);
    return result;
  }

  /** Validation error details. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ValidationError {
    /** Field name that has the error. */
    private String field;

    /** Error message. */
    private String message;
  }

  /** Validation warning details. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ValidationWarning {
    /** Field name that has the warning. */
    private String field;

    /** Warning message. */
    private String message;
  }
}
