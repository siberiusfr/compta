package tn.cyberious.compta.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Tag response")
public class TagResponse {

  @Schema(description = "Tag ID", example = "1")
  private Long id;

  @Schema(description = "Tag name", example = "facture")
  private String name;

  @Schema(description = "Creation timestamp")
  private LocalDateTime createdAt;
}
