package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Search criteria for documents")
public class DocumentSearchRequest {

  @Schema(
      description = "Search query (searches in title, description, file name)",
      example = "facture")
  private String query;

  @Schema(description = "Filter by category ID", example = "1")
  private Long categoryId;

  @Schema(description = "Filter by uploader user ID", example = "user123")
  private String uploadedBy;

  @Schema(description = "Filter by tag name", example = "comptabilité")
  private String tag;
}
