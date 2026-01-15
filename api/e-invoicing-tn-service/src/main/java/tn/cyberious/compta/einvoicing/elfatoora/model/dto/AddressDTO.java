package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.cyberious.compta.einvoicing.elfatoora.validation.constraints.ValidCountryCode;

/**
 * DTO representing an address in El Fatoora invoice.
 *
 * <p>Validation rules from XSD:
 *
 * <ul>
 *   <li>Address description: Max 500 characters
 *   <li>Street: Max 35 characters
 *   <li>City: Max 35 characters
 *   <li>Postal code: 4 digits (Tunisie)
 *   <li>Country: ISO 3166-1 alpha-2
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

  /** Description de l'adresse (ligne principale). */
  @Size(max = 500, message = "La description d'adresse ne peut pas dépasser 500 caractères")
  private String addressDescription;

  /** Nom de la rue. */
  @Size(max = 35, message = "La rue ne peut pas dépasser 35 caractères")
  private String street;

  /** Ville. */
  @Size(max = 35, message = "La ville ne peut pas dépasser 35 caractères")
  private String city;

  /**
   * Code postal tunisien (4 chiffres).
   *
   * <p>Exemple: 1002 (Tunis Belvédère)
   */
  @Pattern(regexp = "\\d{4}", message = "Le code postal doit contenir exactement 4 chiffres")
  private String postalCode;

  /**
   * Code pays ISO 3166-1 alpha-2.
   *
   * <p>Défaut: TN (Tunisie)
   */
  @NotBlank(message = "Le code pays est obligatoire")
  @ValidCountryCode
  @Builder.Default
  private String country = "TN";

  /** Langue de l'adresse (fr, en, ar). */
  @Builder.Default private String language = "fr";
}
