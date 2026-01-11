package tn.cyberious.compta.authz.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Association utilisateur - societe cliente")
public record UserSocietesDto(
    @Schema(description = "Identifiant unique", example = "1") Long id,
    @Schema(description = "ID de l'utilisateur", example = "42") @JsonProperty("user_id")
        Long userId,
    @Schema(description = "ID de la societe cliente", example = "5") @JsonProperty("societe_id")
        Long societeId,
    @Schema(description = "Rôle dans la societe", example = "FINANCE") String role,
    @Schema(description = "Date de debut d'affectation") @JsonProperty("date_debut")
        LocalDate dateDebut,
    @Schema(description = "Date de fin d'affectation (null si toujours actif)")
        @JsonProperty("date_fin")
        LocalDate dateFin,
    @Schema(description = "Statut actif", example = "true") @JsonProperty("is_active")
        Boolean isActive,
    @Schema(description = "Date de creation") @JsonProperty("created_at")
        LocalDateTime createdAt) {}
