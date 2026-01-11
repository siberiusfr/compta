package tn.cyberious.compta.document.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request for sharing a document")
public class DocumentShareRequest {

  @NotBlank(message = "Shared with user ID is required")
  @Size(max = 100, message = "User ID must not exceed 100 characters")
  @Schema(description = "User ID to share with", example = "user456")
  private String sharedWith;

  @Pattern(regexp = "^(READ|WRITE)$", message = "Permission must be READ or WRITE")
  @Schema(
      description = "Permission level",
      example = "READ",
      allowableValues = {"READ", "WRITE"})
  private String permission = "READ";

  @Schema(description = "Expiration date (null for no expiration)")
  private LocalDateTime expiresAt;
}
