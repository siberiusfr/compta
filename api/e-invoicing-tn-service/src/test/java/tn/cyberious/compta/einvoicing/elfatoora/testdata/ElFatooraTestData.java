package tn.cyberious.compta.einvoicing.elfatoora.testdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.AddressDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.BankAccountDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ContactDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CustomerDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.PaymentTermsDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.PostalAccountDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.SupplierDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.IdentifierType;
import tn.cyberious.compta.einvoicing.elfatoora.model.enums.TaxTypeCode;

/**
 * Test data factory for El Fatoora unit tests.
 *
 * <p>Provides sample invoices and related data based on the official El Fatoora example documents.
 */
public final class ElFatooraTestData {

  private ElFatooraTestData() {}

  /**
   * Creates a sample invoice based on the official El Fatoora example.
   *
   * @return sample invoice DTO
   */
  public static ElFatooraInvoiceDTO createSampleInvoice() {
    return ElFatooraInvoiceDTO.builder()
        .invoiceNumber("12016_2024")
        .invoiceDate(LocalDate.of(2024, 6, 7))
        .dueDate(LocalDate.of(2024, 7, 7))
        .servicePeriodStart(LocalDate.of(2024, 5, 1))
        .servicePeriodEnd(LocalDate.of(2024, 5, 31))
        .documentType(ElFatooraInvoiceDTO.DocumentType.INVOICE)
        .documentTypeDescription("Facture")
        .supplier(createSampleSupplier())
        .customer(createSampleCustomer())
        .lines(List.of(createSampleLine()))
        .paymentTerms(createSamplePaymentTerms())
        .currency("TND")
        .schemaVersion("1.8.8")
        .stampDuty(new BigDecimal("0.300"))
        .totalAmountInWords("DEUX DINARS ET CINQ CENT QUARANTE MILLIMES")
        .build();
  }

  /**
   * Creates a sample invoice with multiple lines and different tax rates.
   *
   * @return sample invoice with multiple lines
   */
  public static ElFatooraInvoiceDTO createMultiLineInvoice() {
    return ElFatooraInvoiceDTO.builder()
        .invoiceNumber("MULTI_2024_001")
        .invoiceDate(LocalDate.of(2024, 6, 15))
        .dueDate(LocalDate.of(2024, 7, 15))
        .documentType(ElFatooraInvoiceDTO.DocumentType.INVOICE)
        .documentTypeDescription("Facture")
        .supplier(createSampleSupplier())
        .customer(createSampleCustomer())
        .lines(
            List.of(
                createLineWithRate(
                    1,
                    "PROD001",
                    "Product A",
                    new BigDecimal("10"),
                    new BigDecimal("50.000"),
                    new BigDecimal("19")),
                createLineWithRate(
                    2,
                    "PROD002",
                    "Product B",
                    new BigDecimal("5"),
                    new BigDecimal("100.000"),
                    new BigDecimal("13")),
                createLineWithRate(
                    3,
                    "PROD003",
                    "Product C (exempt)",
                    new BigDecimal("2"),
                    new BigDecimal("200.000"),
                    BigDecimal.ZERO)))
        .paymentTerms(createSamplePaymentTerms())
        .currency("TND")
        .schemaVersion("1.8.8")
        .stampDuty(new BigDecimal("0.600"))
        .build();
  }

  /**
   * Creates a sample credit note.
   *
   * @return sample credit note DTO
   */
  public static ElFatooraInvoiceDTO createSampleCreditNote() {
    return ElFatooraInvoiceDTO.builder()
        .invoiceNumber("AV_2024_001")
        .invoiceDate(LocalDate.of(2024, 6, 20))
        .documentType(ElFatooraInvoiceDTO.DocumentType.CREDIT_NOTE)
        .documentTypeDescription("Avoir")
        .supplier(createSampleSupplier())
        .customer(createSampleCustomer())
        .lines(List.of(createSampleLine()))
        .currency("TND")
        .schemaVersion("1.8.8")
        .build();
  }

  /**
   * Creates a sample supplier (based on TTN example).
   *
   * @return sample supplier DTO
   */
  public static SupplierDTO createSampleSupplier() {
    return SupplierDTO.builder()
        .taxIdentifier("0736202XAM000")
        .companyName("Tunisie TradeNet")
        .registrationNumber("B154702000")
        .legalForm("SA")
        .identifierType(IdentifierType.I_01)
        .address(createSupplierAddress())
        .contact(createSupplierContact())
        .build();
  }

