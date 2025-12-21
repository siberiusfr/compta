package tn.cyberious.compta.authz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {
  @NotNull(message = "User ID is required")
  private Long userId;

  @NotNull(message = "Societe ID is required")
  private Long societeId;

  private String matriculeEmployee;

  @NotBlank(message = "Poste is required")
  private String poste;

  private String departement;
  private LocalDate dateEmbauche;
  private LocalDate dateFinContrat;
  private String typeContrat; // CDI, CDD, SIVP, KARAMA, etc.
}
