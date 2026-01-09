package tn.cyberious.compta.document.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request for uploading a document")
public class DocumentUploadRequest {

  @NotBlank(message = "Title is required")
  @Size(max = 255, message = "Title must not exceed 255 characters")
  @Schema(description = "Document title", example = "Facture 2024-001")
  private String title;

  @Size(max = 2000, message = "Description must not exceed 2000 characters")
  @Schema(description = "Document description", example = "Facture de janvier 2024")
  private String description;

  @Schema(description = "Category ID", example = "1")
  private Long categoryId;

  @Schema(description = "Make document public", example = "false")
  private Boolean isPublic;

  @Schema(description = "Tags to associate with the document", example = "[\"facture\", \"2024\"]")
  private List<String> tags;
}
