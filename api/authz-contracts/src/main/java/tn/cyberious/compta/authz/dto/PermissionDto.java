package tn.cyberious.compta.authz.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Permission granulaire")
public record PermissionDto(
    @Schema(description = "Identifiant unique", example = "1") Long id,
    @Schema(description = "Code unique de la permission", example = "JOURNAL_ENTRY_CREATE")
        String code,
    @Schema(description = "Ressource concernee", example = "JOURNAL_ENTRY") String resource,
    @Schema(description = "Action autorisee", example = "CREATE") String action,
    @Schema(
            description = "Description de la permission",
            example = "Permet de creer des ecritures comptables")
        String description,
    @Schema(description = "Date de creation") @JsonProperty("created_at")
        LocalDateTime createdAt) {}
