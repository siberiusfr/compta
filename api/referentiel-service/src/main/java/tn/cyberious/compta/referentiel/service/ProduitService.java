package tn.cyberious.compta.referentiel.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.referentiel.dto.ProduitRequest;
import tn.cyberious.compta.referentiel.dto.ProduitResponse;
import tn.cyberious.compta.referentiel.generated.tables.pojos.Produits;
import tn.cyberious.compta.referentiel.repository.ProduitRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProduitService {

  private final ProduitRepository produitRepository;

  public ProduitResponse create(ProduitRequest request, Long companyId) {
    log.info("Creating produit for company: {}", companyId);

    if (produitRepository.existsByReferenceAndEntrepriseId(request.reference(), companyId)) {
      throw new RuntimeException(
          "Produit with reference " + request.reference() + " already exists for this company");
    }

    Produits produit = new Produits();
    produit.setReference(request.reference());
    produit.setDesignation(request.designation());
    produit.setDescription(request.description());
    produit.setPrixAchat(request.prixAchat());
    produit.setPrixVente(request.prixVente());
    produit.setTauxTva(request.tauxTva());
    produit.setUnite(request.unite());
    produit.setTypeStock(request.typeStock());
    produit.setTypeArticle(request.typeArticle());
    produit.setFamilleId(request.familleId());
    produit.setEntrepriseId(companyId);
    produit.setActif(request.actif() != null ? request.actif() : true);

    Produits saved = produitRepository.insert(produit);
    return toResponse(saved);
  }

  public ProduitResponse update(Long id, ProduitRequest request, Long companyId) {
    log.info("Updating produit {} for company: {}", id, companyId);

    Produits produit =
        produitRepository.findById(id).orElseThrow(() -> new RuntimeException("Produit not found"));

    if (!produit.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Produit does not belong to this company");
    }

    produit.setReference(request.reference());
    produit.setDesignation(request.designation());
    produit.setDescription(request.description());
    produit.setPrixAchat(request.prixAchat());
    produit.setPrixVente(request.prixVente());
    produit.setTauxTva(request.tauxTva());
    produit.setUnite(request.unite());
    produit.setTypeStock(request.typeStock());
    produit.setTypeArticle(request.typeArticle());
    produit.setFamilleId(request.familleId());
    produit.setActif(request.actif());

    Produits updated = produitRepository.update(produit);
    return toResponse(updated);
  }

  public void delete(Long id, Long companyId) {
    log.info("Deleting produit {} for company: {}", id, companyId);

    Produits produit =
        produitRepository.findById(id).orElseThrow(() -> new RuntimeException("Produit not found"));

    if (!produit.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Produit does not belong to this company");
    }

    produitRepository.delete(id);
  }

  public ProduitResponse getById(Long id, Long companyId) {
    log.info("Getting produit {} for company: {}", id, companyId);

    Produits produit =
        produitRepository.findById(id).orElseThrow(() -> new RuntimeException("Produit not found"));

    if (!produit.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Produit does not belong to this company");
    }

    return toResponse(produit);
  }

  public List<ProduitResponse> getAllByCompany(Long companyId) {
    log.info("Getting all produits for company: {}", companyId);
    return produitRepository.findAllByEntrepriseId(companyId).stream()
        .map(this::toResponse)
        .toList();
  }

  public List<ProduitResponse> getByFamilleAndCompany(Long familleId, Long companyId) {
    log.info("Getting produits by famille {} for company: {}", familleId, companyId);
    return produitRepository.findByFamilleIdAndEntrepriseId(familleId, companyId).stream()
        .map(this::toResponse)
        .toList();
  }

  private ProduitResponse toResponse(Produits produit) {
    return new ProduitResponse(
        produit.getId(),
        produit.getReference(),
        produit.getDesignation(),
        produit.getDescription(),
        produit.getPrixAchat(),
        produit.getPrixVente(),
        produit.getTauxTva(),
        produit.getUnite(),
        produit.getTypeStock(),
        produit.getTypeArticle(),
        produit.getFamilleId(),
        produit.getEntrepriseId(),
        produit.getActif(),
        produit.getCreatedAt(),
        produit.getUpdatedAt());
  }
}
