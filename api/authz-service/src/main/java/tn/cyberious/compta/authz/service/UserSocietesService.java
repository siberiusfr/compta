package tn.cyberious.compta.authz.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.authz.dto.SocieteDto;
import tn.cyberious.compta.authz.dto.UserSocietesDto;
import tn.cyberious.compta.authz.dto.request.AssignUserToSocieteRequest;
import tn.cyberious.compta.authz.enums.SocieteRole;
import tn.cyberious.compta.authz.generated.tables.pojos.Societes;
import tn.cyberious.compta.authz.generated.tables.pojos.UserSocietes;
import tn.cyberious.compta.authz.repository.SocieteRepository;
import tn.cyberious.compta.authz.repository.UserSocietesRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSocietesService {

  private final UserSocietesRepository userSocietesRepository;
  private final SocieteRepository societeRepository;

  @Transactional
  public UserSocietesDto assignUser(AssignUserToSocieteRequest request) {
    // Valider le rôle
    SocieteRole role = SocieteRole.fromCode(request.getRole());

    // Verifier que la societe existe
    if (!societeRepository.existsById(request.getSocieteId())) {
      throw new IllegalArgumentException("Societe non trouvee: " + request.getSocieteId());
    }

    // Verifier si l'utilisateur est deja assigne
    if (userSocietesRepository.existsByUserId(request.getUserId())) {
      throw new IllegalArgumentException("L'utilisateur est deja assigne a une societe cliente");
    }

    // Verifier la contrainte MANAGER unique
    if (role == SocieteRole.MANAGER
        && userSocietesRepository.existsActiveManagerBySocieteId(request.getSocieteId())) {
      throw new IllegalArgumentException("Cette societe a deja un manager actif");
    }

    UserSocietes assignment = new UserSocietes();
    assignment.setUserId(request.getUserId());
    assignment.setSocieteId(request.getSocieteId());
    assignment.setRole(role.getCode());
    assignment.setDateDebut(request.getDateDebut());
    assignment.setDateFin(request.getDateFin());

    UserSocietes created = userSocietesRepository.insert(assignment);
    log.info(
        "Utilisateur {} assigne a la societe {} avec le rôle {}",
        request.getUserId(),
        request.getSocieteId(),
        role.getCode());
    return toDto(created);
  }

  @Transactional
  public UserSocietesDto updateRole(Long id, String newRole) {
    SocieteRole role = SocieteRole.fromCode(newRole);

    UserSocietes existing =
        userSocietesRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assignation non trouvee: " + id));

    // Verifier la contrainte MANAGER unique si changement vers MANAGER
    if (role == SocieteRole.MANAGER && !"MANAGER".equals(existing.getRole())) {
      if (userSocietesRepository.existsActiveManagerBySocieteId(existing.getSocieteId())) {
        throw new IllegalArgumentException("Cette societe a deja un manager actif");
      }
    }

    existing.setRole(role.getCode());
    UserSocietes updated = userSocietesRepository.update(existing);
    log.info("Rôle de l'utilisateur {} mis a jour: {}", existing.getUserId(), role.getCode());
    return toDto(updated);
  }

  @Transactional
  public void deactivate(Long id) {
    UserSocietes existing =
        userSocietesRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assignation non trouvee: " + id));

    existing.setIsActive(false);
    userSocietesRepository.update(existing);
    log.info("Assignation desactivee pour l'utilisateur {}", existing.getUserId());
  }

  @Transactional
  public void delete(Long id) {
    if (userSocietesRepository.findById(id).isEmpty()) {
      throw new IllegalArgumentException("Assignation non trouvee: " + id);
    }
    userSocietesRepository.delete(id);
    log.info("Assignation supprimee: ID {}", id);
  }

  @Transactional(readOnly = true)
  public UserSocietesDto findById(Long id) {
    return userSocietesRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new IllegalArgumentException("Assignation non trouvee: " + id));
  }

  @Transactional(readOnly = true)
  public UserSocietesDto findByUserId(Long userId) {
    return userSocietesRepository
        .findByUserId(userId)
        .map(this::toDto)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Aucune assignation trouvee pour l'utilisateur: " + userId));
  }

  @Transactional(readOnly = true)
  public UserSocietesDto findActiveByUserId(Long userId) {
    return userSocietesRepository.findActiveByUserId(userId).map(this::toDto).orElse(null);
  }

  @Transactional(readOnly = true)
  public List<UserSocietesDto> findBySocieteId(Long societeId) {
    return userSocietesRepository.findBySocieteId(societeId).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<UserSocietesDto> findActiveBySocieteId(Long societeId) {
    return userSocietesRepository.findActiveBySocieteId(societeId).stream()
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public UserSocietesDto findManagerBySocieteId(Long societeId) {
    return userSocietesRepository.findManagerBySocieteId(societeId).map(this::toDto).orElse(null);
  }

  @Transactional(readOnly = true)
  public SocieteDto findSocieteByUserId(Long userId) {
    return userSocietesRepository.findSocieteByUserId(userId).map(this::toSocieteDto).orElse(null);
  }

  private UserSocietesDto toDto(UserSocietes entity) {
    return UserSocietesDto.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .societeId(entity.getSocieteId())
        .role(entity.getRole())
        .dateDebut(entity.getDateDebut())
        .dateFin(entity.getDateFin())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  private SocieteDto toSocieteDto(Societes entity) {
    return SocieteDto.builder()
        .id(entity.getId())
        .raisonSociale(entity.getRaisonSociale())
        .matriculeFiscale(entity.getMatriculeFiscale())
        .codeTva(entity.getCodeTva())
        .codeDouane(entity.getCodeDouane())
        .registreCommerce(entity.getRegistreCommerce())
        .formeJuridique(entity.getFormeJuridique())
        .capitalSocial(entity.getCapitalSocial())
        .dateCreation(entity.getDateCreation())
        .adresse(entity.getAdresse())
        .ville(entity.getVille())
        .codePostal(entity.getCodePostal())
        .telephone(entity.getTelephone())
        .fax(entity.getFax())
        .email(entity.getEmail())
        .siteWeb(entity.getSiteWeb())
        .activite(entity.getActivite())
        .secteur(entity.getSecteur())
        .societeComptableId(entity.getSocieteComptableId())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