  /**
   * Creates a sample customer (based on TTN example).
   *
   * @return sample customer DTO
   */
  public static CustomerDTO createSampleCustomer() {
    return CustomerDTO.builder()
        .taxIdentifier("0914089JAM000")
        .companyName("STE FRERE ET MOSAIQUE")
        .registrationNumber("0914089J")
        .customerType("SMTP")
        .establishmentName("Salle Publique")
        .taxRegime("P")
        .vatNumber("41115530")
        .identifierType(IdentifierType.I_01)
        .address(createCustomerAddress())
        .build();
  }

  /**
   * Creates a sample customer with CIN identifier (individual).
   *
   * @return sample individual customer DTO
   */
  public static CustomerDTO createIndividualCustomer() {
    return CustomerDTO.builder()
        .taxIdentifier("12345678")
        .companyName("Mohamed Ben Ali")
        .customerType("SMPP")
        .taxRegime("NP")
        .identifierType(IdentifierType.I_02)
        .address(createCustomerAddress())
        .build();
  }

  /**
   * Creates a sample invoice line.
   *
   * @return sample line DTO
   */
  public static InvoiceLineDTO createSampleLine() {
    return InvoiceLineDTO.builder()
        .lineNumber(1)
        .itemCode("DDM")
        .itemDescription("Dossier DDM")
        .unitType("UNIT")
        .quantity(new BigDecimal("1.000"))
        .unitPrice(new BigDecimal("2.000"))
        .taxRate(new BigDecimal("19"))
        .taxTypeCode(TaxTypeCode.TVA)
        .language("fr")
        .build();
  }

  /** Creates a sample line with specified tax rate. */
  public static InvoiceLineDTO createLineWithRate(
      int lineNumber,
      String itemCode,
      String description,
      BigDecimal quantity,
      BigDecimal unitPrice,
      BigDecimal taxRate) {
    return InvoiceLineDTO.builder()
        .lineNumber(lineNumber)
        .itemCode(itemCode)
        .itemDescription(description)
        .unitType("UNIT")
        .quantity(quantity)
        .unitPrice(unitPrice)
        .taxRate(taxRate)
        .taxTypeCode(TaxTypeCode.TVA)
        .language("fr")
        .build();
  }

  /**
   * Creates sample payment terms (bank and postal).
   *
   * @return list of payment terms
   */
  public static List<PaymentTermsDTO> createSamplePaymentTerms() {
    PaymentTermsDTO bankTransfer =
        PaymentTermsDTO.builder()
            .paymentMethod(PaymentTermsDTO.PaymentMethod.BANK_TRANSFER)
            .description(
                "Les banques sont priees de payer au RIB suivant: 0410 5044 4047 0138 1036.")
            .bankAccount(createBankAccount())
            .build();

    PaymentTermsDTO postalTransfer =
        PaymentTermsDTO.builder()
            .paymentMethod(PaymentTermsDTO.PaymentMethod.POSTAL_TRANSFER)
            .description(
                "A Regler exclusivement au niveau des bureaux postaux sur presentation de la facture.")
            .postalAccount(createPostalAccount())
            .build();

    return List.of(bankTransfer, postalTransfer);
  }

  /**
   * Creates a sample bank account.
   *
   * @return sample bank account DTO
   */
  public static BankAccountDTO createBankAccount() {
    return BankAccountDTO.builder()
        .accountNumber("04105044404701381036")
        .bankCode("0410")
        .branchIdentifier("5044")
        .institutionName("Banque de Tunisie")
        .country("TN")
        .build();
  }

  /**
   * Creates a sample postal account.
   *
   * @return sample postal account DTO
   */
  public static PostalAccountDTO createPostalAccount() {
    return PostalAccountDTO.builder()
        .accountNumber("0120021241115530")
        .ownerIdentifier("1B")
        .centerCode("0760")
        .branchIdentifier("0760")
        .institutionName("La poste")
        .country("TN")
        .build();
  }

  private static AddressDTO createSupplierAddress() {
    return AddressDTO.builder()
        .addressDescription("Lotissement El Khalij Les Berges du Lac")
        .street("Rue du Lac Malaren")
        .city("Tunis")
        .postalCode("1053")
        .country("TN")
        .language("fr")
        .build();
  }

  private static AddressDTO createCustomerAddress() {
    return AddressDTO.builder()
        .addressDescription("")
        .street("")
        .city("Salle publique Tunis")
        .postalCode("1000")
        .country("TN")
        .language("fr")
        .build();
  }

  private static ContactDTO createSupplierContact() {
    return ContactDTO.builder()
        .contactIdentifier("TTN")
        .contactName("Tunisie TradeNet")
        .phone("71 861 712")
        .fax("71 861 141")
        .website("www.tradenet.com.tn")
        .build();
  }
}
