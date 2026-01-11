package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Document share response")
public class DocumentShareResponse {

  @Schema(description = "Share ID", example = "1")
  private Long id;

  @Schema(description = "Document ID", example = "1")
  private Long documentId;

  @Schema(description = "Document title", example = "Facture 2024-001")
  private String documentTitle;

  @Schema(description = "Shared with user ID", example = "user456")
  private String sharedWith;

  @Schema(description = "Permission level", example = "READ")
  private String permission;

  @Schema(description = "Expiration date")
  private LocalDateTime expiresAt;

  @Schema(description = "Is share still active")
  private Boolean isActive;

  @Schema(description = "Created by user ID", example = "user123")
  private String createdBy;

  @Schema(description = "Creation timestamp")
  private LocalDateTime createdAt;
}
