package tn.cyberious.compta.authz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.cyberious.compta.authz.dto.UserAccessDto.AccessType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Societe accessible par un utilisateur avec details d'acces")
public class SocieteAccessDto {

  @Schema(description = "ID de la societe", example = "10")
  private Long societeId;

  @Schema(description = "Raison sociale", example = "ACME SARL")
  private String raisonSociale;

  @Schema(description = "Matricule fiscale", example = "1234567ABC000")
  private String matriculeFiscale;

  @Schema(description = "Type d'acces: COMPTABLE ou MEMBRE", example = "COMPTABLE")
  private AccessType accessType;

  @Schema(description = "Role de l'utilisateur", example = "MANAGER")
  private String role;

  @Schema(description = "Droit de lecture", example = "true")
  private Boolean canRead;

  @Schema(description = "Droit d'ecriture", example = "true")
  private Boolean canWrite;

  @Schema(description = "Droit de validation", example = "false")
  private Boolean canValidate;
}
