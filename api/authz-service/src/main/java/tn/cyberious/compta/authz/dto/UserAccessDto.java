package tn.cyberious.compta.authz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Information d'acces d'un utilisateur a une societe")
public class UserAccessDto {

  @Schema(description = "ID de l'utilisateur", example = "1")
  private Long userId;

  @Schema(description = "ID de la societe", example = "10")
  private Long societeId;

  @Schema(description = "L'utilisateur a acces a la societe", example = "true")
  private boolean hasAccess;

  @Schema(description = "Type d'acces: COMPTABLE ou MEMBRE", example = "COMPTABLE")
  private AccessType accessType;

  @Schema(description = "Role de l'utilisateur", example = "MANAGER")
  private String role;

  @Schema(description = "Droit de lecture (pour comptables)", example = "true")
  private Boolean canRead;

  @Schema(description = "Droit d'ecriture (pour comptables)", example = "false")
  private Boolean canWrite;

  @Schema(description = "Droit de validation (pour comptables)", example = "false")
  private Boolean canValidate;

  public enum AccessType {
    COMPTABLE, // Acces via comptable_societes
    MEMBRE, // Acces via user_societes (employe de la societe)
    NONE // Pas d'acces
  }

  public static UserAccessDto noAccess(Long userId, Long societeId) {
    return UserAccessDto.builder()
        .userId(userId)
        .societeId(societeId)
        .hasAccess(false)
        .accessType(AccessType.NONE)
        .build();
  }
}
