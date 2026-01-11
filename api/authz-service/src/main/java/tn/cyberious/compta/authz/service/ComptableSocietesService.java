package tn.cyberious.compta.authz.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.authz.dto.ComptableSocietesDto;
import tn.cyberious.compta.authz.dto.SocieteDto;
import tn.cyberious.compta.authz.dto.request.AssignComptableToSocieteRequest;
import tn.cyberious.compta.authz.dto.request.UpdateComptableSocieteAccessRequest;
import tn.cyberious.compta.authz.generated.tables.pojos.ComptableSocietes;
import tn.cyberious.compta.authz.generated.tables.pojos.Societes;
import tn.cyberious.compta.authz.repository.ComptableSocietesRepository;
import tn.cyberious.compta.authz.repository.SocieteRepository;
import tn.cyberious.compta.authz.repository.UserSocieteComptableRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComptableSocietesService {

  private final ComptableSocietesRepository comptableSocietesRepository;
  private final SocieteRepository societeRepository;
  private final UserSocieteComptableRepository userSocieteComptableRepository;

  @Transactional
  public ComptableSocietesDto assignComptableToSociete(AssignComptableToSocieteRequest request) {
    // Verifier que l'utilisateur est bien un comptable (appartient a une societe comptable)
    if (!userSocieteComptableRepository.existsByUserId(request.getUserId())) {
      throw new IllegalArgumentException("L'utilisateur n'appartient pas a une societe comptable");
    }

    // Verifier que la societe cliente existe
    if (!societeRepository.existsById(request.getSocieteId())) {
      throw new IllegalArgumentException("Societe non trouvee: " + request.getSocieteId());
    }

    // Verifier si l'acces existe deja
    if (comptableSocietesRepository.existsByUserIdAndSocieteId(
        request.getUserId(), request.getSocieteId())) {
      throw new IllegalArgumentException(
          "L'acces existe deja pour cet utilisateur sur cette societe");
    }

    ComptableSocietes access = new ComptableSocietes();
    access.setUserId(request.getUserId());
    access.setSocieteId(request.getSocieteId());
    access.setCanRead(request.getCanRead());
    access.setCanWrite(request.getCanWrite());
    access.setCanValidate(request.getCanValidate());
    access.setDateDebut(request.getDateDebut());
    access.setDateFin(request.getDateFin());

    ComptableSocietes created = comptableSocietesRepository.insert(access);
    log.info(
        "Acces cree pour le comptable {} sur la societe {}",
        request.getUserId(),
        request.getSocieteId());
    return toDto(created);
  }

  @Transactional
  public ComptableSocietesDto updateAccess(Long id, UpdateComptableSocieteAccessRequest request) {
    ComptableSocietes existing =
        comptableSocietesRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Acces non trouve: " + id));

    if (request.getCanRead() != null) {
      existing.setCanRead(request.getCanRead());
    }
    if (request.getCanWrite() != null) {
      existing.setCanWrite(request.getCanWrite());
    }
    if (request.getCanValidate() != null) {
      existing.setCanValidate(request.getCanValidate());
    }
    if (request.getDateFin() != null) {
      existing.setDateFin(request.getDateFin());
    }
    if (request.getIsActive() != null) {
      existing.setIsActive(request.getIsActive());
    }

    ComptableSocietes updated = comptableSocietesRepository.update(existing);
    log.info(
        "Acces mis a jour pour le comptable {} sur la societe {}",
        existing.getUserId(),
        existing.getSocieteId());
    return toDto(updated);
  }

  @Transactional
  public void revokeAccess(Long userId, Long societeId) {
    if (!comptableSocietesRepository.existsByUserIdAndSocieteId(userId, societeId)) {
      throw new IllegalArgumentException(
          "Acces non trouve pour l'utilisateur " + userId + " sur la societe " + societeId);
    }
    comptableSocietesRepository.deleteByUserIdAndSocieteId(userId, societeId);
    log.info("Acces revoque pour le comptable {} sur la societe {}", userId, societeId);
  }

  @Transactional(readOnly = true)
  public ComptableSocietesDto findById(Long id) {
    return comptableSocietesRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new IllegalArgumentException("Acces non trouve: " + id));
  }

  @Transactional(readOnly = true)
  public ComptableSocietesDto findByUserIdAndSocieteId(Long userId, Long societeId) {
    return comptableSocietesRepository
        .findByUserIdAndSocieteId(userId, societeId)
        .map(this::toDto)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Acces non trouve pour l'utilisateur "
                        + userId
                        + " sur la societe "
                        + societeId));
  }

  @Transactional(readOnly = true)
  public List<ComptableSocietesDto> findByUserId(Long userId) {
    return comptableSocietesRepository.findByUserId(userId).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<ComptableSocietesDto> findActiveByUserId(Long userId) {
    return comptableSocietesRepository.findActiveByUserId(userId).stream()
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ComptableSocietesDto> findBySocieteId(Long societeId) {
    return comptableSocietesRepository.findBySocieteId(societeId).stream()
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteDto> findSocietesByUserId(Long userId) {
    return comptableSocietesRepository.findSocietesByUserId(userId).stream()
        .map(this::toSocieteDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteDto> findSocietesWithWriteAccessByUserId(Long userId) {
    return comptableSocietesRepository.findSocietesWithWriteAccessByUserId(userId).stream()
        .map(this::toSocieteDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public boolean hasAccess(Long userId, Long societeId) {
    return comptableSocietesRepository.hasActiveAccess(userId, societeId);
  }

  @Transactional(readOnly = true)
  public boolean hasWriteAccess(Long userId, Long societeId) {
    return comptableSocietesRepository.hasWriteAccess(userId, societeId);
  }

  @Transactional(readOnly = true)
  public boolean hasValidateAccess(Long userId, Long societeId) {
    return comptableSocietesRepository.hasValidateAccess(userId, societeId);
  }

  private ComptableSocietesDto toDto(ComptableSocietes entity) {
    return ComptableSocietesDto.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .societeId(entity.getSocieteId())
        .canRead(entity.getCanRead())
        .canWrite(entity.getCanWrite())
        .canValidate(entity.getCanValidate())
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
