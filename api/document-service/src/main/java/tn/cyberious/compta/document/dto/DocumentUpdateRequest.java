package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "Request for updating a document")
public class DocumentUpdateRequest {

  @Size(max = 255, message = "Title must not exceed 255 characters")
  @Schema(description = "Document title", example = "Facture 2024-001 - Updated")
  private String title;

  @Size(max = 2000, message = "Description must not exceed 2000 characters")
  @Schema(description = "Document description", example = "Facture de janvier 2024 - corrigée")
  private String description;

  @Schema(description = "Category ID", example = "2")
  private Long categoryId;

  @Schema(description = "Make document public", example = "true")
  private Boolean isPublic;

  @Schema(
      description = "Tags to associate with the document",
      example = "[\"facture\", \"2024\", \"corrigé\"]")
  private List<String> tags;
}
