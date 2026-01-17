package tn.cyberious.compta.authz.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.authz.dto.UserSocieteComptableDto;
import tn.cyberious.compta.authz.dto.request.AssignUserToSocieteComptableRequest;
import tn.cyberious.compta.authz.enums.CabinetRole;
import tn.cyberious.compta.authz.generated.tables.pojos.UserSocieteComptable;
import tn.cyberious.compta.authz.repository.SocieteComptableRepository;
import tn.cyberious.compta.authz.repository.UserSocieteComptableRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSocieteComptableService {

  private final UserSocieteComptableRepository userSocieteComptableRepository;
  private final SocieteComptableRepository societeComptableRepository;

  @Transactional
  public UserSocieteComptableDto assignUser(AssignUserToSocieteComptableRequest request) {
    // Valider le rôle
    CabinetRole role = CabinetRole.fromCode(request.getRole());

    // Verifier que la societe comptable existe
    if (!societeComptableRepository.existsById(request.getSocieteComptableId())) {
      throw new IllegalArgumentException(
          "Societe comptable non trouvee: " + request.getSocieteComptableId());
    }

    // Verifier si l'utilisateur est deja assigne
    if (userSocieteComptableRepository.existsByUserId(request.getUserId())) {
      throw new IllegalArgumentException("L'utilisateur est deja assigne a une societe comptable");
    }

    // Verifier la contrainte MANAGER unique
    if (role == CabinetRole.MANAGER
        && userSocieteComptableRepository.existsActiveManagerBySocieteComptableId(
            request.getSocieteComptableId())) {
      throw new IllegalArgumentException("Cette societe comptable a deja un manager actif");
    }

    UserSocieteComptable assignment = new UserSocieteComptable();
    assignment.setUserId(request.getUserId());
    assignment.setSocieteComptableId(request.getSocieteComptableId());
    assignment.setRole(role.getCode());
    assignment.setDateDebut(request.getDateDebut());
    assignment.setDateFin(request.getDateFin());

    UserSocieteComptable created = userSocieteComptableRepository.insert(assignment);
    log.info(
        "Utilisateur {} assigne a la societe comptable {} avec le rôle {}",
        request.getUserId(),
        request.getSocieteComptableId(),
        role.getCode());
    return toDto(created);
  }

  @Transactional
  public UserSocieteComptableDto updateRole(Long id, String newRole) {
    CabinetRole role = CabinetRole.fromCode(newRole);

    UserSocieteComptable existing =
        userSocieteComptableRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assignation non trouvee: " + id));

    // Verifier la contrainte MANAGER unique si changement vers MANAGER
    if (role == CabinetRole.MANAGER && !"MANAGER".equals(existing.getRole())) {
      if (userSocieteComptableRepository.existsActiveManagerBySocieteComptableId(
          existing.getSocieteComptableId())) {
        throw new IllegalArgumentException("Cette societe comptable a deja un manager actif");
      }
    }

    existing.setRole(role.getCode());
    UserSocieteComptable updated = userSocieteComptableRepository.update(existing);
    log.info("Rôle de l'utilisateur {} mis a jour: {}", existing.getUserId(), role.getCode());
    return toDto(updated);
  }

  @Transactional
  public void deactivate(Long id) {
    UserSocieteComptable existing =
        userSocieteComptableRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assignation non trouvee: " + id));

    existing.setIsActive(false);
    userSocieteComptableRepository.update(existing);
    log.info("Assignation desactivee pour l'utilisateur {}", existing.getUserId());
  }

  @Transactional
  public void delete(Long id) {
    if (userSocieteComptableRepository.findById(id).isEmpty()) {
      throw new IllegalArgumentException("Assignation non trouvee: " + id);
    }
    userSocieteComptableRepository.delete(id);
    log.info("Assignation supprimee: ID {}", id);
  }

  @Transactional(readOnly = true)
  public UserSocieteComptableDto findById(Long id) {
    return userSocieteComptableRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new IllegalArgumentException("Assignation non trouvee: " + id));
  }

  @Transactional(readOnly = true)
  public UserSocieteComptableDto findByUserId(Long userId) {
    return userSocieteComptableRepository
        .findByUserId(userId)
        .map(this::toDto)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Aucune assignation trouvee pour l'utilisateur: " + userId));
  }

  @Transactional(readOnly = true)
  public UserSocieteComptableDto findActiveByUserId(Long userId) {
    return userSocieteComptableRepository.findActiveByUserId(userId).map(this::toDto).orElse(null);
  }

  @Transactional(readOnly = true)
  public List<UserSocieteComptableDto> findBySocieteComptableId(Long societeComptableId) {
    return userSocieteComptableRepository.findBySocieteComptableId(societeComptableId).stream()
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<UserSocieteComptableDto> findActiveBySocieteComptableId(Long societeComptableId) {
    return userSocieteComptableRepository
        .findActiveBySocieteComptableId(societeComptableId)
        .stream()
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public UserSocieteComptableDto findManagerBySocieteComptableId(Long societeComptableId) {
    return userSocieteComptableRepository
        .findManagerBySocieteComptableId(societeComptableId)
        .map(this::toDto)
        .orElse(null);
  }

  private UserSocieteComptableDto toDto(UserSocieteComptable entity) {
    return UserSocieteComptableDto.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .societeComptableId(entity.getSocieteComptableId())
        .role(entity.getRole())
        .dateDebut(entity.getDateDebut())
        .dateFin(entity.getDateFin())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
