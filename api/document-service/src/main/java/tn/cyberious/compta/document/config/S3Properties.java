package tn.cyberious.compta.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "s3")
public class S3Properties {

  private String key;
  private String secret;
  private String endpoint;
  private String region;
  private String bucket;
  private String folder;
}
