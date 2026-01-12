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
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;
import tn.cyberious.compta.einvoicing.elfatoora.testdata.ElFatooraTestData;

/** Unit tests for {@link ElFatooraXmlGeneratorService}. */
@SpringBootTest
@ActiveProfiles("test")
class ElFatooraXmlGeneratorServiceTest {

  @Autowired private ElFatooraXmlGeneratorService xmlGeneratorService;

  @Nested
  @DisplayName("XML Generation Tests")
  class XmlGenerationTests {

    @Test
    @DisplayName("Should generate valid XML from DTO")
    void shouldGenerateValidXmlFromDto() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).isNotNull();
      assertThat(xml).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"");
      assertThat(xml).contains("<TEIF");
      assertThat(xml).contains("version=\"1.8.8\"");
      assertThat(xml).contains("controlingAgency=\"TTN\"");
      assertThat(xml).contains("<InvoiceHeader>");
      assertThat(xml).contains("<InvoiceBody>");
      assertThat(xml).contains("<MessageSenderIdentifier");
      assertThat(xml).contains("<MessageRecieverIdentifier");
    }

    @Test
    @DisplayName("Should include document identifier")
    void shouldIncludeDocumentIdentifier() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setInvoiceNumber("TEST_2024_001");

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("<DocumentIdentifier>TEST_2024_001</DocumentIdentifier>");
    }

    @Test
    @DisplayName("Should include supplier information")
    void shouldIncludeSupplierInformation() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("0736202XAM000"); // Supplier tax ID
      assertThat(xml).contains("Tunisie TradeNet"); // Supplier name
      assertThat(xml).contains("functionCode=\"I-62\""); // Supplier function code
    }

    @Test
    @DisplayName("Should include customer information")
    void shouldIncludeCustomerInformation() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("0914089JAM000"); // Customer tax ID
      assertThat(xml).contains("STE FRERE ET MOSAIQUE"); // Customer name
      assertThat(xml).contains("functionCode=\"I-64\""); // Customer function code
    }

    @Test
    @DisplayName("Should include line items")
    void shouldIncludeLineItems() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("<LinSection>");
      assertThat(xml).contains("<Lin>");
      assertThat(xml).contains("<ItemCode>DDM</ItemCode>");
      assertThat(xml).contains("<ItemDescription>Dossier DDM</ItemDescription>");
    }

    @Test
    @DisplayName("Should include payment information")
    void shouldIncludePaymentInformation() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("<PytSection>");
      assertThat(xml)
          .contains("<PaymentTearmsTypeCode>I-114</PaymentTearmsTypeCode>"); // Bank transfer
      assertThat(xml).contains("<PaymentTearmsTypeCode>I-115</PaymentTearmsTypeCode>"); // Postal
    }

    @Test
    @DisplayName("Should generate XML with multiple lines")
    void shouldGenerateXmlWithMultipleLines() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createMultiLineInvoice();

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("<ItemCode>PROD001</ItemCode>");
      assertThat(xml).contains("<ItemCode>PROD002</ItemCode>");
      assertThat(xml).contains("<ItemCode>PROD003</ItemCode>");
    }
  }

  @Nested
  @DisplayName("Tax Calculation Tests")
  class TaxCalculationTests {

    @Test
    @DisplayName("Should calculate line amounts correctly")
    void shouldCalculateLineAmountsCorrectly() {
      // Given
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("TEST")
              .itemDescription("Test item")
              .unitType("UNIT")
              .quantity(new BigDecimal("10.000"))
              .unitPrice(new BigDecimal("5.000"))
              .taxRate(new BigDecimal("19"))
              .taxTypeCode(InvoiceLineDTO.TaxTypeCode.TVA)
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

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      // Line amount = 10 * 5 = 50.000
      assertThat(xml).contains("50.000");
      // Total excl tax = 50.000
      // Total tax = 50 * 0.19 = 9.500
      // Total incl tax = 50 + 9.5 = 59.500
    }

    @Test
    @DisplayName("Should calculate with 3 decimal precision")
    void shouldCalculateWithThreeDecimalPrecision() {
      // Given
      InvoiceLineDTO line =
          InvoiceLineDTO.builder()
              .lineNumber(1)
              .itemCode("PREC")
              .itemDescription("Precision test")
              .unitType("KGM")
              .quantity(new BigDecimal("1.234"))
              .unitPrice(new BigDecimal("5.678"))
              .taxRate(new BigDecimal("19"))
              .taxTypeCode(InvoiceLineDTO.TaxTypeCode.TVA)
              .build();

      ElFatooraInvoiceDTO invoice =
          ElFatooraInvoiceDTO.builder()
              .invoiceNumber("PREC_TEST_001")
              .invoiceDate(LocalDate.now())
              .documentType(ElFatooraInvoiceDTO.DocumentType.INVOICE)
              .supplier(ElFatooraTestData.createSampleSupplier())
              .customer(ElFatooraTestData.createSampleCustomer())
              .lines(List.of(line))
              .currency("TND")
              .build();

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then - amounts should have exactly 3 decimal places
      assertThat(xml).containsPattern("\\d+\\.\\d{3}");
    }
  }

  @Nested
  @DisplayName("Date Format Tests")
  class DateFormatTests {

    @Test
    @DisplayName("Should format invoice date as ddMMyy")
    void shouldFormatInvoiceDateCorrectly() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setInvoiceDate(LocalDate.of(2024, 6, 7));

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("functionCode=\"I-31\"");
      assertThat(xml).contains("format=\"ddMMyy\"");
      assertThat(xml).contains(">070624</DateText>"); // 07 Jun 2024
    }

    @Test
    @DisplayName("Should format service period as ddMMyy-ddMMyy")
    void shouldFormatServicePeriodCorrectly() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setServicePeriodStart(LocalDate.of(2024, 5, 1));
      invoice.setServicePeriodEnd(LocalDate.of(2024, 5, 31));

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("functionCode=\"I-36\"");
      assertThat(xml).contains("format=\"ddMMyy-ddMMyy\"");
      assertThat(xml).contains(">010524-310524</DateText>");
    }
  }

  @Nested
  @DisplayName("Document Type Tests")
  class DocumentTypeTests {

    @Test
    @DisplayName("Should set correct document type for invoice")
    void shouldSetCorrectDocumentTypeForInvoice() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleInvoice();
      invoice.setDocumentType(ElFatooraInvoiceDTO.DocumentType.INVOICE);

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("code=\"I-11\"");
    }

    @Test
    @DisplayName("Should set correct document type for credit note")
    void shouldSetCorrectDocumentTypeForCreditNote() {
      // Given
      ElFatooraInvoiceDTO invoice = ElFatooraTestData.createSampleCreditNote();

      // When
      String xml = xmlGeneratorService.generateXml(invoice);

      // Then
      assertThat(xml).contains("code=\"I-12\"");
    }
  }
}
