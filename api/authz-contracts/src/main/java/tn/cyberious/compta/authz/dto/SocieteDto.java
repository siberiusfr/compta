package tn.cyberious.compta.authz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Societe cliente")
public record SocieteDto(
    @Schema(description = "Identifiant unique", example = "1") Long id,
    @Schema(description = "Raison sociale", example = "Entreprise ABC SARL") String raisonSociale,
    @Schema(description = "Matricule fiscale (13 caracteres)", example = "9876543XYZ000")
        String matriculeFiscale,
    @Schema(description = "Code TVA", example = "TVA987654") String codeTva,
    @Schema(description = "Code douane", example = "DOU123") String codeDouane,
    @Schema(description = "Numero registre commerce", example = "RC123456") String registreCommerce,
    @Schema(description = "Forme juridique", example = "SARL") String formeJuridique,
    @Schema(description = "Capital social", example = "50000.00") BigDecimal capitalSocial,
    @Schema(description = "Date de creation de la societe") LocalDate dateCreation,
    @Schema(description = "Adresse", example = "25 Avenue Habib Bourguiba") String adresse,
    @Schema(description = "Ville", example = "Sfax") String ville,
    @Schema(description = "Code postal", example = "3000") String codePostal,
    @Schema(description = "Telephone", example = "+216 74 123 456") String telephone,
    @Schema(description = "Fax", example = "+216 74 123 457") String fax,
    @Schema(description = "Email", example = "contact@entreprise.tn") String email,
    @Schema(description = "Site web", example = "https://www.entreprise.tn") String siteWeb,
    @Schema(description = "Activite principale", example = "Commerce de gros") String activite,
    @Schema(description = "Secteur d'activite", example = "Distribution") String secteur,
    @Schema(description = "ID de la societe comptable gerant cette societe")
        @JsonProperty("societe_comptable_id")
        Long societeComptableId,
    @Schema(description = "Statut actif", example = "true") @JsonProperty("is_active")
        Boolean isActive,
    @Schema(description = "Date de creation") @JsonProperty("created_at") LocalDateTime createdAt,
    @Schema(description = "Date de mise a jour") @JsonProperty("updated_at")
        LocalDateTime updatedAt) {}
