package tn.cyberious.compta.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

  @Email(message = "Email invalide")
  private String email;

  @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
  private String firstName;

  @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
  private String lastName;

  @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
  private String phone;
}
