package tn.cyberious.compta.oauth2.dto;

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
public class PasswordResetRequest {

  @Email(message = "Email must be valid")
  @NotBlank(message = "Email is required")
  private String email;

  @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
  private String newPassword;

  @NotBlank(message = "Reset token is required")
  private String token;
}
