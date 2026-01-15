package tn.cyberious.compta.referentiel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create or update a client")
public record ClientRequest(
    @NotBlank @Size(max = 50) @Schema(description = "Code unique du client") String code,
    @NotBlank @Size(max = 255) @Schema(description = "Raison sociale") String raisonSociale,
    @Size(max = 50) @Schema(description = "Matricule fiscal") String matriculeFiscal,
    @Schema(description = "Adresse") String adresse,
    @Size(max = 100) @Schema(description = "Ville") String ville,
    @Size(max = 10) @Schema(description = "Code postal") String codePostal,
    @Size(max = 20) @Schema(description = "Téléphone") String telephone,
    @Size(max = 100) @Schema(description = "Email") String email,
    @NotBlank
        @Schema(
            description = "Type de client: PARTICULIER ou ENTREPRISE",
            allowableValues = {"PARTICULIER", "ENTREPRISE"})
        String typeClient,
    @Schema(description = "Actif") Boolean actif) {}
