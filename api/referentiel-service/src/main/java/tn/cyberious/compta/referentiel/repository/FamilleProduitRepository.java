package tn.cyberious.compta.referentiel.repository;

import static tn.cyberious.compta.referentiel.generated.Tables.FAMILLES_PRODUITS;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.referentiel.generated.tables.pojos.FamillesProduits;
import tn.cyberious.compta.referentiel.generated.tables.records.FamillesProduitsRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FamilleProduitRepository {

  private final DSLContext dsl;

  public FamillesProduits insert(FamillesProduits famille) {
    log.debug("Inserting famille produit: {}", famille.getCode());

    FamillesProduitsRecord record =
        dsl.insertInto(FAMILLES_PRODUITS)
            .set(FAMILLES_PRODUITS.CODE, famille.getCode())
            .set(FAMILLES_PRODUITS.LIBELLE, famille.getLibelle())
            .set(FAMILLES_PRODUITS.DESCRIPTION, famille.getDescription())
            .set(FAMILLES_PRODUITS.ENTREPRISE_ID, famille.getEntrepriseId())
            .set(FAMILLES_PRODUITS.ACTIF, famille.getActif() != null ? famille.getActif() : true)
            .returning()
            .fetchOne();

    return record != null ? record.into(FamillesProduits.class) : null;
  }

  public FamillesProduits update(FamillesProduits famille) {
    log.debug("Updating famille produit: {}", famille.getId());

    FamillesProduitsRecord record =
        dsl.update(FAMILLES_PRODUITS)
            .set(FAMILLES_PRODUITS.CODE, famille.getCode())
            .set(FAMILLES_PRODUITS.LIBELLE, famille.getLibelle())
            .set(FAMILLES_PRODUITS.DESCRIPTION, famille.getDescription())
            .set(FAMILLES_PRODUITS.ACTIF, famille.getActif())
            .where(FAMILLES_PRODUITS.ID.eq(famille.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(FamillesProduits.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting famille produit: {}", id);
    int deleted = dsl.deleteFrom(FAMILLES_PRODUITS).where(FAMILLES_PRODUITS.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<FamillesProduits> findById(Long id) {
    log.debug("Finding famille produit by id: {}", id);
    return dsl.selectFrom(FAMILLES_PRODUITS)
        .where(FAMILLES_PRODUITS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(FamillesProduits.class));
  }

  public Optional<FamillesProduits> findByCodeAndEntrepriseId(String code, Long entrepriseId) {
    log.debug("Finding famille produit by code: {} and entreprise: {}", code, entrepriseId);
    return dsl.selectFrom(FAMILLES_PRODUITS)
        .where(
            FAMILLES_PRODUITS.CODE.eq(code).and(FAMILLES_PRODUITS.ENTREPRISE_ID.eq(entrepriseId)))
        .fetchOptional()
        .map(record -> record.into(FamillesProduits.class));
  }

  public List<FamillesProduits> findAllByEntrepriseId(Long entrepriseId) {
    log.debug("Finding all familles produits for entreprise: {}", entrepriseId);
    return dsl.selectFrom(FAMILLES_PRODUITS)
        .where(FAMILLES_PRODUITS.ENTREPRISE_ID.eq(entrepriseId))
        .fetch()
        .into(FamillesProduits.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if famille produit exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(FAMILLES_PRODUITS).where(FAMILLES_PRODUITS.ID.eq(id)));
  }

  public boolean existsByCodeAndEntrepriseId(String code, Long entrepriseId) {
    return dsl.fetchExists(
        dsl.selectFrom(FAMILLES_PRODUITS)
            .where(
                FAMILLES_PRODUITS
                    .CODE
                    .eq(code)
                    .and(FAMILLES_PRODUITS.ENTREPRISE_ID.eq(entrepriseId))));
  }
}
