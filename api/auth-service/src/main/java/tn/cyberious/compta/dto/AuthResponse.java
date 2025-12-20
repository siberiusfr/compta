package tn.cyberious.compta.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
  private String token;
  private String refreshToken;
  @Builder.Default private String type = "Bearer";
  private Long userId;
  private String username;
  private String email;
  private List<String> roles;
}
