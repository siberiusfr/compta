package tn.cyberious.compta.einvoicing.elfatoora.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.AddressDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CustomerDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.DocumentReferenceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.SupplierDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.TaxTypeCode;
import tn.cyberious.compta.einvoicing.elfatoora.testdata.ElFatooraTestData;

/**
 * Tests d'intégration pour {@link ElFatooraController}.
 *
 * <p>Ces tests vérifient le comportement complet des endpoints REST, incluant la sérialisation
 * JSON, la validation Bean Validation, et les réponses HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ElFatooraController Integration Tests")
class ElFatooraControllerIntegrationTest {

  private static final String BASE_URL = "/api/invoices/elfatoora";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Nested
  @DisplayName("POST /generate - Génération de facture")
  class GenerateInvoiceTests {

    @Test
    @DisplayName("Devrait générer une facture avec succès")
    void shouldGenerateInvoiceSuccessfully() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      mockMvc
          .perform(
              post(BASE_URL + "/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.invoiceNumber", is(invoice.getInvoiceNumber())))
          .andExpect(jsonPath("$.unsignedXml", notNullValue()))
          .andExpect(jsonPath("$.signedXml", notNullValue()))
          .andExpect(jsonPath("$.generatedAt", notNullValue()))
          .andExpect(jsonPath("$.xsdValidated", is(true)));
    }

    @Test
    @DisplayName("Devrait retourner 400 pour une facture sans numéro")
    void shouldReturn400ForMissingInvoiceNumber() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setInvoiceNumber(null);

      mockMvc
          .perform(
              post(BASE_URL + "/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
          .andExpect(jsonPath("$.details", hasSize(1)));
    }

    @Test
    @DisplayName("Devrait retourner 400 pour un matricule fiscal invalide")
    void shouldReturn400ForInvalidTaxIdentifier() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getSupplier().setTaxIdentifier("INVALID");

      mockMvc
          .perform(
              post(BASE_URL + "/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Devrait retourner 400 pour un taux de TVA invalide")
    void shouldReturn400ForInvalidTaxRate() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getLines().get(0).setTaxRate(new BigDecimal("15")); // 15% n'est pas valide

      mockMvc
          .perform(
              post(BASE_URL + "/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code", is("VALIDATION_ERROR"))) // Bean Validation
          .andExpect(jsonPath("$.details[0]", containsString("taxRate")));
    }

    @Test
    @DisplayName("Devrait retourner 400 pour une facture sans lignes")
    void shouldReturn400ForEmptyLines() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setLines(List.of());

      mockMvc
          .perform(
              post(BASE_URL + "/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("POST /generate-unsigned - Génération XML non signé")
  class GenerateUnsignedTests {

    @Test
    @DisplayName("Devrait générer un XML non signé")
    void shouldGenerateUnsignedXml() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      mockMvc
          .perform(
              post(BASE_URL + "/generate-unsigned")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
          .andExpect(content().string(containsString("<TEIF")))
          .andExpect(content().string(containsString(invoice.getInvoiceNumber())));
    }

    @Test
    @DisplayName("Le XML généré ne devrait pas contenir de signature")
    void shouldNotContainSignature() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      mockMvc
          .perform(
              post(BASE_URL + "/generate-unsigned")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(content().string(Matchers.not(containsString("<ds:Signature"))));
    }
  }

  @Nested
  @DisplayName("POST /validate - Validation de facture")
  class ValidateInvoiceTests {

    @Test
    @DisplayName("Devrait retourner un résultat valide pour une facture correcte")
    void shouldReturnValidResultForCorrectInvoice() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    @DisplayName("Devrait retourner 400 pour une facture avec erreurs Bean Validation")
    void shouldReturn400ForInvalidInvoice() throws Exception {
      ElFatooraInvoiceDTO invoice = createInvalidInvoice();

      // Bean Validation intercepte les erreurs avant le service
      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
          .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("Devrait valider le format CIN pour un client individuel")
    void shouldValidateCinFormatForIndividualCustomer() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      CustomerDTO customer = ElFatooraTestData.createIndividualCustomer();
      customer.setTaxIdentifier("12345678"); // CIN valide
      invoice.setCustomer(customer);

      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    @DisplayName("Devrait rejeter un CIN invalide")
    void shouldRejectInvalidCin() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      CustomerDTO customer = ElFatooraTestData.createIndividualCustomer();
      customer.setTaxIdentifier("1234"); // CIN invalide (doit être 8 chiffres)
      invoice.setCustomer(customer);

      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.errors", hasSize(Matchers.greaterThan(0))));
    }
  }

  @Nested
  @DisplayName("GET /certificate-info - Information certificat")
  class CertificateInfoTests {

    @Test
    @DisplayName("Devrait retourner les informations du certificat")
    void shouldReturnCertificateInfo() throws Exception {
      mockMvc
          .perform(get(BASE_URL + "/certificate-info"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
  }

  @Nested
  @DisplayName("POST /verify-signature - Vérification de signature")
  class VerifySignatureTests {

    @Test
    @DisplayName("Devrait retourner false pour un XML non signé")
    void shouldReturnFalseForUnsignedXml() throws Exception {
      String unsignedXml = "<TEIF><test>data</test></TEIF>";

      mockMvc
          .perform(
              post(BASE_URL + "/verify-signature")
                  .contentType(MediaType.APPLICATION_XML)
                  .content(unsignedXml))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.valid", is(false)));
    }
  }

  @Nested
  @DisplayName("Validation Bean Validation")
  class BeanValidationTests {

    @Test
    @DisplayName("Devrait valider les champs obligatoires du fournisseur")
    void shouldValidateSupplierRequiredFields() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getSupplier().setCompanyName(null);

      mockMvc
          .perform(
              post(BASE_URL + "/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("Devrait valider les champs obligatoires du client")
    void shouldValidateCustomerRequiredFields() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getCustomer().setCompanyName(null);

      mockMvc
          .perform(
              post(BASE_URL + "/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Devrait rejeter un code postal invalide via Bean Validation")
    void shouldValidatePostalCodeFormat() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getSupplier().getAddress().setPostalCode("12345"); // Doit être 4 chiffres

      // Bean Validation @Pattern rejette les codes postaux != 4 chiffres
      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
          .andExpect(jsonPath("$.details[0]", containsString("postalCode")));
    }

    @Test
    @DisplayName("Devrait valider le type de client (SMTP/SMPP)")
    void shouldValidateCustomerType() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getCustomer().setCustomerType("INVALID");

      mockMvc
          .perform(
              post(BASE_URL + "/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Tests de calculs")
  class CalculationTests {

    @Test
    @DisplayName("Devrait calculer automatiquement les totaux")
    void shouldCalculateTotalsAutomatically() throws Exception {
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("CALC")
              .itemDescription("Test calcul")
              .unitType("UNIT")
              .quantity(new BigDecimal("2.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19"))
              .taxTypeCode(TaxTypeCode.TVA)
              .build();

      ElFatooraInvoiceDTO invoice =
          ElFatooraInvoiceDTO.builder()
              .invoiceNumber("CALC_TEST_001")
              .invoiceDate(LocalDate.now())
              .documentType(ElFatooraInvoiceDTO.DocumentType.INVOICE)
              .supplier(ElFatooraTestData.createSampleSupplier())
              .customer(ElFatooraTestData.createSampleCustomer())
              .lines(List.of(line))
              .currency("TND")
              .build();

      mockMvc
          .perform(
              post(BASE_URL + "/generate-unsigned")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString("200.000"))) // Total HT: 2 × 100
          .andExpect(content().string(containsString("38.000"))); // TVA: 200 × 19%
    }
  }

  @Nested
  @DisplayName("Tests des types de documents")
  class DocumentTypeTests {

    @Test
    @DisplayName("Devrait générer une facture standard (I-11)")
    void shouldGenerateStandardInvoice() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setDocumentType(ElFatooraInvoiceDTO.DocumentType.INVOICE);

      mockMvc
          .perform(
              post(BASE_URL + "/generate-unsigned")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString("code=\"I-11\"")));
    }

    @Test
    @DisplayName("Devrait générer un avoir (I-12) avec référence facture d'origine")
    void shouldGenerateCreditNote() throws Exception {
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleCreditNote();
      // Un avoir doit référencer la facture d'origine
      invoice.setOriginalInvoiceReference(
          DocumentReferenceDTO.builder()
              .referenceType(DocumentReferenceDTO.ReferenceType.ORIGINAL_INVOICE)
              .referenceValue("FAC_2024_001")
              .build());

      mockMvc
          .perform(
              post(BASE_URL + "/generate-unsigned")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString("code=\"I-12\"")));
    }
  }

  @Nested
  @DisplayName("Tests des taux de TVA")
  class TaxRateTests {

    @Test
    @DisplayName("Devrait accepter le taux normal 19%")
    void shouldAcceptStandardRate19() throws Exception {
      ElFatooraInvoiceDTO invoice = createInvoiceWithTaxRate(new BigDecimal("19"));

      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    @DisplayName("Devrait accepter le taux réduit 13%")
    void shouldAcceptReducedRate13() throws Exception {
      ElFatooraInvoiceDTO invoice = createInvoiceWithTaxRate(new BigDecimal("13"));

      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    @DisplayName("Devrait accepter le taux super-réduit 7%")
    void shouldAcceptSuperReducedRate7() throws Exception {
      ElFatooraInvoiceDTO invoice = createInvoiceWithTaxRate(new BigDecimal("7"));

      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    @DisplayName("Devrait accepter le taux exonéré 0%")
    void shouldAcceptExemptRate0() throws Exception {
      ElFatooraInvoiceDTO invoice = createInvoiceWithTaxRate(BigDecimal.ZERO);

      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    @DisplayName("Devrait rejeter un taux non autorisé 18% via Bean Validation")
    void shouldRejectInvalidRate18() throws Exception {
      ElFatooraInvoiceDTO invoice = createInvoiceWithTaxRate(new BigDecimal("18"));

      // Bean Validation avec @ValidTaxRate rejette directement les taux invalides
      mockMvc
          .perform(
              post(BASE_URL + "/validate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invoice)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
          .andExpect(jsonPath("$.details[0]", containsString("taxRate")));
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // Méthodes utilitaires
  // ═══════════════════════════════════════════════════════════════════════════

  private ElFatooraInvoiceDTO createInvalidInvoice() {
    return ElFatooraInvoiceDTO.builder()
        .invoiceNumber("INVALID_001")
        .invoiceDate(LocalDate.now())
        .documentType(ElFatooraInvoiceDTO.DocumentType.INVOICE)
        .supplier(
            SupplierDTO.builder()
                .taxIdentifier("INVALID") // Matricule fiscal invalide
                .companyName("Test Company")
                .identifierType(IdentifierType.I_01)
                .address(
                    AddressDTO.builder().city("Tunis").postalCode("1000").country("TN").build())
                .build())
        .customer(
            CustomerDTO.builder()
                .companyName("Client Test")
                .customerType("SMTP")
                .identifierType(IdentifierType.I_01)
                .address(
                    AddressDTO.builder().city("Tunis").postalCode("1000").country("TN").build())
                .build())
        .lines(
            List.of(
                InvoiceLineDTO.builder()
                    .lineNumber(1)
                    .itemCode("TEST")
                    .unitType("UNIT")
                    .quantity(new BigDecimal("1.000"))
                    .unitPrice(new BigDecimal("100.000"))
                    .taxRate(new BigDecimal("15")) // Taux invalide
                    .taxTypeCode(TaxTypeCode.TVA)
                    .build()))
        .currency("TND")
        .build();
  }

  private ElFatooraInvoiceDTO createInvoiceWithTaxRate(BigDecimal taxRate) {
    InvoiceLineDTO line =
        InvoiceLineDTO.builder()
            .lineNumber(1)
            .itemCode("TAX_TEST")
            .itemDescription("Test taux TVA")
            .unitType("UNIT")
            .quantity(new BigDecimal("1.000"))
            .unitPrice(new BigDecimal("100.000"))
            .taxRate(taxRate)
            .taxTypeCode(TaxTypeCode.TVA)
            .build();

    return ElFatooraInvoiceDTO.builder()
        .invoiceNumber("TAX_TEST_" + taxRate)
        .invoiceDate(LocalDate.now())
        .documentType(ElFatooraInvoiceDTO.DocumentType.INVOICE)
        .supplier(ElFatooraTestData.createSampleSupplier())
        .customer(ElFatooraTestData.createSampleCustomer())
        .lines(List.of(line))
        .currency("TND")
        .build();
  }
}
