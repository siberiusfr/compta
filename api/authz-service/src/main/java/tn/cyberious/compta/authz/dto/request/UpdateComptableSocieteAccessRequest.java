package tn.cyberious.compta.authz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise a jour des droits d'un comptable sur une societe")
public class UpdateComptableSocieteAccessRequest {
    @Schema(description = "Droit de lecture", example = "true")
    private Boolean canRead;

    @Schema(description = "Droit d'ecriture", example = "true")
    private Boolean canWrite;

    @Schema(description = "Droit de validation", example = "false")
    private Boolean canValidate;

    @Schema(description = "Date de fin d'acces (null si permanent)")
    private LocalDate dateFin;

    @Schema(description = "Statut actif", example = "true")
    private Boolean isActive;
}
