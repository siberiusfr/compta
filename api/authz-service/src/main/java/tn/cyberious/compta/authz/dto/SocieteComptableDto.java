package tn.cyberious.compta.authz.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Societe comptable (cabinet)")
public class SocieteComptableDto {
  @Schema(description = "Identifiant unique", example = "1")
  private Long id;

  @Schema(description = "Raison sociale", example = "Cabinet Expert Comptable SARL")
  private String raisonSociale;

  @Schema(description = "Matricule fiscale (13 caracteres)", example = "1234567ABC000")
  private String matriculeFiscale;

  @Schema(description = "Code TVA", example = "TVA123456")
  private String codeTva;

  @Schema(description = "Adresse", example = "15 Rue de la Comptabilite")
  private String adresse;

  @Schema(description = "Ville", example = "Tunis")
  private String ville;

  @Schema(description = "Code postal", example = "1000")
  private String codePostal;

  @Schema(description = "Telephone", example = "+216 71 123 456")
  private String telephone;

  @Schema(description = "Email", example = "contact@cabinet.tn")
  private String email;

  @Schema(description = "Site web", example = "https://www.cabinet.tn")
  private String siteWeb;

  @Schema(description = "Statut actif", example = "true")
  private Boolean isActive;

  @Schema(description = "Date de creation")
  private LocalDateTime createdAt;

  @Schema(description = "Date de mise a jour")
  private LocalDateTime updatedAt;
}
