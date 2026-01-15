package tn.cyberious.compta.einvoicing.elfatoora.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests unitaires pour DateValidator.
 *
 * <p>Vérifie les règles de validation des dates El Fatoora.
 */
@DisplayName("DateValidator")
class DateValidatorTest {

  private DateValidator validator;

  @BeforeEach
  void setUp() {
    validator = new DateValidator();
  }

  @Nested
  @DisplayName("Format ddMMyy")
  class DateFormatTests {

    @ParameterizedTest
    @DisplayName("Accepte des dates valides")
    @ValueSource(strings = {"010125", "311225", "290224", "150624"})
    void shouldAcceptValidDates(String date) {
      assertThat(validator.isValidDate(date)).isTrue();
    }

    @Test
    @DisplayName("Rejette un jour invalide (32)")
    void shouldRejectInvalidDay() {
      assertThat(validator.isValidDate("320125")).isFalse();
    }

    @Test
    @DisplayName("Rejette un mois invalide (13)")
    void shouldRejectInvalidMonth() {
      assertThat(validator.isValidDate("011325")).isFalse();
    }

    @Test
    @DisplayName("Rejette le 30 février")
    void shouldRejectFebruary30() {
      assertThat(validator.isValidDate("300225")).isFalse();
    }

    @Test
    @DisplayName("Accepte le 29 février année bissextile")
    void shouldAcceptLeapYearFeb29() {
      // 2024 est bissextile
      assertThat(validator.isValidDate("290224")).isTrue();
    }

    @Test
    @DisplayName("Rejette le 29 février année non bissextile")
    void shouldRejectNonLeapYearFeb29() {
      // 2025 n'est pas bissextile
      assertThat(validator.isValidDate("290225")).isFalse();
    }

    @Test
    @DisplayName("Rejette un format trop court")
    void shouldRejectTooShort() {
      assertThat(validator.isValidDate("01012")).isFalse();
    }

    @Test
    @DisplayName("Rejette un format trop long")
    void shouldRejectTooLong() {
      assertThat(validator.isValidDate("0101255")).isFalse();
    }

    @Test
    @DisplayName("Rejette null")
    void shouldRejectNull() {
      assertThat(validator.isValidDate(null)).isFalse();
    }
  }

  @Nested
  @DisplayName("Format ddMMyyHHmm")
  class DateTimeFormatTests {

    @ParameterizedTest
    @DisplayName("Accepte des dates/heures valides")
    @ValueSource(strings = {"0101251230", "3112252359", "1506241000"})
    void shouldAcceptValidDateTimes(String dateTime) {
      assertThat(validator.isValidDateTime(dateTime)).isTrue();
    }

    @Test
    @DisplayName("Rejette une heure invalide (25)")
    void shouldRejectInvalidHour() {
      assertThat(validator.isValidDateTime("0101252500")).isFalse();
    }

    @Test
    @DisplayName("Rejette des minutes invalides (60)")
    void shouldRejectInvalidMinutes() {
      assertThat(validator.isValidDateTime("0101251260")).isFalse();
    }

    @Test
    @DisplayName("Accepte minuit (0000)")
    void shouldAcceptMidnight() {
      assertThat(validator.isValidDateTime("0101250000")).isTrue();
    }

    @Test
    @DisplayName("Accepte 23:59")
    void shouldAccept2359() {
      assertThat(validator.isValidDateTime("0101252359")).isTrue();
    }
  }

  @Nested
  @DisplayName("Format période ddMMyy-ddMMyy")
  class PeriodFormatTests {

    @Test
    @DisplayName("Accepte une période valide")
    void shouldAcceptValidPeriod() {
      assertThat(validator.isValidPeriod("010125-310125")).isTrue();
    }

    @Test
    @DisplayName("Accepte une période d'un jour")
    void shouldAcceptOneDayPeriod() {
      assertThat(validator.isValidPeriod("150125-150125")).isTrue();
    }

    @Test
    @DisplayName("Rejette une période avec début > fin")
    void shouldRejectInvertedPeriod() {
      assertThat(validator.isValidPeriod("310125-010125")).isFalse();
    }

    @Test
    @DisplayName("Rejette une période avec date invalide")
    void shouldRejectPeriodWithInvalidDate() {
      assertThat(validator.isValidPeriod("320125-310125")).isFalse();
    }

    @Test
    @DisplayName("Rejette un format invalide")
    void shouldRejectInvalidFormat() {
      assertThat(validator.isValidPeriod("01012025-31012025")).isFalse();
    }
  }

  @Nested
  @DisplayName("Parsing de dates")
  class DateParsingTests {

    @Test
    @DisplayName("Parse une date valide")
    void shouldParseValidDate() {
      LocalDate date = validator.parseDate("150625");
      assertThat(date).isEqualTo(LocalDate.of(2025, 6, 15));
    }

    @Test
    @DisplayName("Retourne null pour une date invalide")
    void shouldReturnNullForInvalidDate() {
      assertThat(validator.parseDate("320125")).isNull();
    }

    @Test
    @DisplayName("Formate une date correctement")
    void shouldFormatDate() {
      String formatted = validator.formatDate(LocalDate.of(2025, 1, 15));
      assertThat(formatted).isEqualTo("150125");
    }
  }

  @Nested
  @DisplayName("Cohérence des dates")
  class DateCoherenceTests {

    @Test
    @DisplayName("Accepte des dates cohérentes")
    void shouldAcceptCoherentDates() {
      ValidationResult result =
          validator.validateDateCoherence(
              LocalDate.of(2025, 1, 15), // facture
              LocalDate.of(2025, 2, 15), // échéance
              LocalDate.of(2025, 1, 1), // début période
              LocalDate.of(2025, 1, 31) // fin période
              );

      assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Rejette une date d'échéance antérieure à la facture")
    void shouldRejectDueDateBeforeInvoiceDate() {
      ValidationResult result =
          validator.validateDateCoherence(
              LocalDate.of(2025, 1, 15), // facture
              LocalDate.of(2025, 1, 10), // échéance avant facture
              null,
              null);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_INVALID_DUE_DATE"));
    }

    @Test
    @DisplayName("Rejette une période inversée")
    void shouldRejectInvertedPeriod() {
      ValidationResult result =
          validator.validateDateCoherence(
              LocalDate.of(2025, 1, 15),
              null,
              LocalDate.of(2025, 1, 31), // début après fin
              LocalDate.of(2025, 1, 1));

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_INVALID_PERIOD"));
    }

    @Test
    @DisplayName("Avertit si période partiellement définie")
    void shouldWarnPartialPeriod() {
      ValidationResult result =
          validator.validateDateCoherence(
              LocalDate.of(2025, 1, 15),
              null,
              LocalDate.of(2025, 1, 1), // début sans fin
              null);

      assertThat(result.hasWarnings()).isTrue();
      assertThat(result.getWarnings())
          .anyMatch(w -> w.getCode().equals("ELF_WARN_INCOMPLETE_PERIOD"));
    }

    @Test
    @DisplayName("Rejette si date de facture manquante")
    void shouldRejectMissingInvoiceDate() {
      ValidationResult result = validator.validateDateCoherence(null, null, null, null);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getCode().equals("ELF_MISSING_INVOICE_DATE"));
    }
  }
}
