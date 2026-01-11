package tn.cyberious.compta.authz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Association utilisateur - societe comptable")
public class UserSocieteComptableDto {
  @Schema(description = "Identifiant unique", example = "1")
  private Long id;

  @Schema(description = "ID de l'utilisateur", example = "42")
  private Long userId;

  @Schema(description = "ID de la societe comptable", example = "1")
  private Long societeComptableId;

  @Schema(description = "Rôle dans le cabinet", example = "COMPTABLE")
  private String role;

  @Schema(description = "Date de debut d'affectation")
  private LocalDate dateDebut;

  @Schema(description = "Date de fin d'affectation (null si toujours actif)")
  private LocalDate dateFin;

  @Schema(description = "Statut actif", example = "true")
  private Boolean isActive;

  @Schema(description = "Date de creation")
  private LocalDateTime createdAt;
}
