package tn.cyberious.compta.authz.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Association rôle - permission")
public record RolePermissionDto(
    @Schema(description = "Identifiant unique", example = "1") Long id,
    @Schema(description = "Nom du rôle", example = "COMPTABLE") String role,
    @Schema(description = "ID de la permission", example = "5") Long permissionId,
    @Schema(description = "Date de creation") @JsonProperty("created_at")
        LocalDateTime createdAt) {}
