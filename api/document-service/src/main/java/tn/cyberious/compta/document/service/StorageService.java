package tn.cyberious.compta.document.service;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import tn.cyberious.compta.document.config.S3Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

  private final S3Client s3Client;
  private final S3Properties s3Properties;

  public record UploadResult(String filePath, String checksum, long fileSize) {}

  public UploadResult uploadFile(MultipartFile file) {
    try {
      String originalFilename = file.getOriginalFilename();
      String extension = getFileExtension(originalFilename);
      String uniqueFileName = UUID.randomUUID().toString() + extension;
      String filePath = s3Properties.getFolder() + uniqueFileName;

      byte[] fileBytes = file.getBytes();
      String checksum = calculateChecksum(fileBytes);

      PutObjectRequest putRequest =
          PutObjectRequest.builder()
              .bucket(s3Properties.getBucket())
              .key(filePath)
              .contentType(file.getContentType())
              .contentLength(file.getSize())
              .build();

      s3Client.putObject(putRequest, RequestBody.fromBytes(fileBytes));

      log.info("File uploaded successfully: {}", filePath);
      return new UploadResult(filePath, checksum, file.getSize());
    } catch (Exception e) {
      log.error("Failed to upload file", e);
      throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
    }
  }

  public InputStream downloadFile(String filePath) {
    try {
      GetObjectRequest getRequest =
          GetObjectRequest.builder().bucket(s3Properties.getBucket()).key(filePath).build();

      return s3Client.getObject(getRequest);
    } catch (Exception e) {
      log.error("Failed to download file: {}", filePath, e);
      throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
    }
  }

  public void deleteFile(String filePath) {
    try {
      DeleteObjectRequest deleteRequest =
          DeleteObjectRequest.builder().bucket(s3Properties.getBucket()).key(filePath).build();

      s3Client.deleteObject(deleteRequest);
      log.info("File deleted successfully: {}", filePath);
    } catch (Exception e) {
      log.error("Failed to delete file: {}", filePath, e);
      throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
    }
  }

  public String generatePresignedUrl(String filePath, Duration expiration) {
    try (S3Presigner presigner =
        S3Presigner.builder()
            .region(software.amazon.awssdk.regions.Region.of(s3Properties.getRegion()))
            .credentialsProvider(
                software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                    software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(
                        s3Properties.getKey(), s3Properties.getSecret())))
            .endpointOverride(java.net.URI.create(s3Properties.getEndpoint()))
            .build()) {

      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(s3Properties.getBucket()).key(filePath).build();

      GetObjectPresignRequest presignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(expiration)
              .getObjectRequest(getObjectRequest)
              .build();

      return presigner.presignGetObject(presignRequest).url().toString();
    } catch (Exception e) {
      log.error("Failed to generate presigned URL for: {}", filePath, e);
      throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
    }
  }

  private String getFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf("."));
  }

  private String calculateChecksum(byte[] data) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(data);
      return HexFormat.of().formatHex(hash);
    } catch (Exception e) {
      log.error("Failed to calculate checksum", e);
      return null;
    }
  }
}
