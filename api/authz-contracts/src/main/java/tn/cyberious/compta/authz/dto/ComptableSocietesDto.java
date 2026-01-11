package tn.cyberious.compta.authz.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Acces d'un comptable a une societe cliente")
public record ComptableSocietesDto(
    @Schema(description = "Identifiant unique", example = "1") Long id,
    @Schema(description = "ID de l'utilisateur comptable", example = "42") @JsonProperty("user_id")
        Long userId,
    @Schema(description = "ID de la societe cliente", example = "5") @JsonProperty("societe_id")
        Long societeId,
    @Schema(description = "Droit de lecture", example = "true") Boolean canRead,
    @Schema(description = "Droit d'ecriture", example = "true") Boolean canWrite,
    @Schema(description = "Droit de validation", example = "false") Boolean canValidate,
    @Schema(description = "Date de debut d'acces") @JsonProperty("date_debut") LocalDate dateDebut,
    @Schema(description = "Date de fin d'acces (null si permanent)") @JsonProperty("date_fin")
        LocalDate dateFin,
    @Schema(description = "Statut actif", example = "true") @JsonProperty("is_active")
        Boolean isActive,
    @Schema(description = "Date de creation") @JsonProperty("created_at")
        LocalDateTime createdAt) {}
