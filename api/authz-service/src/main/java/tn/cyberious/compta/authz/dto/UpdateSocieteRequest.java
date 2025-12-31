package tn.cyberious.compta.authz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSocieteRequest {

  @Size(min = 2, max = 255, message = "La raison sociale doit contenir entre 2 et 255 caractères")
  private String raisonSociale;

  @Size(max = 20, message = "Le code TVA ne peut pas dépasser 20 caractères")
  private String codeTva;

  @Size(max = 20, message = "Le code douane ne peut pas dépasser 20 caractères")
  private String codeDouane;

  @Size(max = 50, message = "Le registre de commerce ne peut pas dépasser 50 caractères")
  private String registreCommerce;

  @Size(max = 100, message = "La forme juridique ne peut pas dépasser 100 caractères")
  private String formeJuridique;

  private BigDecimal capitalSocial;

  private LocalDate dateCreation;

  @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
  private String adresse;

  @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
  private String ville;

  @Size(max = 10, message = "Le code postal ne peut pas dépasser 10 caractères")
  private String codePostal;

  @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
  private String telephone;

  @Size(max = 20, message = "Le fax ne peut pas dépasser 20 caractères")
  private String fax;

  @Email(message = "Email invalide")
  private String email;

  @Size(max = 255, message = "Le site web ne peut pas dépasser 255 caractères")
  private String siteWeb;

  @Size(max = 255, message = "L'activité ne peut pas dépasser 255 caractères")
  private String activite;

  @Size(max = 100, message = "Le secteur ne peut pas dépasser 100 caractères")
  private String secteur;

  private Boolean isActive;
}
