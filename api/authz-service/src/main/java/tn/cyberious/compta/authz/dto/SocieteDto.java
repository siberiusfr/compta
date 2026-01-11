package tn.cyberious.compta.authz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Schema(description = "Societe cliente")
public class SocieteDto {
  @Schema(description = "Identifiant unique", example = "1")
  private Long id;

  @Schema(description = "Raison sociale", example = "Entreprise ABC SARL")
  private String raisonSociale;

  @Schema(description = "Matricule fiscale (13 caracteres)", example = "9876543XYZ000")
  private String matriculeFiscale;

  @Schema(description = "Code TVA", example = "TVA987654")
  private String codeTva;

  @Schema(description = "Code douane", example = "DOU123")
  private String codeDouane;

  @Schema(description = "Numero registre commerce", example = "RC123456")
  private String registreCommerce;

  @Schema(description = "Forme juridique", example = "SARL")
  private String formeJuridique;

  @Schema(description = "Capital social", example = "50000.00")
  private BigDecimal capitalSocial;

  @Schema(description = "Date de creation de la societe")
  private LocalDate dateCreation;

  @Schema(description = "Adresse", example = "25 Avenue Habib Bourguiba")
  private String adresse;

  @Schema(description = "Ville", example = "Sfax")
  private String ville;

  @Schema(description = "Code postal", example = "3000")
  private String codePostal;

  @Schema(description = "Telephone", example = "+216 74 123 456")
  private String telephone;

  @Schema(description = "Fax", example = "+216 74 123 457")
  private String fax;

  @Schema(description = "Email", example = "contact@entreprise.tn")
  private String email;

  @Schema(description = "Site web", example = "https://www.entreprise.tn")
  private String siteWeb;

  @Schema(description = "Activite principale", example = "Commerce de gros")
  private String activite;

  @Schema(description = "Secteur d'activite", example = "Distribution")
  private String secteur;

  @Schema(description = "ID de la societe comptable gerant cette societe")
  private Long societeComptableId;

  @Schema(description = "Statut actif", example = "true")
  private Boolean isActive;

  @Schema(description = "Date de creation")
  private LocalDateTime createdAt;

  @Schema(description = "Date de mise a jour")
  private LocalDateTime updatedAt;
}
