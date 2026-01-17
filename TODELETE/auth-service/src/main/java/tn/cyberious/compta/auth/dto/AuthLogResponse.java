package tn.cyberious.compta.auth.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthLogResponse {
  private Long id;
  private Long userId;
  private String username;
  private String action;
  private String ipAddress;
  private String userAgent;
  private String details;
  private LocalDateTime createdAt;
}
