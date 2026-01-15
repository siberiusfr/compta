package tn.cyberious.compta.referentiel.repository;

import static tn.cyberious.compta.referentiel.generated.Tables.PRODUITS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.referentiel.generated.tables.pojos.Produits;
import tn.cyberious.compta.referentiel.generated.tables.records.ProduitsRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProduitRepository {

  private final DSLContext dsl;

  public Produits insert(Produits produit) {
    log.debug("Inserting produit: {}", produit.getReference());

    ProduitsRecord record =
        dsl.insertInto(PRODUITS)
            .set(PRODUITS.REFERENCE, produit.getReference())
            .set(PRODUITS.DESIGNATION, produit.getDesignation())
            .set(PRODUITS.DESCRIPTION, produit.getDescription())
            .set(PRODUITS.PRIX_ACHAT, produit.getPrixAchat())
            .set(PRODUITS.PRIX_VENTE, produit.getPrixVente())
            .set(
                PRODUITS.TAUX_TVA,
                produit.getTauxTva() != null ? produit.getTauxTva() : new BigDecimal("19.00"))
            .set(PRODUITS.UNITE, produit.getUnite() != null ? produit.getUnite() : "U")
            .set(PRODUITS.TYPE_STOCK, produit.getTypeStock())
            .set(PRODUITS.TYPE_ARTICLE, produit.getTypeArticle())
            .set(PRODUITS.FAMILLE_ID, produit.getFamilleId())
            .set(PRODUITS.ENTREPRISE_ID, produit.getEntrepriseId())
            .set(PRODUITS.ACTIF, produit.getActif() != null ? produit.getActif() : true)
            .returning()
            .fetchOne();

    return record != null ? record.into(Produits.class) : null;
  }

  public Produits update(Produits produit) {
    log.debug("Updating produit: {}", produit.getId());

    ProduitsRecord record =
        dsl.update(PRODUITS)
            .set(PRODUITS.REFERENCE, produit.getReference())
            .set(PRODUITS.DESIGNATION, produit.getDesignation())
            .set(PRODUITS.DESCRIPTION, produit.getDescription())
            .set(PRODUITS.PRIX_ACHAT, produit.getPrixAchat())
            .set(PRODUITS.PRIX_VENTE, produit.getPrixVente())
            .set(PRODUITS.TAUX_TVA, produit.getTauxTva())
            .set(PRODUITS.UNITE, produit.getUnite())
            .set(PRODUITS.TYPE_STOCK, produit.getTypeStock())
            .set(PRODUITS.TYPE_ARTICLE, produit.getTypeArticle())
            .set(PRODUITS.FAMILLE_ID, produit.getFamilleId())
            .set(PRODUITS.ACTIF, produit.getActif())
            .where(PRODUITS.ID.eq(produit.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Produits.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting produit: {}", id);
    int deleted = dsl.deleteFrom(PRODUITS).where(PRODUITS.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Produits> findById(Long id) {
    log.debug("Finding produit by id: {}", id);
    return dsl.selectFrom(PRODUITS)
        .where(PRODUITS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Produits.class));
  }

  public Optional<Produits> findByReferenceAndEntrepriseId(String reference, Long entrepriseId) {
    log.debug("Finding produit by reference: {} and entreprise: {}", reference, entrepriseId);
    return dsl.selectFrom(PRODUITS)
        .where(PRODUITS.REFERENCE.eq(reference).and(PRODUITS.ENTREPRISE_ID.eq(entrepriseId)))
        .fetchOptional()
        .map(record -> record.into(Produits.class));
  }

  public List<Produits> findAllByEntrepriseId(Long entrepriseId) {
    log.debug("Finding all produits for entreprise: {}", entrepriseId);
    return dsl.selectFrom(PRODUITS)
        .where(PRODUITS.ENTREPRISE_ID.eq(entrepriseId))
        .fetch()
        .into(Produits.class);
  }

  public List<Produits> findByFamilleIdAndEntrepriseId(Long familleId, Long entrepriseId) {
    log.debug("Finding produits by famille: {} and entreprise: {}", familleId, entrepriseId);
    return dsl.selectFrom(PRODUITS)
        .where(PRODUITS.FAMILLE_ID.eq(familleId).and(PRODUITS.ENTREPRISE_ID.eq(entrepriseId)))
        .fetch()
        .into(Produits.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if produit exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(PRODUITS).where(PRODUITS.ID.eq(id)));
  }

  public boolean existsByReferenceAndEntrepriseId(String reference, Long entrepriseId) {
    return dsl.fetchExists(
        dsl.selectFrom(PRODUITS)
            .where(PRODUITS.REFERENCE.eq(reference).and(PRODUITS.ENTREPRISE_ID.eq(entrepriseId))));
  }
}
