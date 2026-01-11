package tn.cyberious.compta.authz.repository;

import static tn.cyberious.compta.authz.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.authz.generated.tables.pojos.SocietesComptables;
import tn.cyberious.compta.authz.generated.tables.records.SocietesComptablesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SocieteComptableRepository {

  private final DSLContext dsl;

  public SocietesComptables insert(SocietesComptables societe) {
    SocietesComptablesRecord record =
        dsl.insertInto(SOCIETES_COMPTABLES)
            .set(SOCIETES_COMPTABLES.RAISON_SOCIALE, societe.getRaisonSociale())
            .set(SOCIETES_COMPTABLES.MATRICULE_FISCALE, societe.getMatriculeFiscale())
            .set(SOCIETES_COMPTABLES.CODE_TVA, societe.getCodeTva())
            .set(SOCIETES_COMPTABLES.ADRESSE, societe.getAdresse())
            .set(SOCIETES_COMPTABLES.VILLE, societe.getVille())
            .set(SOCIETES_COMPTABLES.CODE_POSTAL, societe.getCodePostal())
            .set(SOCIETES_COMPTABLES.TELEPHONE, societe.getTelephone())
            .set(SOCIETES_COMPTABLES.EMAIL, societe.getEmail())
            .set(SOCIETES_COMPTABLES.SITE_WEB, societe.getSiteWeb())
            .set(SOCIETES_COMPTABLES.IS_ACTIVE, true)
            .set(SOCIETES_COMPTABLES.CREATED_AT, LocalDateTime.now())
            .set(SOCIETES_COMPTABLES.UPDATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();
    return record != null ? record.into(SocietesComptables.class) : null;
  }

  public SocietesComptables update(SocietesComptables societe) {
    SocietesComptablesRecord record =
        dsl.update(SOCIETES_COMPTABLES)
            .set(SOCIETES_COMPTABLES.RAISON_SOCIALE, societe.getRaisonSociale())
            .set(SOCIETES_COMPTABLES.CODE_TVA, societe.getCodeTva())
            .set(SOCIETES_COMPTABLES.ADRESSE, societe.getAdresse())
            .set(SOCIETES_COMPTABLES.VILLE, societe.getVille())
            .set(SOCIETES_COMPTABLES.CODE_POSTAL, societe.getCodePostal())
            .set(SOCIETES_COMPTABLES.TELEPHONE, societe.getTelephone())
            .set(SOCIETES_COMPTABLES.EMAIL, societe.getEmail())
            .set(SOCIETES_COMPTABLES.SITE_WEB, societe.getSiteWeb())
            .set(SOCIETES_COMPTABLES.IS_ACTIVE, societe.getIsActive())
            .set(SOCIETES_COMPTABLES.UPDATED_AT, LocalDateTime.now())
            .where(SOCIETES_COMPTABLES.ID.eq(societe.getId()))
            .returning()
            .fetchOne();
    return record != null ? record.into(SocietesComptables.class) : null;
  }

  public boolean delete(Long id) {
    return dsl.deleteFrom(SOCIETES_COMPTABLES).where(SOCIETES_COMPTABLES.ID.eq(id)).execute() > 0;
  }

  public Optional<SocietesComptables> findById(Long id) {
    return dsl.selectFrom(SOCIETES_COMPTABLES)
        .where(SOCIETES_COMPTABLES.ID.eq(id))
        .fetchOptional()
        .map(r -> r.into(SocietesComptables.class));
  }

  public Optional<SocietesComptables> findByMatriculeFiscale(String matriculeFiscale) {
    return dsl.selectFrom(SOCIETES_COMPTABLES)
        .where(SOCIETES_COMPTABLES.MATRICULE_FISCALE.eq(matriculeFiscale))
        .fetchOptional()
        .map(r -> r.into(SocietesComptables.class));
  }

  public List<SocietesComptables> findAll() {
    return dsl.selectFrom(SOCIETES_COMPTABLES)
        .orderBy(SOCIETES_COMPTABLES.RAISON_SOCIALE)
        .fetch()
        .into(SocietesComptables.class);
  }

  public List<SocietesComptables> findAllActive() {
    return dsl.selectFrom(SOCIETES_COMPTABLES)
        .where(SOCIETES_COMPTABLES.IS_ACTIVE.eq(true))
        .orderBy(SOCIETES_COMPTABLES.RAISON_SOCIALE)
        .fetch()
        .into(SocietesComptables.class);
  }

  public List<SocietesComptables> searchByRaisonSociale(String searchTerm) {
    return dsl.selectFrom(SOCIETES_COMPTABLES)
        .where(SOCIETES_COMPTABLES.RAISON_SOCIALE.likeIgnoreCase("%" + searchTerm + "%"))
        .orderBy(SOCIETES_COMPTABLES.RAISON_SOCIALE)
        .fetch()
        .into(SocietesComptables.class);
  }

  public boolean existsById(Long id) {
    return dsl.fetchExists(
        dsl.selectOne().from(SOCIETES_COMPTABLES).where(SOCIETES_COMPTABLES.ID.eq(id)));
  }

  public boolean existsByMatriculeFiscale(String matriculeFiscale) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(SOCIETES_COMPTABLES)
            .where(SOCIETES_COMPTABLES.MATRICULE_FISCALE.eq(matriculeFiscale)));
  }

  public long countSocietesByComptableId(Long societeComptableId) {
    return dsl.selectCount()
        .from(SOCIETES)
        .where(SOCIETES.SOCIETE_COMPTABLE_ID.eq(societeComptableId))
        .fetchOne(0, Long.class);
  }
}
