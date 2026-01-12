package tn.cyberious.compta.einvoicing.elfatoora.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import tn.cyberious.compta.einvoicing.elfatoora.config.ElFatooraProperties;
import tn.cyberious.compta.einvoicing.elfatoora.exception.ElFatooraException;
import tn.cyberious.compta.einvoicing.elfatoora.exception.ErrorCode;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.AddressDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.BankAccountDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ContactDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CustomerDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.InvoiceLineDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.PaymentTermsDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.PostalAccountDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.SupplierDTO;

/**
 * Service for generating El Fatoora XML documents from DTOs.
 *
 * <p>This service creates XML documents conforming to the Tunisian electronic invoice standard
 * (TEIF - Tunisian Electronic Invoice Format).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElFatooraXmlGeneratorService {

  private static final DateTimeFormatter DATE_FORMAT_DDMMYY = DateTimeFormatter.ofPattern("ddMMyy");
  private static final String CURRENCY_CODE_LIST = "ISO_4217";
  private static final String COUNTRY_CODE_LIST = "ISO_3166-1";
  private static final int DECIMAL_SCALE = 3;

  private final ElFatooraProperties properties;

  @Nullable private final Schema elFatooraSchema;

  /**
   * Generates an unsigned El Fatoora XML document from a DTO.
   *
   * @param invoice the invoice DTO
   * @return XML string representation of the invoice
   * @throws ElFatooraException if generation fails
   */
  public String generateXml(ElFatooraInvoiceDTO invoice) {
    try {
      // Calculate totals if not provided
      calculateTotals(invoice);

      // Build DOM document
      Document document = buildDocument(invoice);

      // Convert to string
      String xml = documentToString(document);

      log.debug("Generated El Fatoora XML for invoice: {}", invoice.getInvoiceNumber());
      return xml;
    } catch (ElFatooraException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to generate El Fatoora XML", e);
      throw new ElFatooraException(ErrorCode.MARSHALLING_ERROR, e);
    }
  }

  /**
   * Validates an XML string against the El Fatoora XSD schema.
   *
   * @param xml the XML string to validate
   * @throws ElFatooraException if validation fails
   */
  public void validateXml(String xml) {
    if (elFatooraSchema == null || !properties.getXsd().isValidationEnabled()) {
      log.debug("XSD validation skipped (disabled or schema not available)");
      return;
    }

    try {
      Validator validator = elFatooraSchema.newValidator();
      validator.validate(new javax.xml.transform.stream.StreamSource(new StringReader(xml)));
      log.debug("XML validated successfully against XSD");
    } catch (Exception e) {
      log.error("XSD validation failed", e);
      throw new ElFatooraException(ErrorCode.XSD_VALIDATION_FAILED, e.getMessage());
    }
  }

  /**
   * Parses an El Fatoora XML document into a DTO.
   *
   * @param xml the XML string to parse
   * @return parsed invoice DTO
   * @throws ElFatooraException if parsing fails
   */
  public ElFatooraInvoiceDTO parseXml(String xml) {
    try {
      DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xml)));

      return parseDocument(document);
    } catch (Exception e) {
      log.error("Failed to parse El Fatoora XML", e);
      throw new ElFatooraException(ErrorCode.XML_PARSING_ERROR, e);
    }
  }

  private Document buildDocument(ElFatooraInvoiceDTO invoice) throws Exception {
    DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.newDocument();
    document.setXmlStandalone(true);

    // Root element: TEIF
    Element root = document.createElement("TEIF");
    root.setAttribute("version", invoice.getSchemaVersion());
    root.setAttribute("controlingAgency", "TTN");
    document.appendChild(root);

    // InvoiceHeader
    Element header = buildInvoiceHeader(document, invoice);
    root.appendChild(header);

    // InvoiceBody
    Element body = buildInvoiceBody(document, invoice);
    root.appendChild(body);

    return document;
  }

  private Element buildInvoiceHeader(Document document, ElFatooraInvoiceDTO invoice) {
    Element header = document.createElement("InvoiceHeader");

    // MessageSenderIdentifier (supplier)
    Element sender = document.createElement("MessageSenderIdentifier");
    sender.setAttribute("type", invoice.getSupplier().getIdentifierType().getCode());
    sender.setTextContent(invoice.getSupplier().getTaxIdentifier());
    header.appendChild(sender);

    // MessageRecieverIdentifier (customer)
    Element receiver = document.createElement("MessageRecieverIdentifier");
    receiver.setAttribute("type", invoice.getCustomer().getIdentifierType().getCode());
    receiver.setTextContent(invoice.getCustomer().getTaxIdentifier());
    header.appendChild(receiver);

    return header;
  }

  private Element buildInvoiceBody(Document document, ElFatooraInvoiceDTO invoice) {
    Element body = document.createElement("InvoiceBody");

    // Bgm (Document identification)
    body.appendChild(buildBgm(document, invoice));

    // Dtm (Dates)
    body.appendChild(buildDtm(document, invoice));

    // PartnerSection
    body.appendChild(buildPartnerSection(document, invoice));

    // PytSection (Payment)
    if (invoice.getPaymentTerms() != null && !invoice.getPaymentTerms().isEmpty()) {
      body.appendChild(buildPytSection(document, invoice));
    }

    // LinSection (Line items)
    body.appendChild(buildLinSection(document, invoice));

    // InvoiceMoa (Amounts)
    body.appendChild(buildInvoiceMoa(document, invoice));

    // InvoiceTax (Tax details)
    body.appendChild(buildInvoiceTax(document, invoice));

    return body;
  }

  private Element buildBgm(Document document, ElFatooraInvoiceDTO invoice) {
    Element bgm = document.createElement("Bgm");

    Element docId = document.createElement("DocumentIdentifier");
    docId.setTextContent(invoice.getInvoiceNumber());
    bgm.appendChild(docId);

    Element docType = document.createElement("DocumentType");
    docType.setAttribute("code", invoice.getDocumentType().getCode());
    docType.setTextContent(
        invoice.getDocumentTypeDescription() != null
            ? invoice.getDocumentTypeDescription()
            : invoice.getDocumentType().getDescription());
    bgm.appendChild(docType);

    return bgm;
  }

  private Element buildDtm(Document document, ElFatooraInvoiceDTO invoice) {
    Element dtm = document.createElement("Dtm");

    // Invoice date (I-31)
    Element invoiceDate = createDateText(document, invoice.getInvoiceDate(), "I-31", "ddMMyy");
    dtm.appendChild(invoiceDate);

    // Service period (I-36) - if provided
    if (invoice.getServicePeriodStart() != null && invoice.getServicePeriodEnd() != null) {
      Element period = document.createElement("DateText");
      period.setAttribute("functionCode", "I-36");
      period.setAttribute("format", "ddMMyy-ddMMyy");
      period.setTextContent(
          invoice.getServicePeriodStart().format(DATE_FORMAT_DDMMYY)
              + "-"
              + invoice.getServicePeriodEnd().format(DATE_FORMAT_DDMMYY));
      dtm.appendChild(period);
    }

    // Due date (I-32) - if provided
    if (invoice.getDueDate() != null) {
      Element dueDate = createDateText(document, invoice.getDueDate(), "I-32", "ddMMyy");
      dtm.appendChild(dueDate);
    }

    return dtm;
  }

  private Element createDateText(
      Document document, LocalDate date, String functionCode, String format) {
    Element dateText = document.createElement("DateText");
    dateText.setAttribute("functionCode", functionCode);
    dateText.setAttribute("format", format);
    dateText.setTextContent(date.format(DATE_FORMAT_DDMMYY));
    return dateText;
  }

  private Element buildPartnerSection(Document document, ElFatooraInvoiceDTO invoice) {
    Element partnerSection = document.createElement("PartnerSection");

    // Supplier (I-62)
    Element supplierDetails = buildPartnerDetails(document, invoice.getSupplier(), "I-62");
    partnerSection.appendChild(supplierDetails);

    // Customer (I-64)
    Element customerDetails = buildCustomerDetails(document, invoice.getCustomer());
    partnerSection.appendChild(customerDetails);

    return partnerSection;
  }

  private Element buildPartnerDetails(
      Document document, SupplierDTO supplier, String functionCode) {
    Element details = document.createElement("PartnerDetails");
    details.setAttribute("functionCode", functionCode);

    Element nad = document.createElement("Nad");

    // PartnerIdentifier
    Element partnerId = document.createElement("PartnerIdentifier");
    partnerId.setAttribute("type", supplier.getIdentifierType().getCode());
    partnerId.setTextContent(supplier.getTaxIdentifier());
    nad.appendChild(partnerId);

    // PartnerName
    Element name = document.createElement("PartnerName");
    name.setAttribute("nameType", "Qualification");
    name.setTextContent(supplier.getCompanyName());
    nad.appendChild(name);

    // PartnerAdresses
    if (supplier.getAddress() != null) {
      nad.appendChild(buildAddress(document, supplier.getAddress()));
    }

    details.appendChild(nad);

    // RffSection for registration number (I-815)
    if (supplier.getRegistrationNumber() != null) {
      details.appendChild(buildReference(document, "I-815", supplier.getRegistrationNumber()));
    }

    // RffSection for legal form (I-816)
    if (supplier.getLegalForm() != null) {
      details.appendChild(buildReference(document, "I-816", supplier.getLegalForm()));
    }

    // CtaSection for contact
    if (supplier.getContact() != null) {
      appendContactSections(document, details, supplier.getContact());
    }

    return details;
  }

  private Element buildCustomerDetails(Document document, CustomerDTO customer) {
    Element details = document.createElement("PartnerDetails");
    details.setAttribute("functionCode", "I-64");

    Element nad = document.createElement("Nad");

    // PartnerIdentifier
    Element partnerId = document.createElement("PartnerIdentifier");
    partnerId.setAttribute("type", customer.getIdentifierType().getCode());
    partnerId.setTextContent(customer.getTaxIdentifier());
    nad.appendChild(partnerId);

    // PartnerName
    Element name = document.createElement("PartnerName");
    name.setAttribute("nameType", "Qualification");
    name.setTextContent(customer.getCompanyName());
    nad.appendChild(name);

    // PartnerAdresses
    if (customer.getAddress() != null) {
      nad.appendChild(buildAddress(document, customer.getAddress()));
    }

    details.appendChild(nad);

    // RffSection for registration (I-81)
    if (customer.getRegistrationNumber() != null) {
      details.appendChild(buildReference(document, "I-81", customer.getRegistrationNumber()));
    }

    // RffSection for customer type (I-811)
    if (customer.getCustomerType() != null) {
      details.appendChild(buildReference(document, "I-811", customer.getCustomerType()));
    }

    // RffSection for establishment name (I-813)
    if (customer.getEstablishmentName() != null) {
      details.appendChild(buildReference(document, "I-813", customer.getEstablishmentName()));
    }

    // RffSection for tax regime (I-812)
    if (customer.getTaxRegime() != null) {
      details.appendChild(buildReference(document, "I-812", customer.getTaxRegime()));
    }

    // RffSection for VAT number (I-814)
    if (customer.getVatNumber() != null) {
      details.appendChild(buildReference(document, "I-814", customer.getVatNumber()));
    }

    return details;
  }

  private Element buildAddress(Document document, AddressDTO address) {
    Element adresses = document.createElement("PartnerAdresses");
    adresses.setAttribute("lang", address.getLanguage() != null ? address.getLanguage() : "fr");

    Element addrDesc = document.createElement("AdressDescription");
    addrDesc.setTextContent(
        address.getAddressDescription() != null ? address.getAddressDescription() : "");
    adresses.appendChild(addrDesc);

    Element street = document.createElement("Street");
    street.setTextContent(address.getStreet() != null ? address.getStreet() : "");
    adresses.appendChild(street);

    Element city = document.createElement("CityName");
    city.setTextContent(address.getCity() != null ? address.getCity() : "");
    adresses.appendChild(city);

    Element postal = document.createElement("PostalCode");
    postal.setTextContent(address.getPostalCode() != null ? address.getPostalCode() : "");
    adresses.appendChild(postal);

    Element country = document.createElement("Country");
    country.setAttribute("codeList", COUNTRY_CODE_LIST);
    country.setTextContent(address.getCountry() != null ? address.getCountry() : "TN");
    adresses.appendChild(country);

    return adresses;
  }

  private Element buildReference(Document document, String refId, String value) {
    Element rffSection = document.createElement("RffSection");
    Element reference = document.createElement("Reference");
    reference.setAttribute("refID", refId);
    reference.setTextContent(value);
    rffSection.appendChild(reference);
    return rffSection;
  }

  private void appendContactSections(Document document, Element parent, ContactDTO contact) {
    if (contact.getPhone() != null) {
      parent.appendChild(buildCtaSection(document, contact, "I-101", contact.getPhone()));
    }
    if (contact.getFax() != null) {
      parent.appendChild(buildCtaSection(document, contact, "I-102", contact.getFax()));
    }
    if (contact.getEmail() != null) {
      parent.appendChild(buildCtaSection(document, contact, "I-103", contact.getEmail()));
    }
    if (contact.getWebsite() != null) {
      parent.appendChild(buildCtaSection(document, contact, "I-104", contact.getWebsite()));
    }
  }

  private Element buildCtaSection(
      Document document, ContactDTO contact, String comType, String address) {
    Element ctaSection = document.createElement("CtaSection");

    Element contactElem = document.createElement("Contact");
    contactElem.setAttribute("functionCode", "I-94");

    Element contactId = document.createElement("ContactIdentifier");
    contactId.setTextContent(
        contact.getContactIdentifier() != null ? contact.getContactIdentifier() : "");
    contactElem.appendChild(contactId);

    Element contactName = document.createElement("ContactName");
    contactName.setTextContent(contact.getContactName() != null ? contact.getContactName() : "");
    contactElem.appendChild(contactName);

    ctaSection.appendChild(contactElem);

    Element communication = document.createElement("Communication");
    Element comMeans = document.createElement("ComMeansType");
    comMeans.setTextContent(comType);
    communication.appendChild(comMeans);

    Element comAddress = document.createElement("ComAdress");
    comAddress.setTextContent(address);
    communication.appendChild(comAddress);

    ctaSection.appendChild(communication);

    return ctaSection;
  }

  private Element buildPytSection(Document document, ElFatooraInvoiceDTO invoice) {
    Element pytSection = document.createElement("PytSection");

    for (PaymentTermsDTO payment : invoice.getPaymentTerms()) {
      Element pytSectionDetails = document.createElement("PytSectionDetails");

      // Pyt element
      Element pyt = document.createElement("Pyt");
      Element paymentTypeCode = document.createElement("PaymentTearmsTypeCode");
      paymentTypeCode.setTextContent(payment.getPaymentMethod().getCode());
      pyt.appendChild(paymentTypeCode);

      if (payment.getDescription() != null) {
        Element paymentDesc = document.createElement("PaymentTearmsDescription");
        paymentDesc.setTextContent(payment.getDescription());
        pyt.appendChild(paymentDesc);
      }

      pytSectionDetails.appendChild(pyt);

      // PytFii for bank/postal account
      if (payment.getBankAccount() != null) {
        pytSectionDetails.appendChild(buildPytFii(document, payment.getBankAccount(), "I-141"));
      }
      if (payment.getPostalAccount() != null) {
        pytSectionDetails.appendChild(buildPytFiiPostal(document, payment.getPostalAccount()));
      }

      pytSection.appendChild(pytSectionDetails);
    }

    return pytSection;
  }

  private Element buildPytFii(Document document, BankAccountDTO bank, String functionCode) {
    Element pytFii = document.createElement("PytFii");
    pytFii.setAttribute("functionCode", functionCode);

    Element accountHolder = document.createElement("AccountHolder");
    Element accountNumber = document.createElement("AccountNumber");
    accountNumber.setTextContent(bank.getAccountNumber());
    accountHolder.appendChild(accountNumber);

    if (bank.getOwnerIdentifier() != null) {
      Element ownerId = document.createElement("OwnerIdentifier");
      ownerId.setTextContent(bank.getOwnerIdentifier());
      accountHolder.appendChild(ownerId);
    }
    pytFii.appendChild(accountHolder);

    if (bank.getBankCode() != null || bank.getInstitutionName() != null) {
      Element instId = document.createElement("InstitutionIdentification");
      instId.setAttribute("nameCode", bank.getBankCode() != null ? bank.getBankCode() : "");

      if (bank.getBranchIdentifier() != null) {
        Element branchId = document.createElement("BranchIdentifier");
        branchId.setTextContent(bank.getBranchIdentifier());
        instId.appendChild(branchId);
      }

      if (bank.getInstitutionName() != null) {
        Element instName = document.createElement("InstitutionName");
        instName.setTextContent(bank.getInstitutionName());
        instId.appendChild(instName);
      }

      pytFii.appendChild(instId);
    }

    return pytFii;
  }

  private Element buildPytFiiPostal(Document document, PostalAccountDTO postal) {
    Element pytFii = document.createElement("PytFii");
    pytFii.setAttribute("functionCode", "I-141");

    Element accountHolder = document.createElement("AccountHolder");
    Element accountNumber = document.createElement("AccountNumber");
    accountNumber.setTextContent(postal.getAccountNumber());
    accountHolder.appendChild(accountNumber);

    if (postal.getOwnerIdentifier() != null) {
      Element ownerId = document.createElement("OwnerIdentifier");
      ownerId.setTextContent(postal.getOwnerIdentifier());
      accountHolder.appendChild(ownerId);
    }
    pytFii.appendChild(accountHolder);

    Element instId = document.createElement("InstitutionIdentification");
    instId.setAttribute("nameCode", postal.getCenterCode() != null ? postal.getCenterCode() : "");

    if (postal.getBranchIdentifier() != null) {
      Element branchId = document.createElement("BranchIdentifier");
      branchId.setTextContent(postal.getBranchIdentifier());
      instId.appendChild(branchId);
    }

    Element instName = document.createElement("InstitutionName");
    instName.setTextContent(postal.getInstitutionName());
    instId.appendChild(instName);

    pytFii.appendChild(instId);

    return pytFii;
  }

  private Element buildLinSection(Document document, ElFatooraInvoiceDTO invoice) {
    Element linSection = document.createElement("LinSection");

    for (InvoiceLineDTO line : invoice.getLines()) {
      Element lin = document.createElement("Lin");

      // ItemIdentifier
      Element itemId = document.createElement("ItemIdentifier");
      itemId.setTextContent(String.valueOf(line.getLineNumber()));
      lin.appendChild(itemId);

      // LinImd
      Element linImd = document.createElement("LinImd");
      linImd.setAttribute("lang", line.getLanguage() != null ? line.getLanguage() : "fr");

      Element itemCode = document.createElement("ItemCode");
      itemCode.setTextContent(line.getItemCode());
      linImd.appendChild(itemCode);

      Element itemDesc = document.createElement("ItemDescription");
      itemDesc.setTextContent(line.getItemDescription() != null ? line.getItemDescription() : "");
      linImd.appendChild(itemDesc);

      lin.appendChild(linImd);

      // LinQty
      Element linQty = document.createElement("LinQty");
      Element quantity = document.createElement("Quantity");
      quantity.setAttribute("measurementUnit", line.getUnitType());
      quantity.setTextContent(formatDecimal(line.getQuantity()));
      linQty.appendChild(quantity);
      lin.appendChild(linQty);

      // LinTax
      Element linTax = document.createElement("LinTax");
      Element taxTypeName = document.createElement("TaxTypeName");
      taxTypeName.setAttribute("code", line.getTaxTypeCode().getCode());
      taxTypeName.setTextContent(line.getTaxTypeCode().getDescription());
      linTax.appendChild(taxTypeName);

      Element taxDetails = document.createElement("TaxDetails");
      Element taxRate = document.createElement("TaxRate");
      taxRate.setTextContent(line.getTaxRate().stripTrailingZeros().toPlainString());
      taxDetails.appendChild(taxRate);
      linTax.appendChild(taxDetails);

      lin.appendChild(linTax);

      // LinMoa
      Element linMoa = document.createElement("LinMoa");

      // Unit price (I-183)
      linMoa.appendChild(
          buildMoaDetails(document, "I-183", line.getUnitPrice(), invoice.getCurrency()));

      // Line amount excl tax (I-171)
      linMoa.appendChild(
          buildMoaDetails(document, "I-171", line.getLineAmountExclTax(), invoice.getCurrency()));

      lin.appendChild(linMoa);

      linSection.appendChild(lin);
    }

    return linSection;
  }

  private Element buildMoaDetails(
      Document document, String amountTypeCode, BigDecimal amount, String currency) {
    Element moaDetails = document.createElement("MoaDetails");
    Element moa = document.createElement("Moa");
    moa.setAttribute("amountTypeCode", amountTypeCode);
    moa.setAttribute("currencyCodeList", CURRENCY_CODE_LIST);

    Element amountElem = document.createElement("Amount");
    amountElem.setAttribute("currencyIdentifier", currency);
    amountElem.setTextContent(formatDecimal(amount));
    moa.appendChild(amountElem);

    moaDetails.appendChild(moa);
    return moaDetails;
  }

  private Element buildInvoiceMoa(Document document, ElFatooraInvoiceDTO invoice) {
    Element invoiceMoa = document.createElement("InvoiceMoa");

    // Total base amount (I-179) - same as total excluding tax for simplicity
    invoiceMoa.appendChild(
        buildAmountDetails(
            document, "I-179", invoice.getTotalExcludingTax(), invoice.getCurrency(), null));

    // Total including tax (I-180)
    invoiceMoa.appendChild(
        buildAmountDetails(
            document,
            "I-180",
            invoice.getTotalIncludingTax(),
            invoice.getCurrency(),
            invoice.getTotalAmountInWords()));

    // Total excluding tax (I-176)
    invoiceMoa.appendChild(
        buildAmountDetails(
            document, "I-176", invoice.getTotalExcludingTax(), invoice.getCurrency(), null));

    // Tax base amount (I-182)
    invoiceMoa.appendChild(
        buildAmountDetails(
            document, "I-182", invoice.getTotalExcludingTax(), invoice.getCurrency(), null));

    // Total tax (I-181)
    invoiceMoa.appendChild(
        buildAmountDetails(document, "I-181", invoice.getTotalTax(), invoice.getCurrency(), null));

    return invoiceMoa;
  }

  private Element buildAmountDetails(
      Document document,
      String amountTypeCode,
      BigDecimal amount,
      String currency,
      String description) {
    Element amountDetails = document.createElement("AmountDetails");
    Element moa = document.createElement("Moa");
    moa.setAttribute("amountTypeCode", amountTypeCode);
    moa.setAttribute("currencyCodeList", CURRENCY_CODE_LIST);

    Element amountElem = document.createElement("Amount");
    amountElem.setAttribute("currencyIdentifier", currency);
    amountElem.setTextContent(formatDecimal(amount));
    moa.appendChild(amountElem);

    if (description != null && !description.isEmpty()) {
      Element amountDesc = document.createElement("AmountDescription");
      amountDesc.setAttribute("lang", "fr");
      amountDesc.setTextContent(description);
      moa.appendChild(amountDesc);
    }

    amountDetails.appendChild(moa);
    return amountDetails;
  }

  private Element buildInvoiceTax(Document document, ElFatooraInvoiceDTO invoice) {
    Element invoiceTax = document.createElement("InvoiceTax");

    // Group lines by tax rate
    Map<BigDecimal, BigDecimal[]> taxByRate = new HashMap<>();
    for (InvoiceLineDTO line : invoice.getLines()) {
      BigDecimal rate = line.getTaxRate();
      BigDecimal[] values =
          taxByRate.computeIfAbsent(rate, k -> new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO});
      values[0] = values[0].add(line.getLineAmountExclTax()); // Base
      BigDecimal taxAmount =
          line.getLineAmountExclTax()
              .multiply(rate)
              .divide(BigDecimal.valueOf(100), DECIMAL_SCALE, RoundingMode.HALF_UP);
      values[1] = values[1].add(taxAmount); // Tax
    }

    // Stamp duty if applicable
    if (invoice.getStampDuty() != null && invoice.getStampDuty().compareTo(BigDecimal.ZERO) > 0) {
      Element stampTaxDetails = document.createElement("InvoiceTaxDetails");

      Element stampTax = document.createElement("Tax");
      Element stampTaxName = document.createElement("TaxTypeName");
      stampTaxName.setAttribute("code", "I-1601");
      stampTaxName.setTextContent("droit de timbre");
      stampTax.appendChild(stampTaxName);

      Element stampTaxDetails2 = document.createElement("TaxDetails");
      Element stampTaxRate = document.createElement("TaxRate");
      stampTaxRate.setTextContent("0");
      stampTaxDetails2.appendChild(stampTaxRate);
      stampTax.appendChild(stampTaxDetails2);

      stampTaxDetails.appendChild(stampTax);

      // Tax amount (I-178)
      stampTaxDetails.appendChild(
          buildAmountDetails(
              document, "I-178", invoice.getStampDuty(), invoice.getCurrency(), null));

      invoiceTax.appendChild(stampTaxDetails);
    }

    // VAT by rate
    for (Map.Entry<BigDecimal, BigDecimal[]> entry : taxByRate.entrySet()) {
      Element taxDetails = document.createElement("InvoiceTaxDetails");

      Element tax = document.createElement("Tax");
      Element taxTypeName = document.createElement("TaxTypeName");
      taxTypeName.setAttribute("code", "I-1602");
      taxTypeName.setTextContent("TVA");
      tax.appendChild(taxTypeName);

      Element taxDetails2 = document.createElement("TaxDetails");
      Element taxRate = document.createElement("TaxRate");
      taxRate.setTextContent(entry.getKey().stripTrailingZeros().toPlainString());
      taxDetails2.appendChild(taxRate);
      tax.appendChild(taxDetails2);

      taxDetails.appendChild(tax);

      // Base amount (I-177)
      taxDetails.appendChild(
          buildAmountDetails(document, "I-177", entry.getValue()[0], invoice.getCurrency(), null));

      // Tax amount (I-178)
      taxDetails.appendChild(
          buildAmountDetails(document, "I-178", entry.getValue()[1], invoice.getCurrency(), null));

      invoiceTax.appendChild(taxDetails);
    }

    return invoiceTax;
  }

  private void calculateTotals(ElFatooraInvoiceDTO invoice) {
    BigDecimal totalExclTax = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;

    for (InvoiceLineDTO line : invoice.getLines()) {
      // Calculate line amount if not provided
      if (line.getLineAmountExclTax() == null) {
        BigDecimal lineAmount =
            line.getQuantity()
                .multiply(line.getUnitPrice())
                .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
        line.setLineAmountExclTax(lineAmount);
      }

      // Calculate tax amount for line
      BigDecimal lineTax =
          line.getLineAmountExclTax()
              .multiply(line.getTaxRate())
              .divide(BigDecimal.valueOf(100), DECIMAL_SCALE, RoundingMode.HALF_UP);

      // Calculate line amount including tax
      if (line.getLineAmountInclTax() == null) {
        line.setLineAmountInclTax(line.getLineAmountExclTax().add(lineTax));
      }

      totalExclTax = totalExclTax.add(line.getLineAmountExclTax());
      totalTax = totalTax.add(lineTax);
    }

    if (invoice.getTotalExcludingTax() == null) {
      invoice.setTotalExcludingTax(totalExclTax.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP));
    }

    if (invoice.getTotalTax() == null) {
      invoice.setTotalTax(totalTax.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP));
    }

    if (invoice.getTotalIncludingTax() == null) {
      BigDecimal total = invoice.getTotalExcludingTax().add(invoice.getTotalTax());
      if (invoice.getStampDuty() != null) {
        total = total.add(invoice.getStampDuty());
      }
      invoice.setTotalIncludingTax(total.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP));
    }
  }

  private String formatDecimal(BigDecimal value) {
    if (value == null) {
      return "0.000";
    }
    return value.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP).toPlainString();
  }

  private String documentToString(Document document) throws Exception {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(document), new StreamResult(writer));
    return writer.toString();
  }

  private DocumentBuilderFactory createSecureDocumentBuilderFactory() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    // Security features
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    return factory;
  }

  private ElFatooraInvoiceDTO parseDocument(Document document) {
    // Basic parsing implementation - can be extended as needed
    Element root = document.getDocumentElement();

    ElFatooraInvoiceDTO.ElFatooraInvoiceDTOBuilder builder = ElFatooraInvoiceDTO.builder();
    builder.schemaVersion(root.getAttribute("version"));

    // Parse header
    Element header = (Element) root.getElementsByTagName("InvoiceHeader").item(0);
    if (header != null) {
      // Extract supplier and customer identifiers from header
      // Full implementation would parse all elements
    }

    // Parse body
    Element body = (Element) root.getElementsByTagName("InvoiceBody").item(0);
    if (body != null) {
      // Parse Bgm
      Element bgm = (Element) body.getElementsByTagName("Bgm").item(0);
      if (bgm != null) {
        Element docId = (Element) bgm.getElementsByTagName("DocumentIdentifier").item(0);
        if (docId != null) {
          builder.invoiceNumber(docId.getTextContent());
        }
      }
    }

    return builder.build();
  }
}
