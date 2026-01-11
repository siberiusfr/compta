package tn.cyberious.compta.authz.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static tn.cyberious.compta.authz.generated.Tables.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.authz.generated.tables.pojos.ComptableSocietes;
import tn.cyberious.compta.authz.generated.tables.pojos.Societes;
import tn.cyberious.compta.authz.generated.tables.records.ComptableSocietesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ComptableSocietesRepository {

  private final DSLContext dsl;

  public ComptableSocietes insert(ComptableSocietes comptableSociete) {
    LocalDate dateDebut =
        comptableSociete.getDateDebut() != null ? comptableSociete.getDateDebut() : LocalDate.now();

    ComptableSocietesRecord record =
        dsl.insertInto(COMPTABLE_SOCIETES)
            .set(COMPTABLE_SOCIETES.USER_ID, comptableSociete.getUserId())
            .set(COMPTABLE_SOCIETES.SOCIETE_ID, comptableSociete.getSocieteId())
            .set(
                COMPTABLE_SOCIETES.CAN_READ,
                comptableSociete.getCanRead() != null ? comptableSociete.getCanRead() : true)
            .set(
                COMPTABLE_SOCIETES.CAN_WRITE,
                comptableSociete.getCanWrite() != null ? comptableSociete.getCanWrite() : false)
            .set(
                COMPTABLE_SOCIETES.CAN_VALIDATE,
                comptableSociete.getCanValidate() != null
                    ? comptableSociete.getCanValidate()
                    : false)
            .set(COMPTABLE_SOCIETES.DATE_DEBUT, dateDebut)
            .set(COMPTABLE_SOCIETES.DATE_FIN, comptableSociete.getDateFin())
            .set(COMPTABLE_SOCIETES.IS_ACTIVE, true)
            .set(COMPTABLE_SOCIETES.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();
    return record != null ? record.into(ComptableSocietes.class) : null;
  }

  public ComptableSocietes update(ComptableSocietes comptableSociete) {
    ComptableSocietesRecord record =
        dsl.update(COMPTABLE_SOCIETES)
            .set(COMPTABLE_SOCIETES.CAN_READ, comptableSociete.getCanRead())
            .set(COMPTABLE_SOCIETES.CAN_WRITE, comptableSociete.getCanWrite())
            .set(COMPTABLE_SOCIETES.CAN_VALIDATE, comptableSociete.getCanValidate())
            .set(COMPTABLE_SOCIETES.DATE_FIN, comptableSociete.getDateFin())
            .set(COMPTABLE_SOCIETES.IS_ACTIVE, comptableSociete.getIsActive())
            .where(COMPTABLE_SOCIETES.ID.eq(comptableSociete.getId()))
            .returning()
            .fetchOne();
    return record != null ? record.into(ComptableSocietes.class) : null;
  }

  public boolean delete(Long id) {
    return dsl.deleteFrom(COMPTABLE_SOCIETES).where(COMPTABLE_SOCIETES.ID.eq(id)).execute() > 0;
  }

  public boolean deleteByUserIdAndSocieteId(Long userId, Long societeId) {
    return dsl.deleteFrom(COMPTABLE_SOCIETES)
            .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
            .and(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
            .execute()
        > 0;
  }

  public Optional<ComptableSocietes> findById(Long id) {
    return dsl.selectFrom(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.ID.eq(id))
        .fetchOptional()
        .map(r -> r.into(ComptableSocietes.class));
  }

  public Optional<ComptableSocietes> findByUserIdAndSocieteId(Long userId, Long societeId) {
    return dsl.selectFrom(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
        .and(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
        .fetchOptional()
        .map(r -> r.into(ComptableSocietes.class));
  }

  public Optional<ComptableSocietes> findActiveAccessByUserIdAndSocieteId(
      Long userId, Long societeId) {
    return dsl.selectFrom(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
        .and(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
        .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
        .and(
            COMPTABLE_SOCIETES
                .DATE_FIN
                .isNull()
                .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .fetchOptional()
        .map(r -> r.into(ComptableSocietes.class));
  }

  public List<ComptableSocietes> findByUserId(Long userId) {
    return dsl.selectFrom(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
        .orderBy(COMPTABLE_SOCIETES.CREATED_AT.desc())
        .fetch()
        .into(ComptableSocietes.class);
  }

  public List<ComptableSocietes> findActiveByUserId(Long userId) {
    return dsl.selectFrom(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
        .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
        .and(
            COMPTABLE_SOCIETES
                .DATE_FIN
                .isNull()
                .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .orderBy(COMPTABLE_SOCIETES.CREATED_AT.desc())
        .fetch()
        .into(ComptableSocietes.class);
  }

  public List<ComptableSocietes> findBySocieteId(Long societeId) {
    return dsl.selectFrom(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
        .orderBy(COMPTABLE_SOCIETES.CREATED_AT.desc())
        .fetch()
        .into(ComptableSocietes.class);
  }

  public List<ComptableSocietes> findActiveBySocieteId(Long societeId) {
    return dsl.selectFrom(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
        .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
        .and(
            COMPTABLE_SOCIETES
                .DATE_FIN
                .isNull()
                .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .orderBy(COMPTABLE_SOCIETES.CREATED_AT.desc())
        .fetch()
        .into(ComptableSocietes.class);
  }

  public List<Societes> findSocietesByUserId(Long userId) {
    return dsl.select(SOCIETES.asterisk())
        .from(SOCIETES)
        .join(COMPTABLE_SOCIETES)
        .on(COMPTABLE_SOCIETES.SOCIETE_ID.eq(SOCIETES.ID))
        .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
        .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
        .and(
            COMPTABLE_SOCIETES
                .DATE_FIN
                .isNull()
                .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .and(SOCIETES.IS_ACTIVE.eq(true))
        .orderBy(SOCIETES.RAISON_SOCIALE)
        .fetchInto(Societes.class);
  }

  public List<Societes> findSocietesWithWriteAccessByUserId(Long userId) {
    return dsl.select(SOCIETES.asterisk())
        .from(SOCIETES)
        .join(COMPTABLE_SOCIETES)
        .on(COMPTABLE_SOCIETES.SOCIETE_ID.eq(SOCIETES.ID))
        .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
        .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
        .and(COMPTABLE_SOCIETES.CAN_WRITE.eq(true))
        .and(
            COMPTABLE_SOCIETES
                .DATE_FIN
                .isNull()
                .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .and(SOCIETES.IS_ACTIVE.eq(true))
        .orderBy(SOCIETES.RAISON_SOCIALE)
        .fetchInto(Societes.class);
  }

  public boolean existsByUserIdAndSocieteId(Long userId, Long societeId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(COMPTABLE_SOCIETES)
            .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
            .and(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId)));
  }

  public boolean hasActiveAccess(Long userId, Long societeId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(COMPTABLE_SOCIETES)
            .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
            .and(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
            .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
            .and(
                COMPTABLE_SOCIETES
                    .DATE_FIN
                    .isNull()
                    .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now()))));
  }

  public boolean hasWriteAccess(Long userId, Long societeId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(COMPTABLE_SOCIETES)
            .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
            .and(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
            .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
            .and(COMPTABLE_SOCIETES.CAN_WRITE.eq(true))
            .and(
                COMPTABLE_SOCIETES
                    .DATE_FIN
                    .isNull()
                    .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now()))));
  }

  public boolean hasValidateAccess(Long userId, Long societeId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(COMPTABLE_SOCIETES)
            .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
            .and(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
            .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
            .and(COMPTABLE_SOCIETES.CAN_VALIDATE.eq(true))
            .and(
                COMPTABLE_SOCIETES
                    .DATE_FIN
                    .isNull()
                    .or(COMPTABLE_SOCIETES.DATE_FIN.ge(LocalDate.now()))));
  }

  public long countByUserId(Long userId) {
    return dsl.selectCount()
        .from(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.USER_ID.eq(userId))
        .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
        .fetchOne(0, Long.class);
  }

  public long countBySocieteId(Long societeId) {
    return dsl.selectCount()
        .from(COMPTABLE_SOCIETES)
        .where(COMPTABLE_SOCIETES.SOCIETE_ID.eq(societeId))
        .and(COMPTABLE_SOCIETES.IS_ACTIVE.eq(true))
        .fetchOne(0, Long.class);
  }
}
