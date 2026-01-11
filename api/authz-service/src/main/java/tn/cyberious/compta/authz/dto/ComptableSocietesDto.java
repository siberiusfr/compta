package tn.cyberious.compta.authz.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Acces d'un comptable a une societe cliente")
public class ComptableSocietesDto {
  @Schema(description = "Identifiant unique", example = "1")
  private Long id;

  @Schema(description = "ID de l'utilisateur comptable", example = "42")
  private Long userId;

  @Schema(description = "ID de la societe cliente", example = "5")
  private Long societeId;

  @Schema(description = "Droit de lecture", example = "true")
  private Boolean canRead;

  @Schema(description = "Droit d'ecriture", example = "true")
  private Boolean canWrite;

  @Schema(description = "Droit de validation", example = "false")
  private Boolean canValidate;

  @Schema(description = "Date de debut d'acces")
  private LocalDate dateDebut;

  @Schema(description = "Date de fin d'acces (null si permanent)")
  private LocalDate dateFin;

  @Schema(description = "Statut actif", example = "true")
  private Boolean isActive;

  @Schema(description = "Date de creation")
  private LocalDateTime createdAt;
}
