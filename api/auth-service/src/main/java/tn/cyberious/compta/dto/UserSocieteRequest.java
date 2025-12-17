package tn.cyberious.compta.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

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
