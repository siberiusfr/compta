package tn.cyberious.compta.authz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requete de creation d'une societe comptable")
public class CreateSocieteComptableRequest {

  @NotBlank(message = "La raison sociale est obligatoire")
  @Size(max = 255, message = "La raison sociale ne peut pas depasser 255 caracteres")
  @Schema(
      description = "Raison sociale",
      example = "Cabinet Expert Comptable SARL",
      required = true)
  private String raisonSociale;

  @NotBlank(message = "Le matricule fiscale est obligatoire")
  @Size(min = 13, max = 13, message = "Le matricule fiscale doit contenir exactement 13 caracteres")
  @Schema(
      description = "Matricule fiscale (13 caracteres)",
      example = "1234567ABC000",
      required = true)
  private String matriculeFiscale;

  @Size(max = 20, message = "Le code TVA ne peut pas depasser 20 caracteres")
  @Schema(description = "Code TVA", example = "TVA123456")
  private String codeTva;

  @Size(max = 255, message = "L'adresse ne peut pas depasser 255 caracteres")
  @Schema(description = "Adresse", example = "15 Rue de la Comptabilite")
  private String adresse;

  @Size(max = 100, message = "La ville ne peut pas depasser 100 caracteres")
  @Schema(description = "Ville", example = "Tunis")
  private String ville;

  @Size(max = 10, message = "Le code postal ne peut pas depasser 10 caracteres")
  @Schema(description = "Code postal", example = "1000")
  private String codePostal;

  @Size(max = 20, message = "Le telephone ne peut pas depasser 20 caracteres")
  @Schema(description = "Telephone", example = "+216 71 123 456")
  private String telephone;

  @Email(message = "L'email doit etre valide")
  @Size(max = 255, message = "L'email ne peut pas depasser 255 caracteres")
  @Schema(description = "Email", example = "contact@cabinet.tn")
  private String email;

  @Size(max = 255, message = "Le site web ne peut pas depasser 255 caracteres")
  @Schema(description = "Site web", example = "https://www.cabinet.tn")
  private String siteWeb;
}
