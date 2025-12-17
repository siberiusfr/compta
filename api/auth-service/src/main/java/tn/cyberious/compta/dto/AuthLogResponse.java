package tn.cyberious.compta.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
