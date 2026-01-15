package tn.cyberious.compta.einvoicing.elfatoora.validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.DateFunctionCode;

/**
 * Validateur pour les dates El Fatoora.
 *
 * <p>Formats supportés:
 *
 * <ul>
 *   <li>ddMMyy - Date simple (6 caractères)
 *   <li>ddMMyyHHmm - Date avec heure (10 caractères)
 *   <li>ddMMyy-ddMMyy - Période (13 caractères)
 * </ul>
 */
@Component
public class DateValidator {

  /** Pattern pour date simple ddMMyy */
  private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{2})(\\d{2})(\\d{2})$");

  /** Pattern pour date avec heure ddMMyyHHmm */
  private static final Pattern DATETIME_PATTERN =
      Pattern.compile("^(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})$");

  /** Pattern pour période ddMMyy-ddMMyy */
  private static final Pattern PERIOD_PATTERN = Pattern.compile("^(\\d{6})-(\\d{6})$");

  /** Formatter pour conversion */
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyy");

  /**
   * Valide une date au format ddMMyy.
   *
   * @param dateStr La chaîne de date
   * @return true si le format et les valeurs sont valides
   */
  public boolean isValidDate(String dateStr) {
    if (dateStr == null || dateStr.length() != 6) {
      return false;
    }

    Matcher matcher = DATE_PATTERN.matcher(dateStr);
    if (!matcher.matches()) {
      return false;
    }

    return isValidDateComponents(
        matcher.group(1), // jour
        matcher.group(2), // mois
        matcher.group(3) // année
        );
  }

  /**
   * Valide une date avec heure au format ddMMyyHHmm.
   *
   * @param dateTimeStr La chaîne de date/heure
   * @return true si valide
   */
  public boolean isValidDateTime(String dateTimeStr) {
    if (dateTimeStr == null || dateTimeStr.length() != 10) {
      return false;
    }

    Matcher matcher = DATETIME_PATTERN.matcher(dateTimeStr);
    if (!matcher.matches()) {
      return false;
    }

    // Valider date
    if (!isValidDateComponents(matcher.group(1), matcher.group(2), matcher.group(3))) {
      return false;
    }

    // Valider heure
    int hour = Integer.parseInt(matcher.group(4));
    int minute = Integer.parseInt(matcher.group(5));

    return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
  }

  /**
   * Valide une période au format ddMMyy-ddMMyy.
   *
   * @param periodStr La chaîne de période
   * @return true si valide et date début <= date fin
   */
  public boolean isValidPeriod(String periodStr) {
    if (periodStr == null || periodStr.length() != 13) {
      return false;
    }

    Matcher matcher = PERIOD_PATTERN.matcher(periodStr);
    if (!matcher.matches()) {
      return false;
    }

    String startStr = matcher.group(1);
    String endStr = matcher.group(2);

    // Valider les deux dates
    if (!isValidDate(startStr) || !isValidDate(endStr)) {
      return false;
    }

    // Vérifier que début <= fin
    LocalDate start = parseDate(startStr);
    LocalDate end = parseDate(endStr);

    return start != null && end != null && !start.isAfter(end);
  }

  /**
   * Parse une date au format ddMMyy.
   *
   * @param dateStr La chaîne de date
   * @return La LocalDate ou null si invalide
   */
  public LocalDate parseDate(String dateStr) {
    if (!isValidDate(dateStr)) {
      return null;
    }
    try {
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  /**
   * Formate une LocalDate au format ddMMyy.
   *
   * @param date La date
   * @return La chaîne formatée
   */
  public String formatDate(LocalDate date) {
    if (date == null) {
      return null;
    }
    return date.format(DATE_FORMATTER);
  }

  /**
   * Formate une LocalDateTime au format ddMMyyHHmm.
   *
   * @param dateTime La date/heure
   * @return La chaîne formatée
   */
  public String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DateTimeFormatter.ofPattern("ddMMyyHHmm"));
  }

  /**
   * Valide la cohérence des dates d'une facture.
   *
   * @param invoiceDate Date de la facture (obligatoire)
   * @param dueDate Date d'échéance (optionnelle)
   * @param periodStart Début de période (optionnel)
   * @param periodEnd Fin de période (optionnel)
   * @return Résultat de validation
   */
  public ValidationResult validateDateCoherence(
      LocalDate invoiceDate, LocalDate dueDate, LocalDate periodStart, LocalDate periodEnd) {

    ValidationResult result = ValidationResult.valid();

    // Date facture obligatoire
    if (invoiceDate == null) {
      result.addError(
          ValidationError.of(
              "invoiceDate", "ELF_MISSING_INVOICE_DATE", "La date de facture est obligatoire"));
      return result;
    }

    // Date échéance doit être >= date facture
    if (dueDate != null && dueDate.isBefore(invoiceDate)) {
      result.addError(
          ValidationError.builder()
              .field("dueDate")
              .code("ELF_INVALID_DUE_DATE")
              .message("La date d'échéance ne peut pas être antérieure à la date de facture")
              .detail("Facture: " + formatDate(invoiceDate) + ", Échéance: " + formatDate(dueDate))
              .build());
    }

    // Cohérence de la période
    if (periodStart != null && periodEnd != null) {
      if (periodStart.isAfter(periodEnd)) {
        result.addError(
            ValidationError.builder()
                .field("servicePeriod")
                .code("ELF_INVALID_PERIOD")
                .message("La date de début de période ne peut pas être après la date de fin")
                .detail("Début: " + formatDate(periodStart) + ", Fin: " + formatDate(periodEnd))
                .build());
      }
    }

    // Si période définie partiellement
    if ((periodStart != null && periodEnd == null) || (periodStart == null && periodEnd != null)) {
      result.addWarning(
          ValidationWarning.of(
              "servicePeriod",
              "ELF_WARN_INCOMPLETE_PERIOD",
              "La période de service n'est définie que partiellement"));
    }

    return result;
  }

  /**
   * Obtient les erreurs de validation pour une date.
   *
   * @param dateStr La chaîne de date
   * @param functionCode Le code fonction de la date
   * @param fieldPath Le chemin du champ
   * @return Liste des erreurs
   */
  public List<ValidationError> getValidationErrors(
      String dateStr, DateFunctionCode functionCode, String fieldPath) {
    List<ValidationError> errors = new ArrayList<>();

    if (dateStr == null || dateStr.isBlank()) {
      if (functionCode.isRequired()) {
        errors.add(
            ValidationError.of(
                fieldPath,
                "ELF_MISSING_DATE",
                "La date " + functionCode.getDescription() + " est obligatoire"));
      }
      return errors;
    }

    DateFunctionCode.DateFormat expectedFormat = functionCode.getExpectedFormat();

    // Vérifier le format
    if (!expectedFormat.matches(dateStr)) {
      errors.add(
          ValidationError.builder()
              .field(fieldPath)
              .code("ELF_INVALID_DATE_FORMAT")
              .message("Format de date invalide pour " + functionCode.getDescription())
              .detail("Format attendu: " + expectedFormat.getPattern())
              .invalidValue(dateStr)
              .build());
      return errors;
    }

    // Valider selon le format
    switch (expectedFormat) {
      case DDMMYY -> {
        if (!isValidDate(dateStr)) {
          errors.add(
              ValidationError.builder()
                  .field(fieldPath)
                  .code("ELF_INVALID_DATE_VALUE")
                  .message("Date invalide (jour/mois incohérent)")
                  .invalidValue(dateStr)
                  .build());
        }
      }
      case DDMMYYHHMM -> {
        if (!isValidDateTime(dateStr)) {
          errors.add(
              ValidationError.builder()
                  .field(fieldPath)
                  .code("ELF_INVALID_DATETIME_VALUE")
                  .message("Date/heure invalide")
                  .invalidValue(dateStr)
                  .build());
        }
      }
      case PERIOD -> {
        if (!isValidPeriod(dateStr)) {
          errors.add(
              ValidationError.builder()
                  .field(fieldPath)
                  .code("ELF_INVALID_PERIOD_VALUE")
                  .message("Période invalide (dates incohérentes ou début > fin)")
                  .invalidValue(dateStr)
                  .build());
        }
      }
    }

    return errors;
  }

  /** Valide les composants de date (jour, mois, année). */
  private boolean isValidDateComponents(String dayStr, String monthStr, String yearStr) {
    int day = Integer.parseInt(dayStr);
    int month = Integer.parseInt(monthStr);
    int year = 2000 + Integer.parseInt(yearStr); // Suppose 20xx

    // Mois valide
    if (month < 1 || month > 12) {
      return false;
    }

    // Jours max par mois
    int maxDays =
        switch (month) {
          case 2 -> isLeapYear(year) ? 29 : 28;
          case 4, 6, 9, 11 -> 30;
          default -> 31;
        };

    return day >= 1 && day <= maxDays;
  }

  /** Vérifie si une année est bissextile. */
  private boolean isLeapYear(int year) {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
  }
}
