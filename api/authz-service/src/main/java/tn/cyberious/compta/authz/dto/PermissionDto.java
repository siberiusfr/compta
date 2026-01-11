package tn.cyberious.compta.authz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Permission granulaire")
public class PermissionDto {
  @Schema(description = "Identifiant unique", example = "1")
  private Long id;

  @Schema(description = "Code unique de la permission", example = "JOURNAL_ENTRY_CREATE")
  private String code;

  @Schema(description = "Ressource concernee", example = "JOURNAL_ENTRY")
  private String resource;

  @Schema(description = "Action autorisee", example = "CREATE")
  private String action;

  @Schema(
      description = "Description de la permission",
      example = "Permet de creer des ecritures comptables")
  private String description;

  @Schema(description = "Date de creation")
  private LocalDateTime createdAt;
}
