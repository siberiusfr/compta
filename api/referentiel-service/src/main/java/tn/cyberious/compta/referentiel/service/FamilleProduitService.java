package tn.cyberious.compta.referentiel.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.referentiel.dto.FamilleProduitRequest;
import tn.cyberious.compta.referentiel.dto.FamilleProduitResponse;
import tn.cyberious.compta.referentiel.generated.tables.pojos.FamillesProduits;
import tn.cyberious.compta.referentiel.repository.FamilleProduitRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FamilleProduitService {

  private final FamilleProduitRepository familleProduitRepository;

  public FamilleProduitResponse create(FamilleProduitRequest request, Long companyId) {
    log.info("Creating famille produit for company: {}", companyId);

    if (familleProduitRepository.existsByCodeAndEntrepriseId(request.code(), companyId)) {
      throw new RuntimeException(
          "Famille produit with code " + request.code() + " already exists for this company");
    }

    FamillesProduits famille = new FamillesProduits();
    famille.setCode(request.code());
    famille.setLibelle(request.libelle());
    famille.setDescription(request.description());
    famille.setEntrepriseId(companyId);
    famille.setActif(request.actif() != null ? request.actif() : true);

    FamillesProduits saved = familleProduitRepository.insert(famille);
    return toResponse(saved);
  }

  public FamilleProduitResponse update(Long id, FamilleProduitRequest request, Long companyId) {
    log.info("Updating famille produit {} for company: {}", id, companyId);

    FamillesProduits famille =
        familleProduitRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Famille produit not found"));

    if (!famille.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Famille produit does not belong to this company");
    }

    famille.setCode(request.code());
    famille.setLibelle(request.libelle());
    famille.setDescription(request.description());
    famille.setActif(request.actif());

    FamillesProduits updated = familleProduitRepository.update(famille);
    return toResponse(updated);
  }

  public void delete(Long id, Long companyId) {
    log.info("Deleting famille produit {} for company: {}", id, companyId);

    FamillesProduits famille =
        familleProduitRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Famille produit not found"));

    if (!famille.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Famille produit does not belong to this company");
    }

    familleProduitRepository.delete(id);
  }

  public FamilleProduitResponse getById(Long id, Long companyId) {
    log.info("Getting famille produit {} for company: {}", id, companyId);

    FamillesProduits famille =
        familleProduitRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Famille produit not found"));

    if (!famille.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Famille produit does not belong to this company");
    }

    return toResponse(famille);
  }

  public List<FamilleProduitResponse> getAllByCompany(Long companyId) {
    log.info("Getting all familles produits for company: {}", companyId);
    return familleProduitRepository.findAllByEntrepriseId(companyId).stream()
        .map(this::toResponse)
        .toList();
  }

  private FamilleProduitResponse toResponse(FamillesProduits famille) {
    return new FamilleProduitResponse(
        famille.getId(),
        famille.getCode(),
        famille.getLibelle(),
        famille.getDescription(),
        famille.getEntrepriseId(),
        famille.getActif(),
        famille.getCreatedAt(),
        famille.getUpdatedAt());
  }
}
