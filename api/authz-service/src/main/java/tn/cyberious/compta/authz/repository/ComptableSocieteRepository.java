package tn.cyberious.compta.authz.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.authz.generated.Tables;
import tn.cyberious.compta.authz.generated.tables.pojos.ComptableSocietes;
import tn.cyberious.compta.authz.generated.tables.records.ComptableSocietesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ComptableSocieteRepository {

  private final DSLContext dsl;

  public ComptableSocietes insert(ComptableSocietes comptableSociete) {
    log.debug(
        "Inserting comptable-societe association: userId={}, societeId={}",
        comptableSociete.getUserId(),
        comptableSociete.getSocieteId());

    ComptableSocietesRecord record =
        dsl.insertInto(Tables.COMPTABLE_SOCIETES)
            .set(Tables.COMPTABLE_SOCIETES.USER_ID, comptableSociete.getUserId())
            .set(Tables.COMPTABLE_SOCIETES.SOCIETE_ID, comptableSociete.getSocieteId())
            .set(Tables.COMPTABLE_SOCIETES.DATE_DEBUT, comptableSociete.getDateDebut())
            .set(Tables.COMPTABLE_SOCIETES.DATE_FIN, comptableSociete.getDateFin())
            .set(
                Tables.COMPTABLE_SOCIETES.IS_ACTIVE,
                comptableSociete.getIsActive() != null ? comptableSociete.getIsActive() : true)
            .set(Tables.COMPTABLE_SOCIETES.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(ComptableSocietes.class) : null;
  }

  public ComptableSocietes update(ComptableSocietes comptableSociete) {
    log.debug("Updating comptable-societe association: {}", comptableSociete.getId());

    ComptableSocietesRecord record =
        dsl.update(Tables.COMPTABLE_SOCIETES)
            .set(Tables.COMPTABLE_SOCIETES.USER_ID, comptableSociete.getUserId())
            .set(Tables.COMPTABLE_SOCIETES.SOCIETE_ID, comptableSociete.getSocieteId())
            .set(Tables.COMPTABLE_SOCIETES.DATE_DEBUT, comptableSociete.getDateDebut())
            .set(Tables.COMPTABLE_SOCIETES.DATE_FIN, comptableSociete.getDateFin())
            .set(Tables.COMPTABLE_SOCIETES.IS_ACTIVE, comptableSociete.getIsActive())
            .where(Tables.COMPTABLE_SOCIETES.ID.eq(comptableSociete.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(ComptableSocietes.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting comptable-societe association: {}", id);
    int deleted =
        dsl.deleteFrom(Tables.COMPTABLE_SOCIETES)
            .where(Tables.COMPTABLE_SOCIETES.ID.eq(id))
            .execute();
    return deleted > 0;
  }

  public Optional<ComptableSocietes> findById(Long id) {
    log.debug("Finding comptable-societe association by id: {}", id);
    return dsl.selectFrom(Tables.COMPTABLE_SOCIETES)
        .where(Tables.COMPTABLE_SOCIETES.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(ComptableSocietes.class));
  }

  public List<ComptableSocietes> findByUserId(Long userId) {
    log.debug("Finding comptable-societe associations by userId: {}", userId);
    return dsl.selectFrom(Tables.COMPTABLE_SOCIETES)
        .where(Tables.COMPTABLE_SOCIETES.USER_ID.eq(userId))
        .fetch()
        .into(ComptableSocietes.class);
  }

  public List<ComptableSocietes> findBySocieteId(Long societeId) {
    log.debug("Finding comptable-societe associations by societeId: {}", societeId);
    return dsl.selectFrom(Tables.COMPTABLE_SOCIETES)
        .where(Tables.COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
        .fetch()
        .into(ComptableSocietes.class);
  }

  public List<ComptableSocietes> findAll() {
    log.debug("Finding all comptable-societe associations");
    return dsl.selectFrom(Tables.COMPTABLE_SOCIETES).fetch().into(ComptableSocietes.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if comptable-societe association exists: {}", id);
    return dsl.fetchExists(
        dsl.selectFrom(Tables.COMPTABLE_SOCIETES).where(Tables.COMPTABLE_SOCIETES.ID.eq(id)));
  }

  public void assignComptableToSociete(
      Long userId, Long societeId, LocalDate dateDebut, LocalDate dateFin) {
    log.debug("Assigning comptable {} to societe {}", userId, societeId);

    dsl.insertInto(Tables.COMPTABLE_SOCIETES)
        .set(Tables.COMPTABLE_SOCIETES.USER_ID, userId)
        .set(Tables.COMPTABLE_SOCIETES.SOCIETE_ID, societeId)
        .set(Tables.COMPTABLE_SOCIETES.DATE_DEBUT, dateDebut)
        .set(Tables.COMPTABLE_SOCIETES.DATE_FIN, dateFin)
        .set(Tables.COMPTABLE_SOCIETES.IS_ACTIVE, true)
        .set(Tables.COMPTABLE_SOCIETES.CREATED_AT, LocalDateTime.now())
        .execute();
  }

  public void removeComptableFromSociete(Long userId, Long societeId) {
    log.debug("Removing comptable {} from societe {}", userId, societeId);

    dsl.deleteFrom(Tables.COMPTABLE_SOCIETES)
        .where(
            Tables.COMPTABLE_SOCIETES
                .USER_ID
                .eq(userId)
                .and(Tables.COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId)))
        .execute();
  }
}
