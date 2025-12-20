package tn.cyberious.compta.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
  private Long id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String phone;
  private Boolean isActive;
  private Boolean isLocked;
  private Integer failedLoginAttempts;
  private LocalDateTime lastLoginAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<String> roles;
}
