# e-invoicing-tn-service - Claude Instructions

## Service Overview

This service handles Tunisian electronic invoicing (El Fatoora) generation, validation, and signing. It generates XML documents conforming to the TEIF (Tunisian Electronic Invoice Format) standard version 1.8.8.

## Key Components

### DTOs (Data Transfer Objects)
Located in `src/main/java/tn/cyberious/compta/einvoicing/elfatoora/model/dto/`:
- `ElFatooraInvoiceDTO` - Main invoice DTO
- `SupplierDTO` - Supplier information (function code I-62)
- `CustomerDTO` - Customer information (function code I-64)
- `InvoiceLineDTO` - Line items with tax calculations
- `PaymentTermsDTO` - Payment methods and bank/postal accounts

### Services
Located in `src/main/java/tn/cyberious/compta/einvoicing/elfatoora/service/`:
- `ElFatooraService` - Facade orchestrating XML generation and signing
- `ElFatooraXmlGeneratorService` - Builds XML from DTOs
- `XadesSignatureService` - XAdES-EPES signature implementation

### Configuration
- `ElFatooraProperties` - Externalized configuration for XSD, certificates, signature policy
- Certificate path and password via environment variables: `ELFATOORA_CERT_PATH`, `ELFATOORA_CERT_PASSWORD`

## Common Patterns

### Creating an Invoice
```java
ElFatooraInvoiceDTO invoice = ElFatooraInvoiceDTO.builder()
    .invoiceNumber("FAC_2024_001")
    .invoiceDate(LocalDate.now())
    .documentType(DocumentType.INVOICE)
    .supplier(supplierDTO)
    .customer(customerDTO)
    .lines(List.of(lineDTO))
    .build();

ElFatooraResult result = elFatooraService.generateInvoice(invoice);
```

### Validation
```java
ValidationResult validation = elFatooraService.validateInvoice(invoice);
if (!validation.isValid()) {
    // Handle errors in validation.getErrors()
}
```

## Important Formats

### Tunisian Tax Identifier (Matricule Fiscal)
Format: `NNNNNNNXAMZZZ`
- 7 digits + 1 letter + A/B/D/N/P + C/M/N/P + 3 digits
- Example: `0736202XAM000`
- Regex: `[0-9]{7}[ABCDEFGHJKLMNPQRSTVWXYZ][ABDNP][CMNP][0-9]{3}`

### Date Formats
- Invoice date: `ddMMyy` (e.g., 070624)
- Period: `ddMMyy-ddMMyy` (e.g., 010524-310524)

### Amount Precision
- All monetary amounts: 3 decimal places
- Rounding: HALF_UP

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/invoices/elfatoora/generate` | Generate signed invoice |
| POST | `/api/invoices/elfatoora/generate-unsigned` | Generate unsigned XML |
| POST | `/api/invoices/elfatoora/validate` | Validate invoice data |
| POST | `/api/invoices/elfatoora/verify-signature` | Verify XAdES signature |
| GET | `/api/invoices/elfatoora/certificate-info` | Get signing certificate info |

## Testing

Test data factory: `ElFatooraTestData` in test sources
- `createSampleInvoice()` - Based on official TTN example
- `createSampleSupplier()` - Tunisie TradeNet example
- `createSampleCustomer()` - Sample customer

## XSD Schema

- Located in `src/main/resources/schema/`
- `facture_INVOIC_V1.8.8_withSig.xsd` - With signature elements
- `facture_INVOIC_V1.8.8_withoutSig.xsd` - Without signature

## JAXB Generation

Classes auto-generated from XSD during Maven build:
- Output: `target/generated-sources/jaxb/`
- Package: `tn.cyberious.compta.einvoicing.elfatoora.model.generated`

Regenerate with: `mvn generate-sources`

## Security Notes

- NEVER commit certificates to Git
- Certificates stored via environment variables
- XML parsing has XXE protection enabled
- All external entity processing disabled
