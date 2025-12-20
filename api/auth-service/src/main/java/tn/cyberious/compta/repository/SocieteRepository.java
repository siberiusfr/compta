package tn.cyberious.compta.repository;

import static tn.cyberious.compta.auth.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.auth.generated.tables.pojos.Societes;
import tn.cyberious.compta.auth.generated.tables.records.SocietesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SocieteRepository {

  private final DSLContext dsl;

  public Societes insert(Societes societe) {
    log.debug("Inserting societe: {}", societe.getRaisonSociale());

    SocietesRecord record =
        dsl.insertInto(SOCIETES)
            .set(SOCIETES.RAISON_SOCIALE, societe.getRaisonSociale())
            .set(SOCIETES.MATRICULE_FISCALE, societe.getMatriculeFiscale())
            .set(SOCIETES.CODE_TVA, societe.getCodeTva())
            .set(SOCIETES.CODE_DOUANE, societe.getCodeDouane())
            .set(SOCIETES.REGISTRE_COMMERCE, societe.getRegistreCommerce())
            .set(SOCIETES.FORME_JURIDIQUE, societe.getFormeJuridique())
            .set(SOCIETES.CAPITAL_SOCIAL, societe.getCapitalSocial())
            .set(SOCIETES.DATE_CREATION, societe.getDateCreation())
            .set(SOCIETES.ADRESSE, societe.getAdresse())
            .set(SOCIETES.VILLE, societe.getVille())
            .set(SOCIETES.CODE_POSTAL, societe.getCodePostal())
            .set(SOCIETES.TELEPHONE, societe.getTelephone())
            .set(SOCIETES.FAX, societe.getFax())
            .set(SOCIETES.EMAIL, societe.getEmail())
            .set(SOCIETES.SITE_WEB, societe.getSiteWeb())
            .set(SOCIETES.ACTIVITE, societe.getActivite())
            .set(SOCIETES.SECTEUR, societe.getSecteur())
            .set(SOCIETES.IS_ACTIVE, societe.getIsActive() != null ? societe.getIsActive() : true)
            .set(SOCIETES.CREATED_AT, LocalDateTime.now())
            .set(SOCIETES.UPDATED_AT, LocalDateTime.now())
            .set(SOCIETES.CREATED_BY, societe.getCreatedBy())
            .set(SOCIETES.UPDATED_BY, societe.getUpdatedBy())
            .returning()
            .fetchOne();

    return record != null ? record.into(Societes.class) : null;
  }

  public Societes update(Societes societe) {
    log.debug("Updating societe: {}", societe.getId());

    SocietesRecord record =
        dsl.update(SOCIETES)
            .set(SOCIETES.RAISON_SOCIALE, societe.getRaisonSociale())
            .set(SOCIETES.MATRICULE_FISCALE, societe.getMatriculeFiscale())
            .set(SOCIETES.CODE_TVA, societe.getCodeTva())
            .set(SOCIETES.CODE_DOUANE, societe.getCodeDouane())
            .set(SOCIETES.REGISTRE_COMMERCE, societe.getRegistreCommerce())
            .set(SOCIETES.FORME_JURIDIQUE, societe.getFormeJuridique())
            .set(SOCIETES.CAPITAL_SOCIAL, societe.getCapitalSocial())
            .set(SOCIETES.DATE_CREATION, societe.getDateCreation())
            .set(SOCIETES.ADRESSE, societe.getAdresse())
            .set(SOCIETES.VILLE, societe.getVille())
            .set(SOCIETES.CODE_POSTAL, societe.getCodePostal())
            .set(SOCIETES.TELEPHONE, societe.getTelephone())
            .set(SOCIETES.FAX, societe.getFax())
            .set(SOCIETES.EMAIL, societe.getEmail())
            .set(SOCIETES.SITE_WEB, societe.getSiteWeb())
            .set(SOCIETES.ACTIVITE, societe.getActivite())
            .set(SOCIETES.SECTEUR, societe.getSecteur())
            .set(SOCIETES.IS_ACTIVE, societe.getIsActive())
            .set(SOCIETES.UPDATED_AT, LocalDateTime.now())
            .set(SOCIETES.UPDATED_BY, societe.getUpdatedBy())
            .where(SOCIETES.ID.eq(societe.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Societes.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting societe: {}", id);
    int deleted = dsl.deleteFrom(SOCIETES).where(SOCIETES.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Societes> findById(Long id) {
    log.debug("Finding societe by id: {}", id);
    return dsl.selectFrom(SOCIETES)
        .where(SOCIETES.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Societes.class));
  }

  public Optional<Societes> findByMatriculeFiscale(String matriculeFiscale) {
    log.debug("Finding societe by matricule fiscale: {}", matriculeFiscale);
    return dsl.selectFrom(SOCIETES)
        .where(SOCIETES.MATRICULE_FISCALE.eq(matriculeFiscale))
        .fetchOptional()
        .map(record -> record.into(Societes.class));
  }

  public List<Societes> findAll() {
    log.debug("Finding all societes");
    return dsl.selectFrom(SOCIETES).fetch().into(Societes.class);
  }

  public List<Societes> findAllActive() {
    log.debug("Finding all active societes");
    return dsl.selectFrom(SOCIETES).where(SOCIETES.IS_ACTIVE.eq(true)).fetch().into(Societes.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if societe exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(SOCIETES).where(SOCIETES.ID.eq(id)));
  }

  public boolean existsByMatriculeFiscale(String matriculeFiscale) {
    return dsl.fetchExists(
        dsl.selectFrom(SOCIETES).where(SOCIETES.MATRICULE_FISCALE.eq(matriculeFiscale)));
  }

  /** Récupère les sociétés accessibles par un COMPTABLE (via comptable_societes) */
  public List<Societes> findByComptableId(Long comptableId) {
    log.debug("Finding societes for comptable: {}", comptableId);

    return dsl.select(SOCIETES.fields())
        .from(SOCIETES)
        .join(COMPTABLE_SOCIETES)
        .on(COMPTABLE_SOCIETES.SOCIETE_ID.eq(SOCIETES.ID))
        .where(
            COMPTABLE_SOCIETES.USER_ID.eq(comptableId).and(COMPTABLE_SOCIETES.IS_ACTIVE.isTrue()))
        .fetch()
        .into(Societes.class);
  }

  /** Récupère les sociétés accessibles par un utilisateur SOCIETE (via user_societes) */
  public List<Societes> findByUserSocieteId(Long userId) {
    log.debug("Finding societes for user societe: {}", userId);

    return dsl.select(SOCIETES.fields())
        .from(SOCIETES)
        .join(USER_SOCIETES)
        .on(USER_SOCIETES.SOCIETE_ID.eq(SOCIETES.ID))
        .where(USER_SOCIETES.USER_ID.eq(userId).and(USER_SOCIETES.IS_ACTIVE.isTrue()))
        .fetch()
        .into(Societes.class);
  }

  /** Récupère la société d'un EMPLOYEE (via employees) */
  public List<Societes> findByEmployeeId(Long employeeId) {
    log.debug("Finding societe for employee: {}", employeeId);

    return dsl.select(SOCIETES.fields())
        .from(SOCIETES)
        .join(EMPLOYEES)
        .on(EMPLOYEES.SOCIETE_ID.eq(SOCIETES.ID))
        .where(EMPLOYEES.USER_ID.eq(employeeId).and(EMPLOYEES.IS_ACTIVE.isTrue()))
        .fetch()
        .into(Societes.class);
  }

  /** Récupère les IDs des sociétés accessibles par un COMPTABLE */
  public List<Long> findSocieteIdsByComptableId(Long comptableId) {
    log.debug("Finding societe IDs for comptable: {}", comptableId);

    return dsl.select(COMPTABLE_SOCIETES.SOCIETE_ID)
        .from(COMPTABLE_SOCIETES)
        .where(
            COMPTABLE_SOCIETES.USER_ID.eq(comptableId).and(COMPTABLE_SOCIETES.IS_ACTIVE.isTrue()))
        .fetch(COMPTABLE_SOCIETES.SOCIETE_ID);
  }

  /** Récupère les IDs des sociétés accessibles par un utilisateur SOCIETE */
  public List<Long> findSocieteIdsByUserSocieteId(Long userId) {
    log.debug("Finding societe IDs for user societe: {}", userId);

    return dsl.select(USER_SOCIETES.SOCIETE_ID)
        .from(USER_SOCIETES)
        .where(USER_SOCIETES.USER_ID.eq(userId).and(USER_SOCIETES.IS_ACTIVE.isTrue()))
        .fetch(USER_SOCIETES.SOCIETE_ID);
  }

  /** Récupère l'ID de la société d'un EMPLOYEE */
  public List<Long> findSocieteIdsByEmployeeId(Long employeeId) {
    log.debug("Finding societe ID for employee: {}", employeeId);

    return dsl.select(EMPLOYEES.SOCIETE_ID)
        .from(EMPLOYEES)
        .where(EMPLOYEES.USER_ID.eq(employeeId).and(EMPLOYEES.IS_ACTIVE.isTrue()))
        .fetch(EMPLOYEES.SOCIETE_ID);
  }
}
