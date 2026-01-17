package tn.cyberious.compta.oauth2.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

  private String id;
  private String name;
  private String description;
  private LocalDateTime createdAt;
}
