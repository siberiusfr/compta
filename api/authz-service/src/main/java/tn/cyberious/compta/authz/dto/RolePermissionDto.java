package tn.cyberious.compta.authz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Association rôle - permission")
public class RolePermissionDto {
    @Schema(description = "Identifiant unique", example = "1")
    private Long id;

    @Schema(description = "Nom du rôle", example = "COMPTABLE")
    private String role;

    @Schema(description = "ID de la permission", example = "5")
    private Long permissionId;

    @Schema(description = "Date de creation")
    private LocalDateTime createdAt;
}
