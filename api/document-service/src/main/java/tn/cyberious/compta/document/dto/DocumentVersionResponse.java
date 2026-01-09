package tn.cyberious.compta.document.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Document version response")
public class DocumentVersionResponse {

  @Schema(description = "Version ID", example = "1")
  private Long id;

  @Schema(description = "Document ID", example = "1")
  private Long documentId;

  @Schema(description = "Version number", example = "1")
  private Integer versionNumber;

  @Schema(description = "File name", example = "document_v1.pdf")
  private String fileName;

  @Schema(description = "File size in bytes", example = "102400")
  private Long fileSize;

  @Schema(description = "Uploaded by user ID", example = "user123")
  private String uploadedBy;

  @Schema(description = "Description of changes", example = "Updated formatting")
  private String changeDescription;

  @Schema(description = "File checksum (SHA-256)")
  private String checksum;

  @Schema(description = "Download URL (presigned)")
  private String downloadUrl;

  @Schema(description = "Creation timestamp")
  private LocalDateTime createdAt;
}
