package tn.cyberious.compta.authz.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UserSocieteRequest {

  @NotNull(message = "L'ID de l'utilisateur est requis")
  private Long userId;

  @NotNull(message = "L'ID de la société est requis")
  private Long societeId;

  private Boolean isOwner = false;

  @NotNull(message = "La date de début est requise")
  private LocalDate dateDebut;

  private LocalDate dateFin;
}
