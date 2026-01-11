package tn.cyberious.compta.authz.repository;

import static tn.cyberious.compta.authz.generated.Tables.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.authz.generated.tables.pojos.Societes;
import tn.cyberious.compta.authz.generated.tables.pojos.UserSocietes;
import tn.cyberious.compta.authz.generated.tables.records.UserSocietesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserSocietesRepository {

  private final DSLContext dsl;

  public UserSocietes insert(UserSocietes userSociete) {
    LocalDate dateDebut =
        userSociete.getDateDebut() != null ? userSociete.getDateDebut() : LocalDate.now();

    UserSocietesRecord record =
        dsl.insertInto(USER_SOCIETES)
            .set(USER_SOCIETES.USER_ID, userSociete.getUserId())
            .set(USER_SOCIETES.SOCIETE_ID, userSociete.getSocieteId())
            .set(USER_SOCIETES.ROLE, userSociete.getRole())
            .set(USER_SOCIETES.DATE_DEBUT, dateDebut)
            .set(USER_SOCIETES.DATE_FIN, userSociete.getDateFin())
            .set(USER_SOCIETES.IS_ACTIVE, true)
            .set(USER_SOCIETES.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();
    return record != null ? record.into(UserSocietes.class) : null;
  }

  public UserSocietes update(UserSocietes userSociete) {
    UserSocietesRecord record =
        dsl.update(USER_SOCIETES)
            .set(USER_SOCIETES.ROLE, userSociete.getRole())
            .set(USER_SOCIETES.DATE_FIN, userSociete.getDateFin())
            .set(USER_SOCIETES.IS_ACTIVE, userSociete.getIsActive())
            .where(USER_SOCIETES.ID.eq(userSociete.getId()))
            .returning()
            .fetchOne();
    return record != null ? record.into(UserSocietes.class) : null;
  }

  public boolean delete(Long id) {
    return dsl.deleteFrom(USER_SOCIETES).where(USER_SOCIETES.ID.eq(id)).execute() > 0;
  }

  public boolean deleteByUserId(Long userId) {
    return dsl.deleteFrom(USER_SOCIETES).where(USER_SOCIETES.USER_ID.eq(userId)).execute() > 0;
  }

  public Optional<UserSocietes> findById(Long id) {
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.ID.eq(id))
        .fetchOptional()
        .map(r -> r.into(UserSocietes.class));
  }

  public Optional<UserSocietes> findByUserId(Long userId) {
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.USER_ID.eq(userId))
        .fetchOptional()
        .map(r -> r.into(UserSocietes.class));
  }

  public Optional<UserSocietes> findActiveByUserId(Long userId) {
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.USER_ID.eq(userId))
        .and(USER_SOCIETES.IS_ACTIVE.eq(true))
        .and(USER_SOCIETES.DATE_FIN.isNull().or(USER_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .fetchOptional()
        .map(r -> r.into(UserSocietes.class));
  }

  public List<UserSocietes> findBySocieteId(Long societeId) {
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.SOCIETE_ID.eq(societeId))
        .orderBy(USER_SOCIETES.ROLE, USER_SOCIETES.CREATED_AT)
        .fetch()
        .into(UserSocietes.class);
  }

  public List<UserSocietes> findActiveBySocieteId(Long societeId) {
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.SOCIETE_ID.eq(societeId))
        .and(USER_SOCIETES.IS_ACTIVE.eq(true))
        .and(USER_SOCIETES.DATE_FIN.isNull().or(USER_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .orderBy(USER_SOCIETES.ROLE, USER_SOCIETES.CREATED_AT)
        .fetch()
        .into(UserSocietes.class);
  }

  public List<UserSocietes> findByRole(String role) {
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.ROLE.eq(role))
        .and(USER_SOCIETES.IS_ACTIVE.eq(true))
        .orderBy(USER_SOCIETES.CREATED_AT)
        .fetch()
        .into(UserSocietes.class);
  }

  public Optional<UserSocietes> findManagerBySocieteId(Long societeId) {
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.SOCIETE_ID.eq(societeId))
        .and(USER_SOCIETES.ROLE.eq("MANAGER"))
        .and(USER_SOCIETES.IS_ACTIVE.eq(true))
        .fetchOptional()
        .map(r -> r.into(UserSocietes.class));
  }

  public Optional<Societes> findSocieteByUserId(Long userId) {
    return dsl.select(SOCIETES.asterisk())
        .from(SOCIETES)
        .join(USER_SOCIETES)
        .on(USER_SOCIETES.SOCIETE_ID.eq(SOCIETES.ID))
        .where(USER_SOCIETES.USER_ID.eq(userId))
        .and(USER_SOCIETES.IS_ACTIVE.eq(true))
        .and(USER_SOCIETES.DATE_FIN.isNull().or(USER_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .fetchOptionalInto(Societes.class);
  }

  public boolean existsByUserId(Long userId) {
    return dsl.fetchExists(
        dsl.selectOne().from(USER_SOCIETES).where(USER_SOCIETES.USER_ID.eq(userId)));
  }

  public boolean hasActiveAccess(Long userId, Long societeId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(USER_SOCIETES)
            .where(USER_SOCIETES.USER_ID.eq(userId))
            .and(USER_SOCIETES.SOCIETE_ID.eq(societeId))
            .and(USER_SOCIETES.IS_ACTIVE.eq(true))
            .and(USER_SOCIETES.DATE_FIN.isNull().or(USER_SOCIETES.DATE_FIN.ge(LocalDate.now()))));
  }

  public Optional<String> findRoleByUserIdAndSocieteId(Long userId, Long societeId) {
    return dsl.select(USER_SOCIETES.ROLE)
        .from(USER_SOCIETES)
        .where(USER_SOCIETES.USER_ID.eq(userId))
        .and(USER_SOCIETES.SOCIETE_ID.eq(societeId))
        .and(USER_SOCIETES.IS_ACTIVE.eq(true))
        .and(USER_SOCIETES.DATE_FIN.isNull().or(USER_SOCIETES.DATE_FIN.ge(LocalDate.now())))
        .fetchOptional(USER_SOCIETES.ROLE);
  }

  public boolean existsActiveManagerBySocieteId(Long societeId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(USER_SOCIETES)
            .where(USER_SOCIETES.SOCIETE_ID.eq(societeId))
            .and(USER_SOCIETES.ROLE.eq("MANAGER"))
            .and(USER_SOCIETES.IS_ACTIVE.eq(true)));
  }

  public long countBySocieteId(Long societeId) {
    return dsl.selectCount()
        .from(USER_SOCIETES)
        .where(USER_SOCIETES.SOCIETE_ID.eq(societeId))
        .and(USER_SOCIETES.IS_ACTIVE.eq(true))
        .fetchOne(0, Long.class);
  }
}
