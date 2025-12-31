package tn.cyberious.compta.oauth2.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private String id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private Boolean enabled;
  private Boolean accountNonExpired;
  private Boolean accountNonLocked;
  private Boolean credentialsNonExpired;
  private List<String> roles;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
