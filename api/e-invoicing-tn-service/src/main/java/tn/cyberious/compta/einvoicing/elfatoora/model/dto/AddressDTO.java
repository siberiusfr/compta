package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO representing an address in El Fatoora invoice. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

  /** Address description (main address line). */
  @Size(max = 500, message = "Address description must not exceed 500 characters")
  private String addressDescription;

  /** Street name. */
  @Size(max = 35, message = "Street must not exceed 35 characters")
  private String street;

  /** City name. */
  @Size(max = 35, message = "City name must not exceed 35 characters")
  private String city;

  /** Postal code (4 digits for Tunisia). */
  @Pattern(regexp = "\\d{4}", message = "Postal code must be exactly 4 digits")
  private String postalCode;

  /** Country code (ISO 3166-1 alpha-2). Defaults to TN. */
  @NotBlank(message = "Country code is required")
  @Size(min = 2, max = 2, message = "Country code must be 2 characters")
  @Builder.Default
  private String country = "TN";

  /** Language for address (fr, en, ar). */
  @Builder.Default private String language = "fr";
}
