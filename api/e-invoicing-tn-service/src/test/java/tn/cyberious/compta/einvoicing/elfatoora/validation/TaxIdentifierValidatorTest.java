package tn.cyberious.compta.einvoicing.elfatoora.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;

/**
 * Tests unitaires pour TaxIdentifierValidator.
 *
 * <p>Vérifie les règles de validation des identifiants fiscaux tunisiens selon le XSD TEIF.
 */
@DisplayName("TaxIdentifierValidator")
class TaxIdentifierValidatorTest {

  private TaxIdentifierValidator validator;

  @BeforeEach
  void setUp() {
    validator = new TaxIdentifierValidator();
  }

  @Nested
  @DisplayName("Matricule Fiscal Tunisien (I-01)")
  class MatriculeFiscalTests {

    @Test
    @DisplayName("Accepte un matricule fiscal valide - exemple TTN")
    void shouldAcceptValidTaxId_TtnExample() {
      // Exemple officiel TTN
      assertThat(validator.isValidTunisianTaxId("0736202XAM000")).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Accepte des matricules fiscaux valides")
    @ValueSource(
        strings = {
          "0736202XAM000", // Exemple TTN
          "1234567ABC123", // Autre exemple valide
          "0000000AAC000", // Minimum valide
          "9999999ZPE999", // Maximum valide
          "1234567KNC500" // Catégorie N
        })
    void shouldAcceptValidTaxIds(String taxId) {
      assertThat(validator.isValid(taxId, IdentifierType.I_01)).isTrue();
    }

    @Test
    @DisplayName("Rejette un matricule avec lettre I (interdite)")
    void shouldRejectTaxIdWithLetterI() {
      // Lettre I interdite en position 8
      assertThat(validator.isValidTunisianTaxId("0736202IAM000")).isFalse();
    }

    @Test
    @DisplayName("Rejette un matricule avec lettre O (interdite)")
    void shouldRejectTaxIdWithLetterO() {
      // Lettre O interdite en position 8
      assertThat(validator.isValidTunisianTaxId("0736202OAM000")).isFalse();
    }

    @Test
    @DisplayName("Rejette un matricule avec position fiscale invalide")
    void shouldRejectTaxIdWithInvalidPosition() {
      // Position fiscale doit être A, B, D, N, P (pas X)
      assertThat(validator.isValidTunisianTaxId("0736202XXM000")).isFalse();
    }

    @Test
    @DisplayName("Rejette un matricule avec catégorie invalide")
    void shouldRejectTaxIdWithInvalidCategory() {
      // Catégorie doit être C, M, N, P, E (pas X)
      assertThat(validator.isValidTunisianTaxId("0736202XAX000")).isFalse();
    }

    @Test
    @DisplayName("Rejette un matricule trop court")
    void shouldRejectTooShortTaxId() {
      assertThat(validator.isValidTunisianTaxId("0736202XAM00")).isFalse();
    }

    @Test
    @DisplayName("Rejette un matricule trop long")
    void shouldRejectTooLongTaxId() {
      assertThat(validator.isValidTunisianTaxId("0736202XAM0000")).isFalse();
    }

    @Test
    @DisplayName("Rejette un matricule avec des lettres dans les 7 premiers caractères")
    void shouldRejectTaxIdWithLettersInDigitPart() {
      assertThat(validator.isValidTunisianTaxId("073620AXAM000")).isFalse();
    }

    @Test
    @DisplayName("Rejette un matricule null")
    void shouldRejectNullTaxId() {
      assertThat(validator.isValidTunisianTaxId(null)).isFalse();
    }

    @Test
    @DisplayName("Fournit des erreurs détaillées pour un matricule invalide")
    void shouldProvideDetailedErrors() {
      List<ValidationError> errors =
          validator.getValidationErrors(
              "0736202IAM000", IdentifierType.I_01, "supplier.taxIdentifier");

      assertThat(errors).isNotEmpty();
      assertThat(errors.get(0).getCode()).isEqualTo("ELF_INVALID_TAX_ID_LETTER");
      assertThat(errors.get(0).getField()).isEqualTo("supplier.taxIdentifier");
    }
  }

  @Nested
  @DisplayName("CIN (I-02)")
  class CINTests {

    @ParameterizedTest
    @DisplayName("Accepte des CIN valides")
    @ValueSource(strings = {"12345678", "00000000", "99999999"})
    void shouldAcceptValidCIN(String cin) {
      assertThat(validator.isValid(cin, IdentifierType.I_02)).isTrue();
    }

    @Test
    @DisplayName("Rejette un CIN trop court")
    void shouldRejectTooShortCIN() {
      assertThat(validator.isValidCIN("1234567")).isFalse();
    }

    @Test
    @DisplayName("Rejette un CIN trop long")
    void shouldRejectTooLongCIN() {
      assertThat(validator.isValidCIN("123456789")).isFalse();
    }

    @Test
    @DisplayName("Rejette un CIN avec des lettres")
    void shouldRejectCINWithLetters() {
      assertThat(validator.isValidCIN("1234567A")).isFalse();
    }
  }

  @Nested
  @DisplayName("Carte de Séjour / Passeport (I-03)")
  class PassportTests {

    @ParameterizedTest
    @DisplayName("Accepte des passeports valides")
    @ValueSource(strings = {"123456789", "000000000", "999999999"})
    void shouldAcceptValidPassport(String passport) {
      assertThat(validator.isValid(passport, IdentifierType.I_03)).isTrue();
    }

    @Test
    @DisplayName("Rejette un passeport trop court")
    void shouldRejectTooShortPassport() {
      assertThat(validator.isValidPassport("12345678")).isFalse();
    }

    @Test
    @DisplayName("Rejette un passeport trop long")
    void shouldRejectTooLongPassport() {
      assertThat(validator.isValidPassport("1234567890")).isFalse();
    }
  }

  @Nested
  @DisplayName("Autre identifiant (I-04)")
  class OtherIdentifierTests {

    @Test
    @DisplayName("Accepte n'importe quel identifiant <= 35 caractères")
    void shouldAcceptAnyIdentifier() {
      assertThat(validator.isValid("ABC123", IdentifierType.I_04)).isTrue();
      assertThat(validator.isValid("12345678901234567890123456789012345", IdentifierType.I_04))
          .isTrue();
    }

    @Test
    @DisplayName("Rejette un identifiant > 35 caractères")
    void shouldRejectTooLongIdentifier() {
      String tooLong = "A".repeat(36);
      assertThat(validator.isValid(tooLong, IdentifierType.I_04)).isFalse();
    }
  }

  @Nested
  @DisplayName("Détection automatique du type")
  class TypeDetectionTests {

    @Test
    @DisplayName("Détecte un matricule fiscal (I-01)")
    void shouldDetectMatriculeFiscal() {
      assertThat(validator.detectType("0736202XAM000")).isEqualTo(IdentifierType.I_01);
    }

    @Test
    @DisplayName("Détecte un CIN (I-02)")
    void shouldDetectCIN() {
      assertThat(validator.detectType("12345678")).isEqualTo(IdentifierType.I_02);
    }

    @Test
    @DisplayName("Détecte un passeport (I-03)")
    void shouldDetectPassport() {
      assertThat(validator.detectType("123456789")).isEqualTo(IdentifierType.I_03);
    }

    @Test
    @DisplayName("Retourne I-04 pour un identifiant non reconnu")
    void shouldReturnOtherForUnknown() {
      assertThat(validator.detectType("UNKNOWN")).isEqualTo(IdentifierType.I_04);
    }
  }
}
