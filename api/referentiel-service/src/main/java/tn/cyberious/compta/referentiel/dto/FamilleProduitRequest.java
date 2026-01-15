package tn.cyberious.compta.referentiel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create or update a famille produit")
public record FamilleProduitRequest(
    @NotBlank @Size(max = 50) @Schema(description = "Code unique de la famille") String code,
    @NotBlank @Size(max = 255) @Schema(description = "Libellé de la famille") String libelle,
    @Schema(description = "Description") String description,
    @Schema(description = "Actif") Boolean actif) {}
