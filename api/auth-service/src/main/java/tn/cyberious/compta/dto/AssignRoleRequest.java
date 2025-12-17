package tn.cyberious.compta.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotBlank(message = "Le nom du rôle est requis")
    private String roleName;
}
