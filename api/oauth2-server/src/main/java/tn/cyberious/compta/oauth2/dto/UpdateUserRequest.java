package tn.cyberious.compta.oauth2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

  @Size(max = 100, message = "First name must not exceed 100 characters")
  private String firstName;

  @Size(max = 100, message = "Last name must not exceed 100 characters")
  private String lastName;

  @Email(message = "Email must be valid")
  private String email;

  private List<String> roles;
}
