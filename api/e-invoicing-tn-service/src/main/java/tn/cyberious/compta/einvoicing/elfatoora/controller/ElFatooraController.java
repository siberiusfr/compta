package tn.cyberious.compta.einvoicing.elfatoora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CertificateInfo;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraInvoiceDTO;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.ElFatooraResult;
import tn.cyberious.compta.einvoicing.elfatoora.service.ElFatooraService;
import tn.cyberious.compta.einvoicing.elfatoora.validation.ValidationResult;

/**
 * REST controller for El Fatoora invoice generation.
 *
 * <p>Provides endpoints for generating, validating, and verifying Tunisian electronic invoices
 * conforming to the El Fatoora standard.
 */
@Slf4j
@RestController
@RequestMapping("/api/invoices/elfatoora")
@RequiredArgsConstructor
@Tag(name = "El Fatoora", description = "Tunisian electronic invoice generation and validation")
public class ElFatooraController {

  private final ElFatooraService elFatooraService;

  @PostMapping("/generate")
  @Operation(
      summary = "Generate El Fatoora invoice",
      description =
          "Generates a complete El Fatoora XML invoice with XAdES signature "
              + "from the provided invoice data")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Invoice generated successfully",
        content = @Content(schema = @Schema(implementation = ElFatooraResult.class))),
    @ApiResponse(responseCode = "400", description = "Invalid invoice data"),
    @ApiResponse(responseCode = "500", description = "Generation failed")
  })
  public ResponseEntity<ElFatooraResult> generateInvoice(
      @Valid @RequestBody ElFatooraInvoiceDTO invoice) {
    log.info("Received request to generate El Fatoora invoice: {}", invoice.getInvoiceNumber());

    ElFatooraResult result = elFatooraService.generateInvoice(invoice);

    log.info(
        "Successfully generated El Fatoora invoice: {}, signed: {}",
        result.getInvoiceNumber(),
        result.isSigned());

    return ResponseEntity.ok(result);
  }

  @PostMapping("/generate-unsigned")
  @Operation(
      summary = "Generate unsigned El Fatoora invoice",
      description = "Generates an El Fatoora XML invoice without signature (for testing)")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Unsigned XML generated successfully",
        content = @Content(mediaType = "application/xml")),
    @ApiResponse(responseCode = "400", description = "Invalid invoice data"),
    @ApiResponse(responseCode = "500", description = "Generation failed")
  })
  public ResponseEntity<String> generateUnsignedInvoice(
      @Valid @RequestBody ElFatooraInvoiceDTO invoice) {
    log.info(
        "Received request to generate unsigned El Fatoora invoice: {}", invoice.getInvoiceNumber());

    String xml = elFatooraService.generateUnsignedXml(invoice);

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml);
  }

  @PostMapping("/validate")
  @Operation(
      summary = "Validate invoice data",
      description = "Validates invoice data without generating XML")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Validation result",
        content = @Content(schema = @Schema(implementation = ValidationResult.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request")
  })
  public ResponseEntity<ValidationResult> validateInvoice(
      @Valid @RequestBody ElFatooraInvoiceDTO invoice) {
    log.info("Received request to validate El Fatoora invoice: {}", invoice.getInvoiceNumber());

    ValidationResult result = elFatooraService.validateInvoice(invoice);

    return ResponseEntity.ok(result);
  }

  @PostMapping("/verify-signature")
  @Operation(
      summary = "Verify XML signature",
      description = "Verifies the XAdES signature on a signed El Fatoora XML document")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Signature verification result"),
    @ApiResponse(responseCode = "400", description = "Invalid XML")
  })
  public ResponseEntity<SignatureVerificationResult> verifySignature(
      @RequestBody String signedXml) {
    log.info("Received request to verify El Fatoora signature");

    boolean valid = elFatooraService.verifySignature(signedXml);

    return ResponseEntity.ok(
        new SignatureVerificationResult(
            valid, valid ? "Signature is valid" : "Signature verification failed"));
  }

  @PostMapping("/parse")
  @Operation(
      summary = "Parse El Fatoora XML",
      description = "Parses an El Fatoora XML document into a DTO")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Parsed invoice",
        content = @Content(schema = @Schema(implementation = ElFatooraInvoiceDTO.class))),
    @ApiResponse(responseCode = "400", description = "Invalid XML")
  })
  public ResponseEntity<ElFatooraInvoiceDTO> parseInvoice(@RequestBody String xml) {
    log.info("Received request to parse El Fatoora XML");

    ElFatooraInvoiceDTO invoice = elFatooraService.parseInvoiceXml(xml);

    return ResponseEntity.ok(invoice);
  }

  @GetMapping("/certificate-info")
  @Operation(
      summary = "Get certificate information",
      description = "Returns information about the currently loaded signing certificate")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Certificate information",
        content = @Content(schema = @Schema(implementation = CertificateInfo.class)))
  })
  public ResponseEntity<CertificateInfo> getCertificateInfo() {
    log.debug("Received request for certificate info");

    CertificateInfo info = elFatooraService.getCertificateInfo();

    return ResponseEntity.ok(info);
  }

  /** Simple record for signature verification result. */
  public record SignatureVerificationResult(boolean valid, String message) {}
}
