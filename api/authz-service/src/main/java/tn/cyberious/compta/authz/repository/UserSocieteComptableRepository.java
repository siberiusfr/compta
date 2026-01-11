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
import tn.cyberious.compta.authz.generated.tables.pojos.UserSocieteComptable;
import tn.cyberious.compta.authz.generated.tables.records.UserSocieteComptableRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserSocieteComptableRepository {

  private final DSLContext dsl;

  public UserSocieteComptable insert(UserSocieteComptable userSocieteComptable) {
    LocalDate dateDebut =
        userSocieteComptable.getDateDebut() != null
            ? userSocieteComptable.getDateDebut()
            : LocalDate.now();

    UserSocieteComptableRecord record =
        dsl.insertInto(USER_SOCIETE_COMPTABLE)
            .set(USER_SOCIETE_COMPTABLE.USER_ID, userSocieteComptable.getUserId())
            .set(
                USER_SOCIETE_COMPTABLE.SOCIETE_COMPTABLE_ID,
                userSocieteComptable.getSocieteComptableId())
            .set(USER_SOCIETE_COMPTABLE.ROLE, userSocieteComptable.getRole())
            .set(USER_SOCIETE_COMPTABLE.DATE_DEBUT, dateDebut)
            .set(USER_SOCIETE_COMPTABLE.DATE_FIN, userSocieteComptable.getDateFin())
            .set(USER_SOCIETE_COMPTABLE.IS_ACTIVE, true)
            .set(USER_SOCIETE_COMPTABLE.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();
    return record != null ? record.into(UserSocieteComptable.class) : null;
  }

  public UserSocieteComptable update(UserSocieteComptable userSocieteComptable) {
    UserSocieteComptableRecord record =
        dsl.update(USER_SOCIETE_COMPTABLE)
            .set(USER_SOCIETE_COMPTABLE.ROLE, userSocieteComptable.getRole())
            .set(USER_SOCIETE_COMPTABLE.DATE_FIN, userSocieteComptable.getDateFin())
            .set(USER_SOCIETE_COMPTABLE.IS_ACTIVE, userSocieteComptable.getIsActive())
            .where(USER_SOCIETE_COMPTABLE.ID.eq(userSocieteComptable.getId()))
            .returning()
            .fetchOne();
    return record != null ? record.into(UserSocieteComptable.class) : null;
  }

  public boolean delete(Long id) {
    return dsl.deleteFrom(USER_SOCIETE_COMPTABLE).where(USER_SOCIETE_COMPTABLE.ID.eq(id)).execute()
        > 0;
  }

  public boolean deleteByUserId(Long userId) {
    return dsl.deleteFrom(USER_SOCIETE_COMPTABLE)
            .where(USER_SOCIETE_COMPTABLE.USER_ID.eq(userId))
            .execute()
        > 0;
  }

  public Optional<UserSocieteComptable> findById(Long id) {
    return dsl.selectFrom(USER_SOCIETE_COMPTABLE)
        .where(USER_SOCIETE_COMPTABLE.ID.eq(id))
        .fetchOptional()
        .map(r -> r.into(UserSocieteComptable.class));
  }

  public Optional<UserSocieteComptable> findByUserId(Long userId) {
    return dsl.selectFrom(USER_SOCIETE_COMPTABLE)
        .where(USER_SOCIETE_COMPTABLE.USER_ID.eq(userId))
        .fetchOptional()
        .map(r -> r.into(UserSocieteComptable.class));
  }

  public Optional<UserSocieteComptable> findActiveByUserId(Long userId) {
    return dsl.selectFrom(USER_SOCIETE_COMPTABLE)
        .where(USER_SOCIETE_COMPTABLE.USER_ID.eq(userId))
        .and(USER_SOCIETE_COMPTABLE.IS_ACTIVE.eq(true))
        .and(
            USER_SOCIETE_COMPTABLE
                .DATE_FIN
                .isNull()
                .or(USER_SOCIETE_COMPTABLE.DATE_FIN.ge(LocalDate.now())))
        .fetchOptional()
        .map(r -> r.into(UserSocieteComptable.class));
  }

  public List<UserSocieteComptable> findBySocieteComptableId(Long societeComptableId) {
    return dsl.selectFrom(USER_SOCIETE_COMPTABLE)
        .where(USER_SOCIETE_COMPTABLE.SOCIETE_COMPTABLE_ID.eq(societeComptableId))
        .orderBy(USER_SOCIETE_COMPTABLE.ROLE, USER_SOCIETE_COMPTABLE.CREATED_AT)
        .fetch()
        .into(UserSocieteComptable.class);
  }

  public List<UserSocieteComptable> findActiveBySocieteComptableId(Long societeComptableId) {
    return dsl.selectFrom(USER_SOCIETE_COMPTABLE)
        .where(USER_SOCIETE_COMPTABLE.SOCIETE_COMPTABLE_ID.eq(societeComptableId))
        .and(USER_SOCIETE_COMPTABLE.IS_ACTIVE.eq(true))
        .and(
            USER_SOCIETE_COMPTABLE
                .DATE_FIN
                .isNull()
                .or(USER_SOCIETE_COMPTABLE.DATE_FIN.ge(LocalDate.now())))
        .orderBy(USER_SOCIETE_COMPTABLE.ROLE, USER_SOCIETE_COMPTABLE.CREATED_AT)
        .fetch()
        .into(UserSocieteComptable.class);
  }

  public List<UserSocieteComptable> findByRole(String role) {
    return dsl.selectFrom(USER_SOCIETE_COMPTABLE)
        .where(USER_SOCIETE_COMPTABLE.ROLE.eq(role))
        .and(USER_SOCIETE_COMPTABLE.IS_ACTIVE.eq(true))
        .orderBy(USER_SOCIETE_COMPTABLE.CREATED_AT)
        .fetch()
        .into(UserSocieteComptable.class);
  }

  public Optional<UserSocieteComptable> findManagerBySocieteComptableId(Long societeComptableId) {
    return dsl.selectFrom(USER_SOCIETE_COMPTABLE)
        .where(USER_SOCIETE_COMPTABLE.SOCIETE_COMPTABLE_ID.eq(societeComptableId))
        .and(USER_SOCIETE_COMPTABLE.ROLE.eq("MANAGER"))
        .and(USER_SOCIETE_COMPTABLE.IS_ACTIVE.eq(true))
        .fetchOptional()
        .map(r -> r.into(UserSocieteComptable.class));
  }

  public boolean existsByUserId(Long userId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(USER_SOCIETE_COMPTABLE)
            .where(USER_SOCIETE_COMPTABLE.USER_ID.eq(userId)));
  }

  public boolean existsActiveManagerBySocieteComptableId(Long societeComptableId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(USER_SOCIETE_COMPTABLE)
            .where(USER_SOCIETE_COMPTABLE.SOCIETE_COMPTABLE_ID.eq(societeComptableId))
            .and(USER_SOCIETE_COMPTABLE.ROLE.eq("MANAGER"))
            .and(USER_SOCIETE_COMPTABLE.IS_ACTIVE.eq(true)));
  }

  public long countBySocieteComptableId(Long societeComptableId) {
    return dsl.selectCount()
        .from(USER_SOCIETE_COMPTABLE)
        .where(USER_SOCIETE_COMPTABLE.SOCIETE_COMPTABLE_ID.eq(societeComptableId))
        .and(USER_SOCIETE_COMPTABLE.IS_ACTIVE.eq(true))
        .fetchOne(0, Long.class);
  }
}
