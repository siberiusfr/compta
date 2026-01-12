package tn.cyberious.compta.einvoicing.elfatoora.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for El Fatoora invoice generation. */
@Data
@Validated
@ConfigurationProperties(prefix = "elfatoora")
public class ElFatooraProperties {

  /** XSD validation configuration. */
  private XsdConfig xsd = new XsdConfig();

  /** Certificate configuration for signing. */
  private CertificateConfig certificate = new CertificateConfig();

  /** Signature configuration. */
  private SignatureConfig signature = new SignatureConfig();

  /** TTN (Tunisie TradeNet) integration configuration. */
  private TtnConfig ttn = new TtnConfig();

  /** XSD validation configuration. */
  @Data
  public static class XsdConfig {
    /** Path to the XSD schema file. */
    private String path = "classpath:schema/facture_INVOIC_V1.8.8_withSig.xsd";

    /** Whether to validate generated XML against XSD. */
    private boolean validationEnabled = true;

    /** Path to XSD without signature (for unsigned validation). */
    private String pathWithoutSig = "classpath:schema/facture_INVOIC_V1.8.8_withoutSig.xsd";
  }

  /** Certificate configuration for XAdES signing. */
  @Data
  public static class CertificateConfig {
    /** Path to the PKCS#12 certificate file. */
    private String path = "${ELFATOORA_CERT_PATH:classpath:certificates/test-cert.p12}";

    /** Password for the certificate file. */
    private String password;

    /** Alias of the key in the keystore. */
    private String alias = "1";

    /** Keystore type (PKCS12 or JKS). */
    private String type = "PKCS12";

    /** Whether certificate is required (false allows unsigned generation). */
    private boolean required = false;
  }

  /** XAdES signature configuration. */
  @Data
  public static class SignatureConfig {
    /** Signature algorithm. */
    private String algorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    /** Digest algorithm. */
    private String digestAlgorithm = "http://www.w3.org/2001/04/xmlenc#sha256";

    /** Canonicalization method. */
    private String canonicalization = "http://www.w3.org/2001/10/xml-exc-c14n#";

    /** Signature policy OID. */
    @NotBlank private String policyOid = "urn:2.16.788.1.2.1";

    /** Signature policy description. */
    private String policyDescription = "Politique de signature de la facture electronique";

    /** Signature policy hash (Base64 encoded). */
    private String policyHash = "3J1oMkha+OAlm9hBNCcAS+/nbKokG8Gf9N3XPipP7yg=";

    /** Signature policy URI. */
    private String policyUri =
        "http://www.tradenet.com.tn/portal/telechargerTelechargement?lien=Politique_de_Signature_de_la_facture_electronique.pdf";

    /** Signer role (CEO, CFO, etc.). */
    private String signerRole = "CEO";

    /** Signature ID prefix for supplier signature. */
    private String signatureIdPrefix = "SigFrs";

    /** Reference ID for supplier signature. */
    private String referenceId = "r-id-frs";
  }

  /** TTN integration configuration. */
  @Data
  public static class TtnConfig {
    /** Whether TTN integration is enabled. */
    private boolean enabled = false;

    /** TTN test environment URL. */
    private String testUrl = "https://test.tradenet.com.tn";

    /** TTN production environment URL. */
    private String prodUrl = "https://www.tradenet.com.tn";

    /** Connection timeout in milliseconds. */
    private int timeout = 30000;

    /** Whether to use test environment. */
    private boolean useTestEnvironment = true;
  }
}
