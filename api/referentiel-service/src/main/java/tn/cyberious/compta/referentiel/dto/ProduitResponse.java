package tn.cyberious.compta.referentiel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Produit response")
public record ProduitResponse(
    @Schema(description = "ID du produit") Long id,
    @Schema(description = "Référence unique du produit") String reference,
    @Schema(description = "Désignation du produit") String designation,
    @Schema(description = "Description") String description,
    @Schema(description = "Prix d'achat") BigDecimal prixAchat,
    @Schema(description = "Prix de vente") BigDecimal prixVente,
    @Schema(description = "Taux TVA") BigDecimal tauxTva,
    @Schema(description = "Unité") String unite,
    @Schema(description = "Type de stock") String typeStock,
    @Schema(description = "Type d'article") String typeArticle,
    @Schema(description = "ID de la famille") Long familleId,
    @Schema(description = "ID de l'entreprise") Long entrepriseId,
    @Schema(description = "Actif") Boolean actif,
    @Schema(description = "Date de création") LocalDateTime createdAt,
    @Schema(description = "Date de mise à jour") LocalDateTime updatedAt) {}
