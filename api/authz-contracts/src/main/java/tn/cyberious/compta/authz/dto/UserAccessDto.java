package tn.cyberious.compta.authz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Information d'acces unifie d'un utilisateur a une societe")
public record UserAccessDto(
    @Schema(description = "ID de l'utilisateur", example = "42") Long userId,
    @Schema(description = "ID de la societe", example = "5") Long societeId,
    @Schema(description = "Indique si l'utilisateur a un acces a cette societe", example = "true")
        boolean hasAccess,
    @Schema(description = "Type d'accès", example = "COMPTABLE") AccessType accessType,
    @Schema(description = "Role de l'utilisateur dans le contexte d'acces", example = "COMPTABLE")
        String role,
    @Schema(description = "Droit de lecture", example = "true") Boolean canRead,
    @Schema(description = "Droit d'ecriture", example = "true") Boolean canWrite,
    @Schema(description = "Droit de validation", example = "false") Boolean canValidate) {

  public enum AccessType {
    COMPTABLE, // Accès via comptable_societes
    MEMBRE, // Accès via user_societes (employé de la société)
    NONE // Pas d'accès
  }

  public static UserAccessDto noAccess(Long userId, Long societeId) {
    return new UserAccessDto(userId, societeId, false, AccessType.NONE, null, false, false, false);
  }
}
