package tn.cyberious.compta.referentiel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Client response")
public record ClientResponse(
    @Schema(description = "ID du client") Long id,
    @Schema(description = "Code unique du client") String code,
    @Schema(description = "Raison sociale") String raisonSociale,
    @Schema(description = "Matricule fiscal") String matriculeFiscal,
    @Schema(description = "Adresse") String adresse,
    @Schema(description = "Ville") String ville,
    @Schema(description = "Code postal") String codePostal,
    @Schema(description = "Téléphone") String telephone,
    @Schema(description = "Email") String email,
    @Schema(description = "Type de client") String typeClient,
    @Schema(description = "ID de l'entreprise") Long entrepriseId,
    @Schema(description = "Actif") Boolean actif,
    @Schema(description = "Date de création") LocalDateTime createdAt,
    @Schema(description = "Date de mise à jour") LocalDateTime updatedAt) {}
