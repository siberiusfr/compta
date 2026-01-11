package tn.cyberious.compta.authz.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.authz.dto.SocieteDto;
import tn.cyberious.compta.authz.dto.request.CreateSocieteRequest;
import tn.cyberious.compta.authz.dto.request.UpdateSocieteRequest;
import tn.cyberious.compta.authz.generated.tables.pojos.Societes;
import tn.cyberious.compta.authz.repository.SocieteComptableRepository;
import tn.cyberious.compta.authz.repository.SocieteRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocieteService {

  private final SocieteRepository societeRepository;
  private final SocieteComptableRepository societeComptableRepository;

  @Transactional
  public SocieteDto create(CreateSocieteRequest request) {
    if (societeRepository.existsByMatriculeFiscale(request.getMatriculeFiscale())) {
      throw new IllegalArgumentException(
          "Une societe avec ce matricule fiscale existe deja: " + request.getMatriculeFiscale());
    }

    if (request.getSocieteComptableId() != null
        && !societeComptableRepository.existsById(request.getSocieteComptableId())) {
      throw new IllegalArgumentException(
          "Societe comptable non trouvee: " + request.getSocieteComptableId());
    }

    Societes societe = new Societes();
    societe.setRaisonSociale(request.getRaisonSociale());
    societe.setMatriculeFiscale(request.getMatriculeFiscale());
    societe.setCodeTva(request.getCodeTva());
    societe.setCodeDouane(request.getCodeDouane());
    societe.setRegistreCommerce(request.getRegistreCommerce());
    societe.setFormeJuridique(request.getFormeJuridique());
    societe.setCapitalSocial(request.getCapitalSocial());
    societe.setDateCreation(request.getDateCreation());
    societe.setAdresse(request.getAdresse());
    societe.setVille(request.getVille());
    societe.setCodePostal(request.getCodePostal());
    societe.setTelephone(request.getTelephone());
    societe.setFax(request.getFax());
    societe.setEmail(request.getEmail());
    societe.setSiteWeb(request.getSiteWeb());
    societe.setActivite(request.getActivite());
    societe.setSecteur(request.getSecteur());
    societe.setSocieteComptableId(request.getSocieteComptableId());

    Societes created = societeRepository.insert(societe);
    log.info("Societe creee: {} (ID: {})", created.getRaisonSociale(), created.getId());
    return toDto(created);
  }

  @Transactional
  public SocieteDto update(Long id, UpdateSocieteRequest request) {
    Societes existing =
        societeRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Societe non trouvee: " + id));

    if (request.getSocieteComptableId() != null
        && !societeComptableRepository.existsById(request.getSocieteComptableId())) {
      throw new IllegalArgumentException(
          "Societe comptable non trouvee: " + request.getSocieteComptableId());
    }

    if (request.getRaisonSociale() != null) {
      existing.setRaisonSociale(request.getRaisonSociale());
    }
    if (request.getCodeTva() != null) {
      existing.setCodeTva(request.getCodeTva());
    }
    if (request.getCodeDouane() != null) {
      existing.setCodeDouane(request.getCodeDouane());
    }
    if (request.getRegistreCommerce() != null) {
      existing.setRegistreCommerce(request.getRegistreCommerce());
    }
    if (request.getFormeJuridique() != null) {
      existing.setFormeJuridique(request.getFormeJuridique());
    }
    if (request.getCapitalSocial() != null) {
      existing.setCapitalSocial(request.getCapitalSocial());
    }
    if (request.getDateCreation() != null) {
      existing.setDateCreation(request.getDateCreation());
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
    if (request.getFax() != null) {
      existing.setFax(request.getFax());
    }
    if (request.getEmail() != null) {
      existing.setEmail(request.getEmail());
    }
    if (request.getSiteWeb() != null) {
      existing.setSiteWeb(request.getSiteWeb());
    }
    if (request.getActivite() != null) {
      existing.setActivite(request.getActivite());
    }
    if (request.getSecteur() != null) {
      existing.setSecteur(request.getSecteur());
    }
    if (request.getSocieteComptableId() != null) {
      existing.setSocieteComptableId(request.getSocieteComptableId());
    }
    if (request.getIsActive() != null) {
      existing.setIsActive(request.getIsActive());
    }

    Societes updated = societeRepository.update(existing);
    log.info("Societe mise a jour: {} (ID: {})", updated.getRaisonSociale(), updated.getId());
    return toDto(updated);
  }

  @Transactional
  public void delete(Long id) {
    if (!societeRepository.existsById(id)) {
      throw new IllegalArgumentException("Societe non trouvee: " + id);
    }
    societeRepository.delete(id);
    log.info("Societe supprimee: ID {}", id);
  }

  @Transactional(readOnly = true)
  public SocieteDto findById(Long id) {
    return societeRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new IllegalArgumentException("Societe non trouvee: " + id));
  }

  @Transactional(readOnly = true)
  public SocieteDto findByMatriculeFiscale(String matriculeFiscale) {
    return societeRepository
        .findByMatriculeFiscale(matriculeFiscale)
        .map(this::toDto)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Societe non trouvee avec matricule: " + matriculeFiscale));
  }

  @Transactional(readOnly = true)
  public List<SocieteDto> findAll() {
    return societeRepository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteDto> findAllActive() {
    return societeRepository.findAllActive().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteDto> findBySocieteComptableId(Long societeComptableId) {
    return societeRepository.findBySocieteComptableId(societeComptableId).stream()
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteDto> findActiveBySocieteComptableId(Long societeComptableId) {
    return societeRepository.findActiveBySocieteComptableId(societeComptableId).stream()
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteDto> search(String searchTerm) {
    return societeRepository.searchByRaisonSociale(searchTerm).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<SocieteDto> findBySecteur(String secteur) {
    return societeRepository.findBySecteur(secteur).stream().map(this::toDto).toList();
  }

  private SocieteDto toDto(Societes entity) {
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
