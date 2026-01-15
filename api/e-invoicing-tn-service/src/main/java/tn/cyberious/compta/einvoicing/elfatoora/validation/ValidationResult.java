package tn.cyberious.compta.einvoicing.elfatoora.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

/**
 * Résultat de la validation d'une facture El Fatoora.
 *
 * <p>Contient les erreurs (bloquantes) et avertissements (informatifs).
 */
@Data
@Builder
public class ValidationResult {

  /** Liste des erreurs de validation (bloquantes) */
  @Builder.Default private List<ValidationError> errors = new ArrayList<>();

  /** Liste des avertissements (non bloquants) */
  @Builder.Default private List<ValidationWarning> warnings = new ArrayList<>();

  /**
   * Crée un résultat de validation valide (sans erreurs).
   *
   * @return Un résultat valide
   */
  public static ValidationResult valid() {
    return ValidationResult.builder().build();
  }

  /**
   * Crée un résultat de validation invalide avec une erreur.
   *
   * @param error L'erreur de validation
   * @return Un résultat invalide
   */
  public static ValidationResult invalid(ValidationError error) {
    List<ValidationError> errors = new ArrayList<>();
    errors.add(error);
    return ValidationResult.builder().errors(errors).build();
  }

  /**
   * Crée un résultat de validation invalide avec plusieurs erreurs.
   *
   * @param errors Les erreurs de validation
   * @return Un résultat invalide
   */
  public static ValidationResult invalid(List<ValidationError> errors) {
    return ValidationResult.builder().errors(new ArrayList<>(errors)).build();
  }

  /**
   * Vérifie si la validation est passée (aucune erreur).
   *
   * @return true si aucune erreur
   */
  public boolean isValid() {
    return errors == null || errors.isEmpty();
  }

  /**
   * Vérifie si des erreurs sont présentes.
   *
   * @return true si au moins une erreur
   */
  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

  /**
   * Vérifie si des avertissements sont présents.
   *
   * @return true si au moins un avertissement
   */
  public boolean hasWarnings() {
    return warnings != null && !warnings.isEmpty();
  }

  /**
   * Retourne le nombre d'erreurs.
   *
   * @return Le nombre d'erreurs
   */
  public int getErrorCount() {
    return errors == null ? 0 : errors.size();
  }

  /**
   * Retourne le nombre d'avertissements.
   *
   * @return Le nombre d'avertissements
   */
  public int getWarningCount() {
    return warnings == null ? 0 : warnings.size();
  }

  /**
   * Ajoute une erreur au résultat.
   *
   * @param error L'erreur à ajouter
   * @return this pour chaînage
   */
  public ValidationResult addError(ValidationError error) {
    if (errors == null) {
      errors = new ArrayList<>();
    }
    errors.add(error);
    return this;
  }

  /**
   * Ajoute un avertissement au résultat.
   *
   * @param warning L'avertissement à ajouter
   * @return this pour chaînage
   */
  public ValidationResult addWarning(ValidationWarning warning) {
    if (warnings == null) {
      warnings = new ArrayList<>();
    }
    warnings.add(warning);
    return this;
  }

  /**
   * Fusionne un autre résultat de validation dans celui-ci.
   *
   * @param other L'autre résultat à fusionner
   * @return this pour chaînage
   */
  public ValidationResult merge(ValidationResult other) {
    if (other == null) {
      return this;
    }
    if (other.errors != null) {
      if (this.errors == null) {
        this.errors = new ArrayList<>();
      }
      this.errors.addAll(other.errors);
    }
    if (other.warnings != null) {
      if (this.warnings == null) {
        this.warnings = new ArrayList<>();
      }
      this.warnings.addAll(other.warnings);
    }
    return this;
  }

  /**
   * Génère un message formaté avec toutes les erreurs.
   *
   * @return Le message formaté
   */
  public String getFormattedErrorMessage() {
    if (isValid()) {
      return "Validation réussie";
    }
    return errors.stream()
        .map(ValidationError::toFormattedString)
        .collect(Collectors.joining("\n"));
  }

  /**
   * Génère un rapport complet de validation.
   *
   * @return Le rapport formaté
   */
  public String getFullReport() {
    StringBuilder sb = new StringBuilder();

    if (isValid()) {
      sb.append("✓ Validation réussie");
    } else {
      sb.append("✗ Validation échouée - ").append(getErrorCount()).append(" erreur(s)");
    }

    if (hasWarnings()) {
      sb.append(" - ").append(getWarningCount()).append(" avertissement(s)");
    }

    sb.append("\n");

    if (hasErrors()) {
      sb.append("\n=== ERREURS ===\n");
      for (ValidationError error : errors) {
        sb.append("  • ").append(error.toFormattedString()).append("\n");
      }
    }

    if (hasWarnings()) {
      sb.append("\n=== AVERTISSEMENTS ===\n");
      for (ValidationWarning warning : warnings) {
        sb.append("  ⚠ ").append(warning.toFormattedString()).append("\n");
      }
    }

    return sb.toString();
  }
}
