package tn.cyberious.compta.authz.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'assignation d'un utilisateur a une societe cliente")
public class AssignUserToSocieteRequest {
  @NotNull(message = "L'ID utilisateur est obligatoire")
  @Schema(description = "ID de l'utilisateur", example = "42", required = true)
  private Long userId;

  @NotNull(message = "L'ID de la societe cliente est obligatoire")
  @Schema(description = "ID de la societe cliente", example = "5", required = true)
  private Long societeId;

  @NotBlank(message = "Le rôle est obligatoire")
  @Schema(
      description = "Rôle dans la societe (MANAGER, FINANCE, VIEWER)",
      example = "FINANCE",
      required = true)
  private String role;

  @Schema(description = "Date de debut d'affectation (par defaut: aujourd'hui)")
  private LocalDate dateDebut;

  @Schema(description = "Date de fin d'affectation (null si permanent)")
  private LocalDate dateFin;
}
