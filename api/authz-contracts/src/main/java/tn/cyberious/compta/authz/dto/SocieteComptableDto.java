package tn.cyberious.compta.authz.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Societe comptable (cabinet)")
public record SocieteComptableDto(
    @Schema(description = "Identifiant unique", example = "1") Long id,
    @Schema(description = "Raison sociale", example = "Cabinet Expert Comptable SARL")
        String raisonSociale,
    @Schema(description = "Matricule fiscale (13 caracteres)", example = "1234567ABC000")
        String matriculeFiscale,
    @Schema(description = "Code TVA", example = "TVA123456") String codeTva,
    @Schema(description = "Adresse", example = "15 Rue de la Comptabilite") String adresse,
    @Schema(description = "Ville", example = "Tunis") String ville,
    @Schema(description = "Code postal", example = "1000") String codePostal,
    @Schema(description = "Telephone", example = "+216 71 123 456") String telephone,
    @Schema(description = "Email", example = "contact@cabinet.tn") String email,
    @Schema(description = "Site web", example = "https://www.cabinet.tn") String siteWeb,
    @Schema(description = "Statut actif", example = "true") @JsonProperty("is_active")
        Boolean isActive,
    @Schema(description = "Date de creation") @JsonProperty("created_at") LocalDateTime createdAt,
    @Schema(description = "Date de mise a jour") @JsonProperty("updated_at")
        LocalDateTime updatedAt) {}
