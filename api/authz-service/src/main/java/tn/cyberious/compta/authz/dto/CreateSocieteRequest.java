package tn.cyberious.compta.authz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSocieteRequest {
  @NotBlank(message = "Raison sociale is required")
  @Size(max = 255, message = "Raison sociale cannot exceed 255 characters")
  private String raisonSociale;

  @NotBlank(message = "Matricule fiscale is required")
  @Pattern(
      regexp = "\\d{7}[A-Z]\\d{3}",
      message = "Matricule fiscale must follow format: 7 digits + letter + 3 digits")
  private String matriculeFiscale;

  private String codeTva;
  private String codeDouane;
  private String registreCommerce;
  private String formeJuridique;
  private BigDecimal capitalSocial;
  private LocalDate dateCreation;
  private String adresse;
  private String ville;
  private String codePostal;
  private String telephone;
  private String fax;

  @Email(message = "Email should be valid")
  private String email;

  private String siteWeb;
  private String activite;
  private String secteur;
}
