package tn.cyberious.compta.referentiel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Request to create or update a produit")
public record ProduitRequest(
    @NotBlank @Size(max = 50) @Schema(description = "Référence unique du produit") String reference,
    @NotBlank @Size(max = 255) @Schema(description = "Désignation du produit") String designation,
    @Schema(description = "Description") String description,
    @Schema(description = "Prix d'achat") BigDecimal prixAchat,
    @Schema(description = "Prix de vente") BigDecimal prixVente,
    @Schema(description = "Taux TVA") BigDecimal tauxTva,
    @Size(max = 20) @Schema(description = "Unité") String unite,
    @NotNull
        @Schema(
            description = "Type de stock: STOCKABLE ou NON_STOCKABLE",
            allowableValues = {"STOCKABLE", "NON_STOCKABLE"})
        String typeStock,
    @NotNull
        @Schema(
            description = "Type d'article: PRODUIT ou SERVICE",
            allowableValues = {"PRODUIT", "SERVICE"})
        String typeArticle,
    @Schema(description = "ID de la famille") Long familleId,
    @Schema(description = "Actif") Boolean actif) {}
