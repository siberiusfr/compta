package tn.cyberious.compta.authz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Societe accessible par un utilisateur avec details d'acces")
public record SocieteAccessDto(
    @Schema(description = "ID de la societe", example = "5") Long societeId,
    @Schema(description = "Raison sociale de la societe", example = "Entreprise ABC SARL")
        String raisonSociale,
    @Schema(description = "Matricule fiscale", example = "9876543XYZ000") String matriculeFiscale,
    @Schema(description = "Type d'accès", example = "COMPTABLE")
        UserAccessDto.AccessType accessType,
    @Schema(description = "Role de l'utilisateur", example = "COMPTABLE") String role,
    @Schema(description = "Droit de lecture", example = "true") Boolean canRead,
    @Schema(description = "Droit d'ecriture", example = "true") Boolean canWrite,
    @Schema(description = "Droit de validation", example = "false") Boolean canValidate) {}
