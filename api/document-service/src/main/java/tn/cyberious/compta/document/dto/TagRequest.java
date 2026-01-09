package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request for creating a tag")
public class TagRequest {

  @NotBlank(message = "Name is required")
  @Size(max = 50, message = "Name must not exceed 50 characters")
  @Schema(description = "Tag name", example = "facture")
  private String name;
}
