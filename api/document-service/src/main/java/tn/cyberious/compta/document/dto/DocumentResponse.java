package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Document response")
public class DocumentResponse {

  @Schema(description = "Document ID", example = "1")
  private Long id;

  @Schema(description = "Document title", example = "Facture 2024-001")
  private String title;

  @Schema(description = "Document description", example = "Facture de janvier 2024")
  private String description;

  @Schema(description = "Original file name", example = "facture_2024_001.pdf")
  private String fileName;

  @Schema(description = "File size in bytes", example = "102400")
  private Long fileSize;

  @Schema(description = "MIME type", example = "application/pdf")
  private String mimeType;

  @Schema(description = "Category ID", example = "1")
  private Long categoryId;

  @Schema(description = "Category name", example = "Comptabilité")
  private String categoryName;

  @Schema(description = "Uploaded by user ID", example = "user123")
  private String uploadedBy;

  @Schema(description = "Is document public", example = "false")
  private Boolean isPublic;

  @Schema(description = "Current version number", example = "1")
  private Integer version;

  @Schema(description = "File checksum (SHA-256)", example = "abc123...")
  private String checksum;

  @Schema(description = "Associated tags")
  private List<TagResponse> tags;

  @Schema(description = "Custom metadata")
  private Map<String, String> metadata;

  @Schema(description = "Download URL (presigned)")
  private String downloadUrl;

  @Schema(description = "Creation timestamp")
  private LocalDateTime createdAt;

  @Schema(description = "Last update timestamp")
  private LocalDateTime updatedAt;
}
