package tn.cyberious.compta.referentiel.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.referentiel.dto.FournisseurRequest;
import tn.cyberious.compta.referentiel.dto.FournisseurResponse;
import tn.cyberious.compta.referentiel.generated.tables.pojos.Fournisseurs;
import tn.cyberious.compta.referentiel.repository.FournisseurRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FournisseurService {

  private final FournisseurRepository fournisseurRepository;

  public FournisseurResponse create(FournisseurRequest request, Long companyId) {
    log.info("Creating fournisseur for company: {}", companyId);

    if (fournisseurRepository.existsByCodeAndEntrepriseId(request.code(), companyId)) {
      throw new RuntimeException(
          "Fournisseur with code " + request.code() + " already exists for this company");
    }

    Fournisseurs fournisseur = new Fournisseurs();
    fournisseur.setCode(request.code());
    fournisseur.setRaisonSociale(request.raisonSociale());
    fournisseur.setMatriculeFiscal(request.matriculeFiscal());
    fournisseur.setAdresse(request.adresse());
    fournisseur.setVille(request.ville());
    fournisseur.setCodePostal(request.codePostal());
    fournisseur.setTelephone(request.telephone());
    fournisseur.setEmail(request.email());
    fournisseur.setEntrepriseId(companyId);
    fournisseur.setActif(request.actif() != null ? request.actif() : true);

    Fournisseurs saved = fournisseurRepository.insert(fournisseur);
    return toResponse(saved);
  }

  public FournisseurResponse update(Long id, FournisseurRequest request, Long companyId) {
    log.info("Updating fournisseur {} for company: {}", id, companyId);

    Fournisseurs fournisseur =
        fournisseurRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur not found"));

    if (!fournisseur.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Fournisseur does not belong to this company");
    }

    fournisseur.setCode(request.code());
    fournisseur.setRaisonSociale(request.raisonSociale());
    fournisseur.setMatriculeFiscal(request.matriculeFiscal());
    fournisseur.setAdresse(request.adresse());
    fournisseur.setVille(request.ville());
    fournisseur.setCodePostal(request.codePostal());
    fournisseur.setTelephone(request.telephone());
    fournisseur.setEmail(request.email());
    fournisseur.setActif(request.actif());

    Fournisseurs updated = fournisseurRepository.update(fournisseur);
    return toResponse(updated);
  }

  public void delete(Long id, Long companyId) {
    log.info("Deleting fournisseur {} for company: {}", id, companyId);

    Fournisseurs fournisseur =
        fournisseurRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur not found"));

    if (!fournisseur.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Fournisseur does not belong to this company");
    }

    fournisseurRepository.delete(id);
  }

  public FournisseurResponse getById(Long id, Long companyId) {
    log.info("Getting fournisseur {} for company: {}", id, companyId);

    Fournisseurs fournisseur =
        fournisseurRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur not found"));

    if (!fournisseur.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Fournisseur does not belong to this company");
    }

    return toResponse(fournisseur);
  }

  public List<FournisseurResponse> getAllByCompany(Long companyId) {
    log.info("Getting all fournisseurs for company: {}", companyId);
    return fournisseurRepository.findAllByEntrepriseId(companyId).stream()
        .map(this::toResponse)
        .toList();
  }

  private FournisseurResponse toResponse(Fournisseurs fournisseur) {
    return new FournisseurResponse(
        fournisseur.getId(),
        fournisseur.getCode(),
        fournisseur.getRaisonSociale(),
        fournisseur.getMatriculeFiscal(),
        fournisseur.getAdresse(),
        fournisseur.getVille(),
        fournisseur.getCodePostal(),
        fournisseur.getTelephone(),
        fournisseur.getEmail(),
        fournisseur.getEntrepriseId(),
        fournisseur.getActif(),
        fournisseur.getCreatedAt(),
        fournisseur.getUpdatedAt());
  }
}
