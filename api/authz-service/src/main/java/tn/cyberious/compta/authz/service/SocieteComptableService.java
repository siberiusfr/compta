package tn.cyberious.compta.authz.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.authz.dto.SocieteComptableDto;
import tn.cyberious.compta.authz.dto.request.CreateSocieteComptableRequest;
import tn.cyberious.compta.authz.dto.request.UpdateSocieteComptableRequest;
import tn.cyberious.compta.authz.generated.tables.pojos.SocietesComptables;
import tn.cyberious.compta.authz.repository.SocieteComptableRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocieteComptableService {

  private final SocieteComptableRepository societeComptableRepository;

  @Transactional
  public SocieteComptableDto create(CreateSocieteComptableRequest request) {
    if (societeComptableRepository.existsByMatriculeFiscale(request.getMatriculeFiscale())) {
      throw new IllegalArgumentException(
          "Une societe comptable avec ce matricule fiscale existe deja: "
              + request.getMatriculeFiscale());
    }

    SocietesComptables societe = new SocietesComptables();
    societe.setRaisonSociale(request.getRaisonSociale());
    societe.setMatriculeFiscale(request.getMatriculeFiscale());
    societe.setCodeTva(request.getCodeTva());
    societe.setAdresse(request.getAdresse());
    societe.setVille(request.getVille());
    societe.setCodePostal(request.getCodePostal());
    societe.setTelephone(request.getTelephone());
    societe.setEmail(request.getEmail());
    societe.setSiteWeb(request.getSiteWeb());

    SocietesComptables created = societeComptableRepository.insert(societe);
    log.info("Societe comptable creee: {} (ID: {})", created.getRaisonSociale(), created.getId());
    return toDto(created);
  }

  @Transactional
  public SocieteComptableDto update(Long id, UpdateSocieteComptableRequest request) {
    SocietesComptables existing =
        societeComptableRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Societe comptable non trouvee: " + id));

    if (request.getRaisonSociale() != null) {
      existing.setRaisonSociale(request.getRaisonSociale());
    }
    if (request.getCodeTva() != null) {
      existing.setCodeTva(request.getCodeTva());
    }
    if (request.getAdresse() != null) {
      existing.setAdresse(request.getAdresse());
    }
    if (request.getVille() != null) {
      existing.setVille(request.getVille());
    }
    if (request.getCodePostal() != null) {
      existing.setCodePostal(request.getCodePostal());
    }
    if (request.getTelephone() != null) {
      existing.setTelephone(request.getTelephone());
    }
    if (request.getEmail() != null) {
      existing.setEmail(request.getEmail());
    }
    if (request.getSiteWeb() != null) {
      existing.setSiteWeb(request.getSiteWeb());
    }
    if (request.getIsActive() != null) {
      existing.setIsActive(request.getIsActive());
    }

    SocietesComptables updated = societeComptableRepository.update(existing);
    log.info(
        "Societe comptable mise a jour: {} (ID: {})", updated.getRaisonSociale(), updated.getId());
    return toDto(updated);
  }

  @Transactional
  public void delete(Long id) {
    if (!societeComptableRepository.existsById(id)) {
      throw new IllegalArgumentException("Societe comptable non trouvee: " + id);
    }

    long societeCount = societeComptableRepository.countSocietesByComptableId(id);
    if (societeCount > 0) {
      throw new IllegalStateException(
          "Impossible de supprimer: cette societe comptable gere "
              + societeCount
              + " societe(s) cliente(s)");
    }

    societeComptableRepository.delete(id);
    log.info("Societe comptable supprimee: ID {}", id);
  }

  @Transactional(readOnly = true)
  public SocieteComptableDto findById(Long id) {
    return societeComptableRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new IllegalArgumentException("Societe comptable non trouvee: " + id));
  }

  @Transactional(readOnly = true)
  public SocieteComptableDto findByMatriculeFiscale(String matriculeFiscale) {
    return societeComptableRepository
        .findByMatriculeFiscale(matriculeFiscale)
        .map(this::toDto)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Societe comptable non trouvee avec matricule: " + matriculeFiscale));
  }

  @Transactional(readOnly = true)
  public List<SocieteComptableDto> findAll() {
    return societeComptableRepository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteComptableDto> findAllActive() {
    return societeComptableRepository.findAllActive().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteComptableDto> search(String searchTerm) {
    return societeComptableRepository.searchByRaisonSociale(searchTerm).stream()
        .map(this::toDto)
        .toList();
  }

  private SocieteComptableDto toDto(SocietesComptables entity) {
    return SocieteComptableDto.builder()
        .id(entity.getId())
        .raisonSociale(entity.getRaisonSociale())
        .matriculeFiscale(entity.getMatriculeFiscale())
        .codeTva(entity.getCodeTva())
        .adresse(entity.getAdresse())
        .ville(entity.getVille())
        .codePostal(entity.getCodePostal())
        .telephone(entity.getTelephone())
        .email(entity.getEmail())
        .siteWeb(entity.getSiteWeb())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
