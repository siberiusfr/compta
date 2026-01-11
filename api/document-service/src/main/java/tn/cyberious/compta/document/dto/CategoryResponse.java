package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Category response")
public class CategoryResponse {

  @Schema(description = "Category ID", example = "1")
  private Long id;

  @Schema(description = "Category name", example = "Comptabilité")
  private String name;

  @Schema(description = "Category description", example = "Documents comptables et financiers")
  private String description;

  @Schema(description = "Parent category ID", example = "null")
  private Long parentCategoryId;

  @Schema(description = "Parent category name", example = "null")
  private String parentCategoryName;

  @Schema(description = "Sub-categories")
  private List<CategoryResponse> children;

  @Schema(description = "Creation timestamp")
  private LocalDateTime createdAt;

  @Schema(description = "Last update timestamp")
  private LocalDateTime updatedAt;
}
