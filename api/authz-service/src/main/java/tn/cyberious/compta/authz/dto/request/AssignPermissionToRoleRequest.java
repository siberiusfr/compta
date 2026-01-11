package tn.cyberious.compta.authz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'assignation d'une permission a un rôle")
public class AssignPermissionToRoleRequest {
  @NotBlank(message = "Le rôle est obligatoire")
  @Schema(description = "Nom du rôle", example = "COMPTABLE", required = true)
  private String role;

  @NotNull(message = "L'ID de la permission est obligatoire")
  @Schema(description = "ID de la permission", example = "5", required = true)
  private Long permissionId;
}
