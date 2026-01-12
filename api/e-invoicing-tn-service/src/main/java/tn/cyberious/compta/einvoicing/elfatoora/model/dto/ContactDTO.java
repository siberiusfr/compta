package tn.cyberious.compta.einvoicing.elfatoora.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO representing contact information in El Fatoora invoice. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {

  /** Contact identifier. */
  @Size(max = 17, message = "Contact identifier must not exceed 17 characters")
  private String contactIdentifier;

  /** Contact name. */
  @Size(max = 200, message = "Contact name must not exceed 200 characters")
  private String contactName;

  /** Phone number (communication type I-101). */
  @Size(max = 500, message = "Phone number must not exceed 500 characters")
  private String phone;

  /** Fax number (communication type I-102). */
  @Size(max = 500, message = "Fax number must not exceed 500 characters")
  private String fax;

  /** Email address (communication type I-103). */
  @Email(message = "Invalid email format")
  @Size(max = 500, message = "Email must not exceed 500 characters")
  private String email;

  /** Website URL (communication type I-104). */
  @Size(max = 500, message = "Website URL must not exceed 500 characters")
  private String website;
}
