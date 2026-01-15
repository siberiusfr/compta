package tn.cyberious.compta.referentiel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Famille produit response")
public record FamilleProduitResponse(
    @Schema(description = "ID de la famille") Long id,
    @Schema(description = "Code unique de la famille") String code,
    @Schema(description = "Libellé de la famille") String libelle,
    @Schema(description = "Description") String description,
    @Schema(description = "ID de l'entreprise") Long entrepriseId,
    @Schema(description = "Actif") Boolean actif,
    @Schema(description = "Date de création") LocalDateTime createdAt,
    @Schema(description = "Date de mise à jour") LocalDateTime updatedAt) {}
