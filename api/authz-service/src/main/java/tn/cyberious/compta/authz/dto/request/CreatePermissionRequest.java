package tn.cyberious.compta.authz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de creation d'une permission")
public class CreatePermissionRequest {
  @NotBlank(message = "Le code est obligatoire")
  @Size(max = 100, message = "Le code ne peut pas depasser 100 caracteres")
  @Schema(
      description = "Code unique de la permission",
      example = "JOURNAL_ENTRY_CREATE",
      required = true)
  private String code;

  @NotBlank(message = "La ressource est obligatoire")
  @Size(max = 50, message = "La ressource ne peut pas depasser 50 caracteres")
  @Schema(description = "Ressource concernee", example = "JOURNAL_ENTRY", required = true)
  private String resource;

  @NotBlank(message = "L'action est obligatoire")
  @Size(max = 50, message = "L'action ne peut pas depasser 50 caracteres")
  @Schema(description = "Action autorisee", example = "CREATE", required = true)
  private String action;

  @Schema(
      description = "Description de la permission",
      example = "Permet de creer des ecritures comptables")
  private String description;
}
