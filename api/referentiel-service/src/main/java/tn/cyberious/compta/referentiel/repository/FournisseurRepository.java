package tn.cyberious.compta.referentiel.repository;

import static tn.cyberious.compta.referentiel.generated.Tables.FOURNISSEURS;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.referentiel.generated.tables.pojos.Fournisseurs;
import tn.cyberious.compta.referentiel.generated.tables.records.FournisseursRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FournisseurRepository {

  private final DSLContext dsl;

  public Fournisseurs insert(Fournisseurs fournisseur) {
    log.debug("Inserting fournisseur: {}", fournisseur.getCode());

    FournisseursRecord record =
        dsl.insertInto(FOURNISSEURS)
            .set(FOURNISSEURS.CODE, fournisseur.getCode())
            .set(FOURNISSEURS.RAISON_SOCIALE, fournisseur.getRaisonSociale())
            .set(FOURNISSEURS.MATRICULE_FISCAL, fournisseur.getMatriculeFiscal())
            .set(FOURNISSEURS.ADRESSE, fournisseur.getAdresse())
            .set(FOURNISSEURS.VILLE, fournisseur.getVille())
            .set(FOURNISSEURS.CODE_POSTAL, fournisseur.getCodePostal())
            .set(FOURNISSEURS.TELEPHONE, fournisseur.getTelephone())
            .set(FOURNISSEURS.EMAIL, fournisseur.getEmail())
            .set(FOURNISSEURS.ENTREPRISE_ID, fournisseur.getEntrepriseId())
            .set(FOURNISSEURS.ACTIF, fournisseur.getActif() != null ? fournisseur.getActif() : true)
            .returning()
            .fetchOne();

    return record != null ? record.into(Fournisseurs.class) : null;
  }

  public Fournisseurs update(Fournisseurs fournisseur) {
    log.debug("Updating fournisseur: {}", fournisseur.getId());

    FournisseursRecord record =
        dsl.update(FOURNISSEURS)
            .set(FOURNISSEURS.CODE, fournisseur.getCode())
            .set(FOURNISSEURS.RAISON_SOCIALE, fournisseur.getRaisonSociale())
            .set(FOURNISSEURS.MATRICULE_FISCAL, fournisseur.getMatriculeFiscal())
            .set(FOURNISSEURS.ADRESSE, fournisseur.getAdresse())
            .set(FOURNISSEURS.VILLE, fournisseur.getVille())
            .set(FOURNISSEURS.CODE_POSTAL, fournisseur.getCodePostal())
            .set(FOURNISSEURS.TELEPHONE, fournisseur.getTelephone())
            .set(FOURNISSEURS.EMAIL, fournisseur.getEmail())
            .set(FOURNISSEURS.ACTIF, fournisseur.getActif())
            .where(FOURNISSEURS.ID.eq(fournisseur.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Fournisseurs.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting fournisseur: {}", id);
    int deleted = dsl.deleteFrom(FOURNISSEURS).where(FOURNISSEURS.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Fournisseurs> findById(Long id) {
    log.debug("Finding fournisseur by id: {}", id);
    return dsl.selectFrom(FOURNISSEURS)
        .where(FOURNISSEURS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Fournisseurs.class));
  }

  public Optional<Fournisseurs> findByCodeAndEntrepriseId(String code, Long entrepriseId) {
    log.debug("Finding fournisseur by code: {} and entreprise: {}", code, entrepriseId);
    return dsl.selectFrom(FOURNISSEURS)
        .where(FOURNISSEURS.CODE.eq(code).and(FOURNISSEURS.ENTREPRISE_ID.eq(entrepriseId)))
        .fetchOptional()
        .map(record -> record.into(Fournisseurs.class));
  }

  public List<Fournisseurs> findAllByEntrepriseId(Long entrepriseId) {
    log.debug("Finding all fournisseurs for entreprise: {}", entrepriseId);
    return dsl.selectFrom(FOURNISSEURS)
        .where(FOURNISSEURS.ENTREPRISE_ID.eq(entrepriseId))
        .fetch()
        .into(Fournisseurs.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if fournisseur exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(FOURNISSEURS).where(FOURNISSEURS.ID.eq(id)));
  }

  public boolean existsByCodeAndEntrepriseId(String code, Long entrepriseId) {
    return dsl.fetchExists(
        dsl.selectFrom(FOURNISSEURS)
            .where(FOURNISSEURS.CODE.eq(code).and(FOURNISSEURS.ENTREPRISE_ID.eq(entrepriseId))));
  }
}
