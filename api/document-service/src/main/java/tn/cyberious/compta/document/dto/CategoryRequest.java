package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request for creating or updating a category")
public class CategoryRequest {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must not exceed 100 characters")
  @Schema(description = "Category name", example = "Comptabilité")
  private String name;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  @Schema(description = "Category description", example = "Documents comptables et financiers")
  private String description;

  @Schema(description = "Parent category ID for hierarchical structure", example = "1")
  private Long parentCategoryId;
}
