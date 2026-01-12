package tn.cyberious.compta.einvoicing.elfatoora.config;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.Init;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.SAXException;

/** Configuration class for El Fatoora module. */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ElFatooraProperties.class)
public class ElFatooraConfiguration {

  private final ElFatooraProperties properties;
  private final ResourceLoader resourceLoader;

  @PostConstruct
  public void init() {
    // Initialize Apache Santuario XML Security
    if (!Init.isInitialized()) {
      Init.init();
      log.info("Apache XML Security initialized");
    }
    log.info("El Fatoora configuration loaded");
    log.info("XSD validation enabled: {}", properties.getXsd().isValidationEnabled());
    log.info("Certificate required: {}", properties.getCertificate().isRequired());
  }

  /**
   * Creates and configures the XSD Schema for validation.
   *
   * @return Schema object for validating El Fatoora XML
   */
  @Bean
  public Schema elFatooraSchema() {
    if (!properties.getXsd().isValidationEnabled()) {
      log.info("XSD validation is disabled, schema bean not created");
      return null;
    }

    try {
      Resource xsdResource = resourceLoader.getResource(properties.getXsd().getPath());
      if (!xsdResource.exists()) {
        log.warn("XSD schema not found at: {}", properties.getXsd().getPath());
        return null;
      }

      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      // Disable external entity processing for security
      schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

      try (InputStream is = xsdResource.getInputStream()) {
        Schema schema = schemaFactory.newSchema(new javax.xml.transform.stream.StreamSource(is));
        log.info(
            "El Fatoora XSD schema loaded successfully from: {}", properties.getXsd().getPath());
        return schema;
      }
    } catch (SAXException | IOException e) {
      log.error("Failed to load El Fatoora XSD schema", e);
      return null;
    }
  }

  /**
   * Creates the JAXB context for El Fatoora classes. Note: This will work once JAXB classes are
   * generated from XSD.
   *
   * @return JAXBContext for marshalling/unmarshalling
   */
  @Bean
  public JAXBContext elFatooraJaxbContext() {
    try {
      // The package name matches the JAXB plugin configuration in pom.xml
      JAXBContext context =
          JAXBContext.newInstance("tn.cyberious.compta.einvoicing.elfatoora.model.generated");
      log.info("JAXB context created for El Fatoora");
      return context;
    } catch (JAXBException e) {
      log.warn(
          "JAXB context creation failed. This is expected until JAXB classes are generated: {}",
          e.getMessage());
      return null;
    }
  }
}
