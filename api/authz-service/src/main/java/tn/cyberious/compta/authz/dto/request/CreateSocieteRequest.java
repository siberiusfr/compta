package tn.cyberious.compta.authz.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de creation d'une societe cliente")
public class CreateSocieteRequest {
  @NotBlank(message = "La raison sociale est obligatoire")
  @Size(max = 255, message = "La raison sociale ne peut pas depasser 255 caracteres")
  @Schema(description = "Raison sociale", example = "Entreprise ABC SARL", required = true)
  private String raisonSociale;

  @NotBlank(message = "Le matricule fiscale est obligatoire")
  @Size(min = 13, max = 13, message = "Le matricule fiscale doit contenir exactement 13 caracteres")
  @Schema(
      description = "Matricule fiscale (13 caracteres)",
      example = "9876543XYZ000",
      required = true)
  private String matriculeFiscale;

  @Size(max = 20, message = "Le code TVA ne peut pas depasser 20 caracteres")
  @Schema(description = "Code TVA", example = "TVA987654")
  private String codeTva;

  @Size(max = 20, message = "Le code douane ne peut pas depasser 20 caracteres")
  @Schema(description = "Code douane", example = "DOU123")
  private String codeDouane;

  @Size(max = 50, message = "Le registre commerce ne peut pas depasser 50 caracteres")
  @Schema(description = "Numero registre commerce", example = "RC123456")
  private String registreCommerce;

  @Size(max = 100, message = "La forme juridique ne peut pas depasser 100 caracteres")
  @Schema(description = "Forme juridique", example = "SARL")
  private String formeJuridique;

  @Positive(message = "Le capital social doit être positif")
  @Schema(description = "Capital social", example = "50000.00")
  private BigDecimal capitalSocial;

  @Schema(description = "Date de creation de la societe")
  private LocalDate dateCreation;

  @Size(max = 255, message = "L'adresse ne peut pas depasser 255 caracteres")
  @Schema(description = "Adresse", example = "25 Avenue Habib Bourguiba")
  private String adresse;

  @Size(max = 100, message = "La ville ne peut pas depasser 100 caracteres")
  @Schema(description = "Ville", example = "Sfax")
  private String ville;

  @Size(max = 10, message = "Le code postal ne peut pas depasser 10 caracteres")
  @Schema(description = "Code postal", example = "3000")
  private String codePostal;

  @Size(max = 20, message = "Le telephone ne peut pas depasser 20 caracteres")
  @Schema(description = "Telephone", example = "+216 74 123 456")
  private String telephone;

  @Size(max = 20, message = "Le fax ne peut pas depasser 20 caracteres")
  @Schema(description = "Fax", example = "+216 74 123 457")
  private String fax;

  @Email(message = "L'email doit être valide")
  @Size(max = 255, message = "L'email ne peut pas depasser 255 caracteres")
  @Schema(description = "Email", example = "contact@entreprise.tn")
  private String email;

  @Size(max = 255, message = "Le site web ne peut pas depasser 255 caracteres")
  @Schema(description = "Site web", example = "https://www.entreprise.tn")
  private String siteWeb;

  @Size(max = 255, message = "L'activite ne peut pas depasser 255 caracteres")
  @Schema(description = "Activite principale", example = "Commerce de gros")
  private String activite;

  @Size(max = 100, message = "Le secteur ne peut pas depasser 100 caracteres")
  @Schema(description = "Secteur d'activite", example = "Distribution")
  private String secteur;

  @Schema(description = "ID de la societe comptable gerant cette societe")
  private Long societeComptableId;
}
