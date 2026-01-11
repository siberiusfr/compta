package tn.cyberious.compta.authz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Requete d'assignation d'un comptable a une societe cliente")
public class AssignComptableToSocieteRequest {

  @NotNull(message = "L'ID utilisateur est obligatoire")
  @Schema(description = "ID de l'utilisateur comptable", example = "42", required = true)
  private Long userId;

  @NotNull(message = "L'ID de la societe cliente est obligatoire")
  @Schema(description = "ID de la societe cliente", example = "5", required = true)
  private Long societeId;

  @Schema(description = "Droit de lecture", example = "true")
  @Builder.Default
  private Boolean canRead = true;

  @Schema(description = "Droit d'ecriture", example = "true")
  @Builder.Default
  private Boolean canWrite = true;

  @Schema(description = "Droit de validation", example = "false")
  @Builder.Default
  private Boolean canValidate = false;

  @Schema(description = "Date de debut d'acces (par defaut: aujourd'hui)")
  private LocalDate dateDebut;

  @Schema(description = "Date de fin d'acces (null si permanent)")
  private LocalDate dateFin;
}
