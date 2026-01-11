package tn.cyberious.compta.authz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requete d'assignation d'un utilisateur a une societe comptable")
public class AssignUserToSocieteComptableRequest {

  @NotNull(message = "L'ID utilisateur est obligatoire")
  @Schema(description = "ID de l'utilisateur", example = "42", required = true)
  private Long userId;

  @NotNull(message = "L'ID de la societe comptable est obligatoire")
  @Schema(description = "ID de la societe comptable", example = "1", required = true)
  private Long societeComptableId;

  @NotBlank(message = "Le role est obligatoire")
  @Schema(
      description = "Role dans le cabinet (MANAGER, COMPTABLE, ASSISTANT)",
      example = "COMPTABLE",
      required = true)
  private String role;

  @Schema(description = "Date de debut d'affectation (par defaut: aujourd'hui)")
  private LocalDate dateDebut;

  @Schema(description = "Date de fin d'affectation (null si permanent)")
  private LocalDate dateFin;
}
