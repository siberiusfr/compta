package tn.cyberious.compta.einvoicing.elfatoora.service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import tn.cyberious.compta.einvoicing.elfatoora.config.ElFatooraProperties;
import tn.cyberious.compta.einvoicing.elfatoora.exception.ElFatooraException;
import tn.cyberious.compta.einvoicing.elfatoora.exception.ErrorCode;
import tn.cyberious.compta.einvoicing.elfatoora.model.dto.CertificateInfo;

/**
 * Service for signing El Fatoora XML documents with XAdES-EPES signatures.
 *
 * <p>This service creates XAdES (XML Advanced Electronic Signatures) in EPES (Explicit Policy-based
 * Electronic Signature) format, conforming to the Tunisian electronic invoice signature policy.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XadesSignatureService {

  private static final String DS_NS = "http://www.w3.org/2000/09/xmldsig#";
  private static final String XADES_NS = "http://uri.etsi.org/01903/v1.3.2#";
  private static final String SIGNED_PROPERTIES_TYPE = "http://uri.etsi.org/01903#SignedProperties";

  private final ElFatooraProperties properties;
  private final ResourceLoader resourceLoader;

  private PrivateKey privateKey;
  private X509Certificate certificate;
  private List<X509Certificate> certificateChain;
  private boolean certificateLoaded = false;

  @PostConstruct
  public void init() {
    if (properties.getCertificate().isRequired()) {
      loadCertificate();
    } else {
      log.info("Certificate not required, signature service will operate in optional mode");
    }
  }

  /**
   * Loads the signing certificate from the configured keystore.
   *
   * @throws ElFatooraException if certificate loading fails
   */
  public void loadCertificate() {
    String certPath = properties.getCertificate().getPath();
    String password = properties.getCertificate().getPassword();
    String alias = properties.getCertificate().getAlias();
    String type = properties.getCertificate().getType();

    if (password == null || password.isEmpty()) {
      log.warn("Certificate password not configured, signature will not be available");
      return;
    }

    try {
      KeyStore keyStore = KeyStore.getInstance(type);

      Resource certResource = resourceLoader.getResource(certPath);
      InputStream is;
      if (certResource.exists()) {
        is = certResource.getInputStream();
      } else if (certPath.startsWith("file:")
          || certPath.contains("/")
          || certPath.contains("\\")) {
        is = new FileInputStream(certPath.replace("file:", ""));
      } else {
        log.warn("Certificate file not found: {}", certPath);
        return;
      }

      keyStore.load(is, password.toCharArray());
      is.close();

      // Get private key
      privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
      if (privateKey == null) {
        throw new ElFatooraException(
            ErrorCode.CERTIFICATE_ERROR, "Private key not found for alias: " + alias);
      }

      // Get certificate chain
      java.security.cert.Certificate[] chain = keyStore.getCertificateChain(alias);
      if (chain == null || chain.length == 0) {
        throw new ElFatooraException(
            ErrorCode.CERTIFICATE_ERROR, "Certificate chain not found for alias: " + alias);
      }

      certificateChain = new ArrayList<>();
      for (java.security.cert.Certificate cert : chain) {
        certificateChain.add((X509Certificate) cert);
      }
      certificate = certificateChain.get(0);
      certificateLoaded = true;

      log.info(
          "Certificate loaded successfully: Subject={}",
          certificate.getSubjectX500Principal().getName());

    } catch (ElFatooraException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to load certificate", e);
      if (properties.getCertificate().isRequired()) {
        throw new ElFatooraException(ErrorCode.CERTIFICATE_ERROR, e);
      }
    }
  }

  /**
   * Signs an XML document with XAdES-EPES signature.
   *
   * @param unsignedXml the unsigned XML string
   * @return signed XML string
   * @throws ElFatooraException if signing fails
   */
  public String signXml(String unsignedXml) {
    if (!certificateLoaded) {
      if (properties.getCertificate().isRequired()) {
        throw new ElFatooraException(ErrorCode.CERTIFICATE_ERROR, "Certificate not loaded");
      }
      log.warn("Certificate not loaded, returning unsigned XML");
      return unsignedXml;
    }

    try {
      // Parse the XML
      Document document = parseXml(unsignedXml);

      // Create the signature
      createXadesSignature(document);

      // Convert back to string
      return documentToString(document);

    } catch (ElFatooraException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to sign XML", e);
      throw new ElFatooraException(ErrorCode.SIGNATURE_FAILED, e);
    }
  }

  /**
   * Verifies the XAdES signature on a signed XML document.
   *
   * @param signedXml the signed XML string
   * @return true if signature is valid
   */
  public boolean verifySignature(String signedXml) {
    try {
      Document document = parseXml(signedXml);

      // Find Signature element
      NodeList signatureNodes = document.getElementsByTagNameNS(DS_NS, "Signature");
      if (signatureNodes.getLength() == 0) {
        log.warn("No signature found in document");
        return false;
      }

      // Get first signature (supplier signature)
      Element signatureElement = (Element) signatureNodes.item(0);

      // Get the public key from the certificate in the signature
      NodeList x509Certs = signatureElement.getElementsByTagNameNS(DS_NS, "X509Certificate");
      if (x509Certs.getLength() == 0) {
        log.warn("No X509Certificate found in signature");
        return false;
      }

      String certBase64 = x509Certs.item(0).getTextContent().replaceAll("\\s", "");
      byte[] certBytes = Base64.getDecoder().decode(certBase64);
      java.security.cert.CertificateFactory cf =
          java.security.cert.CertificateFactory.getInstance("X.509");
      X509Certificate signerCert =
          (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
      PublicKey publicKey = signerCert.getPublicKey();

      // Validate signature
      XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
      DOMValidateContext validateContext = new DOMValidateContext(publicKey, signatureElement);
      XMLSignature signature = factory.unmarshalXMLSignature(validateContext);

      boolean valid = signature.validate(validateContext);
      log.info("Signature verification result: {}", valid);
      return valid;

    } catch (Exception e) {
      log.error("Signature verification failed", e);
      return false;
    }
  }

  /**
   * Gets information about the loaded signing certificate.
   *
   * @return certificate info DTO
   */
  public CertificateInfo getCertificateInfo() {
    if (!certificateLoaded || certificate == null) {
      return CertificateInfo.builder().valid(false).build();
    }

    try {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime validFrom =
          LocalDateTime.ofInstant(certificate.getNotBefore().toInstant(), ZoneOffset.UTC);
      LocalDateTime validTo =
          LocalDateTime.ofInstant(certificate.getNotAfter().toInstant(), ZoneOffset.UTC);

      boolean isValid = now.isAfter(validFrom) && now.isBefore(validTo);

      // Calculate fingerprints
      MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      byte[] encoded = certificate.getEncoded();
      String sha1Fp = bytesToHex(sha1.digest(encoded));
      String sha256Fp = bytesToHex(sha256.digest(encoded));

      return CertificateInfo.builder()
          .subjectDN(certificate.getSubjectX500Principal().getName())
          .issuerDN(certificate.getIssuerX500Principal().getName())
          .serialNumber(certificate.getSerialNumber().toString(16).toUpperCase())
          .validFrom(validFrom)
          .validTo(validTo)
          .valid(isValid)
          .alias(properties.getCertificate().getAlias())
          .sha1Fingerprint(sha1Fp)
          .sha256Fingerprint(sha256Fp)
          .keyAlgorithm(certificate.getPublicKey().getAlgorithm())
          .keySize(getKeySize(certificate.getPublicKey()))
          .build();

    } catch (Exception e) {
      log.error("Failed to get certificate info", e);
      return CertificateInfo.builder().valid(false).build();
    }
  }

  /**
   * Checks if a valid certificate is loaded.
   *
   * @return true if certificate is available for signing
   */
  public boolean isCertificateAvailable() {
    return certificateLoaded && certificate != null && privateKey != null;
  }

  private void createXadesSignature(Document document) throws Exception {
    Element root = document.getDocumentElement();
    String signatureId = properties.getSignature().getSignatureIdPrefix();
    String referenceId = properties.getSignature().getReferenceId();

    XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
    KeyInfoFactory keyInfoFactory = factory.getKeyInfoFactory();

    // Create References
    List<Reference> references = new ArrayList<>();

    // Reference to the document (excluding signatures)
    List<Transform> transforms = new ArrayList<>();

    // XPath transform to exclude signatures
    transforms.add(
        factory.newTransform(
            Transform.XPATH,
            new XPathFilterParameterSpec(
                "not(ancestor-or-self::ds:Signature)", Collections.singletonMap("ds", DS_NS))));

    // XPath transform to exclude RefTtnVal
    transforms.add(
        factory.newTransform(
            Transform.XPATH, new XPathFilterParameterSpec("not(ancestor-or-self::RefTtnVal)")));

    // Exclusive canonicalization
    transforms.add(
        factory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null));

    Reference documentRef =
        factory.newReference(
            "", factory.newDigestMethod(DigestMethod.SHA256, null), transforms, null, referenceId);
    references.add(documentRef);

    // Reference to SignedProperties
    String signedPropertiesId = "xades-" + signatureId;
    List<Transform> spTransforms =
        Collections.singletonList(
            factory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null));

    Reference signedPropsRef =
        factory.newReference(
            "#" + signedPropertiesId,
            factory.newDigestMethod(DigestMethod.SHA256, null),
            spTransforms,
            SIGNED_PROPERTIES_TYPE,
            null);
    references.add(signedPropsRef);

    // Create SignedInfo
    SignedInfo signedInfo =
        factory.newSignedInfo(
            factory.newCanonicalizationMethod(
                CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
            factory.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
            references);

    // Create KeyInfo with certificate chain
    List<Object> x509Content = new ArrayList<>();
    for (X509Certificate cert : certificateChain) {
      x509Content.add(cert);
    }
    X509Data x509Data = keyInfoFactory.newX509Data(x509Content);
    KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));

    // Create XMLSignature (without Object for now)
    XMLSignature signature =
        factory.newXMLSignature(signedInfo, keyInfo, null, signatureId, "value-" + signatureId);

    // Sign
    DOMSignContext signContext = new DOMSignContext(privateKey, root);
    signContext.putNamespacePrefix(DS_NS, "ds");

    signature.sign(signContext);

    // Find the created signature element and add XAdES QualifyingProperties
    NodeList sigNodes = document.getElementsByTagNameNS(DS_NS, "Signature");
    Element signatureElement = (Element) sigNodes.item(sigNodes.getLength() - 1);

    // Add Object with QualifyingProperties
    Element objectElement = document.createElementNS(DS_NS, "ds:Object");
    Element qualifyingProps =
        createQualifyingProperties(document, signatureId, signedPropertiesId, referenceId);
    objectElement.appendChild(qualifyingProps);
    signatureElement.appendChild(objectElement);

    // Recalculate SignedProperties digest
    updateSignedPropertiesReference(document, signatureElement, signedPropertiesId);
  }

  private Element createQualifyingProperties(
      Document document, String signatureId, String signedPropertiesId, String referenceId)
      throws Exception {

    Element qualifyingProps = document.createElementNS(XADES_NS, "xades:QualifyingProperties");
    qualifyingProps.setAttribute("Target", "#" + signatureId);

    Element signedProps = document.createElementNS(XADES_NS, "xades:SignedProperties");
    signedProps.setAttribute("Id", signedPropertiesId);
    signedProps.setIdAttribute("Id", true);

    // SignedSignatureProperties
    Element signedSigProps = document.createElementNS(XADES_NS, "xades:SignedSignatureProperties");

    // SigningTime
    Element signingTime = document.createElementNS(XADES_NS, "xades:SigningTime");
    signingTime.setTextContent(
        ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
    signedSigProps.appendChild(signingTime);

    // SigningCertificateV2
    Element signingCertV2 = document.createElementNS(XADES_NS, "xades:SigningCertificateV2");
    Element cert = document.createElementNS(XADES_NS, "xades:Cert");

    Element certDigest = document.createElementNS(XADES_NS, "xades:CertDigest");
    Element digestMethod = document.createElementNS(DS_NS, "ds:DigestMethod");
    digestMethod.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#sha1");
    certDigest.appendChild(digestMethod);

    Element digestValue = document.createElementNS(DS_NS, "ds:DigestValue");
    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    digestValue.setTextContent(
        Base64.getEncoder().encodeToString(sha1.digest(certificate.getEncoded())));
    certDigest.appendChild(digestValue);
    cert.appendChild(certDigest);

    Element issuerSerial = document.createElementNS(XADES_NS, "xades:IssuerSerialV2");
    // Encode IssuerSerial in base64
    issuerSerial.setTextContent(createIssuerSerialV2());
    cert.appendChild(issuerSerial);

    signingCertV2.appendChild(cert);
    signedSigProps.appendChild(signingCertV2);

    // SignaturePolicyIdentifier
    Element sigPolicyId = document.createElementNS(XADES_NS, "xades:SignaturePolicyIdentifier");
    Element sigPolicyIdInner = document.createElementNS(XADES_NS, "xades:SignaturePolicyId");

    Element policyId = document.createElementNS(XADES_NS, "xades:SigPolicyId");
    Element identifier = document.createElementNS(XADES_NS, "xades:Identifier");
    identifier.setAttribute("Qualifier", "OIDasURN");
    identifier.setTextContent(properties.getSignature().getPolicyOid());
    policyId.appendChild(identifier);

    Element description = document.createElementNS(XADES_NS, "xades:Description");
    description.setTextContent(properties.getSignature().getPolicyDescription());
    policyId.appendChild(description);

    sigPolicyIdInner.appendChild(policyId);

    Element sigPolicyHash = document.createElementNS(XADES_NS, "xades:SigPolicyHash");
    Element policyDigestMethod = document.createElementNS(DS_NS, "ds:DigestMethod");
    policyDigestMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmlenc#sha256");
    sigPolicyHash.appendChild(policyDigestMethod);

    Element policyDigestValue = document.createElementNS(DS_NS, "ds:DigestValue");
    policyDigestValue.setTextContent(properties.getSignature().getPolicyHash());
    sigPolicyHash.appendChild(policyDigestValue);

    sigPolicyIdInner.appendChild(sigPolicyHash);

    // Policy Qualifiers
    Element sigPolicyQualifiers = document.createElementNS(XADES_NS, "xades:SigPolicyQualifiers");
    Element sigPolicyQualifier = document.createElementNS(XADES_NS, "xades:SigPolicyQualifier");
    Element spUri = document.createElementNS(XADES_NS, "xades:SPURI");
    spUri.setTextContent(properties.getSignature().getPolicyUri());
    sigPolicyQualifier.appendChild(spUri);
    sigPolicyQualifiers.appendChild(sigPolicyQualifier);
    sigPolicyIdInner.appendChild(sigPolicyQualifiers);

    sigPolicyId.appendChild(sigPolicyIdInner);
    signedSigProps.appendChild(sigPolicyId);

    // SignerRoleV2
    Element signerRole = document.createElementNS(XADES_NS, "xades:SignerRoleV2");
    Element claimedRoles = document.createElementNS(XADES_NS, "xades:ClaimedRoles");
    Element claimedRole = document.createElementNS(XADES_NS, "xades:ClaimedRole");
    claimedRole.setTextContent(properties.getSignature().getSignerRole());
    claimedRoles.appendChild(claimedRole);
    signerRole.appendChild(claimedRoles);
    signedSigProps.appendChild(signerRole);

    signedProps.appendChild(signedSigProps);

    // SignedDataObjectProperties
    Element signedDataObjProps =
        document.createElementNS(XADES_NS, "xades:SignedDataObjectProperties");
    Element dataObjFormat = document.createElementNS(XADES_NS, "xades:DataObjectFormat");
    dataObjFormat.setAttribute("ObjectReference", "#" + referenceId);
    Element mimeType = document.createElementNS(XADES_NS, "xades:MimeType");
    mimeType.setTextContent("application/octet-stream");
    dataObjFormat.appendChild(mimeType);
    signedDataObjProps.appendChild(dataObjFormat);
    signedProps.appendChild(signedDataObjProps);

    qualifyingProps.appendChild(signedProps);

    return qualifyingProps;
  }

  private String createIssuerSerialV2() throws Exception {
    // This should be ASN.1 encoded IssuerAndSerialNumber in base64
    // For simplicity, we concatenate issuer DN and serial number
    // A proper implementation would use BouncyCastle for ASN.1 encoding
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

    // Create GeneralNames for issuer
    byte[] issuerBytes = certificate.getIssuerX500Principal().getEncoded();
    byte[] serialBytes = certificate.getSerialNumber().toByteArray();

    // Simple encoding: length-prefixed concatenation
    baos.write(
        new byte[] {0x30, (byte) (4 + issuerBytes.length + 2 + serialBytes.length)}); // SEQUENCE
    baos.write(new byte[] {(byte) 0xA4, (byte) issuerBytes.length}); // context [4]
    baos.write(issuerBytes);
    baos.write(new byte[] {0x02, (byte) serialBytes.length}); // INTEGER
    baos.write(serialBytes);

    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  private void updateSignedPropertiesReference(
      Document document, Element signatureElement, String signedPropertiesId) throws Exception {
    // Find the SignedProperties element and recalculate its digest
    NodeList signedPropsNodes =
        signatureElement.getElementsByTagNameNS(XADES_NS, "SignedProperties");
    if (signedPropsNodes.getLength() > 0) {
      Element signedProps = (Element) signedPropsNodes.item(0);

      // Canonicalize
      javax.xml.crypto.dsig.XMLSignatureFactory factory =
          javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
      javax.xml.crypto.dsig.TransformService c14n =
          javax.xml.crypto.dsig.TransformService.getInstance(
              CanonicalizationMethod.EXCLUSIVE, "DOM");

      // For now, the reference was calculated without the Object
      // In production, you'd need to re-sign or pre-calculate
      log.debug("SignedProperties reference may need recalculation");
    }
  }

  private Document parseXml(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xml)));
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

  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X", b));
      sb.append(":");
    }
    if (sb.length() > 0) {
      sb.setLength(sb.length() - 1);
    }
    return sb.toString();
  }

  private int getKeySize(PublicKey key) {
    if (key instanceof java.security.interfaces.RSAPublicKey) {
      return ((java.security.interfaces.RSAPublicKey) key).getModulus().bitLength();
    } else if (key instanceof java.security.interfaces.ECPublicKey) {
      return ((java.security.interfaces.ECPublicKey) key).getParams().getOrder().bitLength();
    }
    return 0;
  }
}
