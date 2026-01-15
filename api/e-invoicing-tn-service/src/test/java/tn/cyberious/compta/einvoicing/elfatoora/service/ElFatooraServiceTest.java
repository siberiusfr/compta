package tn.cyberious.compta.einvoicing.elfatoora.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CustomerDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraResult;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.TaxTypeCode;
import tn.cyberious.compta.einvoicing.elfatoora.testdata.ElFatooraTestData;
import tn.cyberious.compta.einvoicing.elfatoora.validation.ValidationResult;

/** Unit tests for {@link ElFatooraService}. */
@SpringBootTest
@ActiveProfiles("test")
class ElFatooraServiceTest {

  @Autowired private ElFatooraService elFatooraService;

  @Nested
  @DisplayName("Invoice Generation Tests")
  class InvoiceGenerationTests {

    @Test
    @DisplayName("Should generate invoice successfully")
    void shouldGenerateInvoiceSuccessfully() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      // When
      ElFatooraResult result = elFatooraService.generateInvoice(invoice);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getInvoiceNumber()).isEqualTo(invoice.getInvoiceNumber());
      assertThat(result.getUnsignedXml()).isNotNull();
      assertThat(result.getUnsignedXml()).contains("<TEIF");
      assertThat(result.getGeneratedAt()).isNotNull();
      assertThat(result.isXsdValidated()).isTrue();
    }

    @Test
    @DisplayName("Should generate unsigned XML")
    void shouldGenerateUnsignedXml() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      // When
      String xml = elFatooraService.generateUnsignedXml(invoice);

      // Then
      assertThat(xml).isNotNull();
      assertThat(xml).contains("<TEIF");
      assertThat(xml).doesNotContain("<ds:Signature"); // No signature
    }
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {

    @Test
    @DisplayName("Should validate valid invoice")
    void shouldValidateValidInvoice() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isTrue();
      assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation for missing invoice number")
    void shouldFailValidationForMissingInvoiceNumber() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setInvoiceNumber(null);

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getField().equals("invoiceNumber"));
    }

    @Test
    @DisplayName("Should fail validation for invalid supplier tax identifier")
    void shouldFailValidationForInvalidSupplierTaxIdentifier() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getSupplier().setTaxIdentifier("INVALID");

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getField().contains("supplier.taxIdentifier"));
    }

    @Test
    @DisplayName("Should fail validation for invalid customer type")
    void shouldFailValidationForInvalidCustomerType() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getCustomer().setCustomerType("INVALID");

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getField().contains("customer.customerType"));
    }

    @Test
    @DisplayName("Should fail validation for empty lines")
    void shouldFailValidationForEmptyLines() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setLines(List.of());

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getField().equals("lines"));
    }

    @Test
    @DisplayName("Should fail validation for invalid postal code")
    void shouldFailValidationForInvalidPostalCode() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getSupplier().getAddress().setPostalCode("12345"); // Should be 4 digits

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrors()).anyMatch(e -> e.getField().contains("postalCode"));
    }

    @Test
    @DisplayName("Should reject non-standard tax rate")
    void shouldRejectNonStandardTaxRate() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.getLines().get(0).setTaxRate(new BigDecimal("15")); // Non-standard rate

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isFalse(); // Invalid - only 0%, 7%, 13%, 19% allowed
      assertThat(result.getErrors()).anyMatch(e -> e.getField().contains("taxRate"));
    }

    @Test
    @DisplayName("Should validate CIN format for individual customer")
    void shouldValidateCinFormatForIndividualCustomer() {
      // Given
      CustomerDTO customer = ElFatooraTestData.createIndividualCustomer();
      customer.setTaxIdentifier("12345678"); // Valid CIN

      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setCustomer(customer);

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should fail validation for invalid CIN")
    void shouldFailValidationForInvalidCin() {
      // Given
      CustomerDTO customer = ElFatooraTestData.createIndividualCustomer();
      customer.setTaxIdentifier("1234"); // Invalid - must be 8 digits

      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setCustomer(customer);

      // When
      ValidationResult result = elFatooraService.validateInvoice(invoice);

      // Then
      assertThat(result.isValid()).isFalse();
    }
  }

  @Nested
  @DisplayName("Edge Cases Tests")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle invoice without payment terms")
    void shouldHandleInvoiceWithoutPaymentTerms() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setPaymentTerms(null);

      // When
      String xml = elFatooraService.generateUnsignedXml(invoice);

      // Then
      assertThat(xml).isNotNull();
      assertThat(xml).doesNotContain("<PytSection>");
    }

    @Test
    @DisplayName("Should handle invoice without service period")
    void shouldHandleInvoiceWithoutServicePeriod() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setServicePeriodStart(null);
      invoice.setServicePeriodEnd(null);

      // When
      String xml = elFatooraService.generateUnsignedXml(invoice);

      // Then
      assertThat(xml).isNotNull();
      assertThat(xml).doesNotContain("functionCode=\"I-36\"");
    }

    @Test
    @DisplayName("Should calculate totals automatically")
    void shouldCalculateTotalsAutomatically() {
      // Given
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("AUTO")
              .itemDescription("Auto calc test")
              .unitType("UNIT")
              .quantity(new BigDecimal("2.000"))
              .unitPrice(new BigDecimal("100.000"))
              .taxRate(new BigDecimal("19"))
              .taxTypeCode(TaxTypeCode.TVA)
              .build();

      ElFatooraInvoiceDTO invoice =
          ElFatooraInvoiceDTO.builder()
              .invoiceNumber("AUTO_CALC_001")
              .invoiceDate(LocalDate.now())
              .documentType(ElFatooraInvoiceDTO.DocumentType.INVOICE)
              .supplier(ElFatooraTestData.createSampleSupplier())
              .customer(ElFatooraTestData.createSampleCustomer())
              .lines(List.of(line))
              .currency("TND")
              .build();

      // When
      ElFatooraResult result = elFatooraService.generateInvoice(invoice);

      // Then
      assertThat(result.getUnsignedXml()).isNotNull();
      // Total should be calculated: 2 * 100 = 200 + 19% = 238
      assertThat(result.getUnsignedXml()).contains("200.000"); // Total excl tax
      assertThat(result.getUnsignedXml()).contains("38.000"); // Tax amount
    }
  }
}
