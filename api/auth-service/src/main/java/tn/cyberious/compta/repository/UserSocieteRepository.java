package tn.cyberious.compta.repository;

import static tn.cyberious.compta.auth.generated.Tables.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.auth.generated.tables.pojos.Societes;
import tn.cyberious.compta.auth.generated.tables.pojos.UserSocietes;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.auth.generated.tables.records.UserSocietesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserSocieteRepository {

  private final DSLContext dsl;

  public UserSocietes insert(UserSocietes userSociete) {
    log.debug(
        "Inserting user-societe association: userId={}, societeId={}",
        userSociete.getUserId(),
        userSociete.getSocieteId());

    UserSocietesRecord record =
        dsl.insertInto(USER_SOCIETES)
            .set(USER_SOCIETES.USER_ID, userSociete.getUserId())
            .set(USER_SOCIETES.SOCIETE_ID, userSociete.getSocieteId())
            .set(
                USER_SOCIETES.IS_OWNER,
                userSociete.getIsOwner() != null ? userSociete.getIsOwner() : false)
            .set(USER_SOCIETES.DATE_DEBUT, userSociete.getDateDebut())
            .set(USER_SOCIETES.DATE_FIN, userSociete.getDateFin())
            .set(
                USER_SOCIETES.IS_ACTIVE,
                userSociete.getIsActive() != null ? userSociete.getIsActive() : true)
            .set(USER_SOCIETES.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(UserSocietes.class) : null;
  }

  public UserSocietes update(UserSocietes userSociete) {
    log.debug("Updating user-societe association: {}", userSociete.getId());

    UserSocietesRecord record =
        dsl.update(USER_SOCIETES)
            .set(USER_SOCIETES.USER_ID, userSociete.getUserId())
            .set(USER_SOCIETES.SOCIETE_ID, userSociete.getSocieteId())
            .set(USER_SOCIETES.IS_OWNER, userSociete.getIsOwner())
            .set(USER_SOCIETES.DATE_DEBUT, userSociete.getDateDebut())
            .set(USER_SOCIETES.DATE_FIN, userSociete.getDateFin())
            .set(USER_SOCIETES.IS_ACTIVE, userSociete.getIsActive())
            .where(USER_SOCIETES.ID.eq(userSociete.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(UserSocietes.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting user-societe association: {}", id);
    int deleted = dsl.deleteFrom(USER_SOCIETES).where(USER_SOCIETES.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<UserSocietes> findById(Long id) {
    log.debug("Finding user-societe association by id: {}", id);
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(UserSocietes.class));
  }

  public List<UserSocietes> findByUserId(Long userId) {
    log.debug("Finding user-societe associations by userId: {}", userId);
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.USER_ID.eq(userId))
        .fetch()
        .into(UserSocietes.class);
  }

  public List<UserSocietes> findBySocieteId(Long societeId) {
    log.debug("Finding user-societe associations by societeId: {}", societeId);
    return dsl.selectFrom(USER_SOCIETES)
        .where(USER_SOCIETES.SOCIETE_ID.eq(societeId))
        .fetch()
        .into(UserSocietes.class);
  }

  public List<UserSocietes> findAll() {
    log.debug("Finding all user-societe associations");
    return dsl.selectFrom(USER_SOCIETES).fetch().into(UserSocietes.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if user-societe association exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(USER_SOCIETES).where(USER_SOCIETES.ID.eq(id)));
  }

  public List<Societes> findSocietesByUserId(Long userId) {
    log.debug("Finding societes for user: {}", userId);
    return dsl.select(SOCIETES.fields())
        .from(USER_SOCIETES)
        .join(SOCIETES)
        .on(USER_SOCIETES.SOCIETE_ID.eq(SOCIETES.ID))
        .where(USER_SOCIETES.USER_ID.eq(userId))
        .fetch()
        .into(Societes.class);
  }

  public List<Users> findUsersBySocieteId(Long societeId) {
    log.debug("Finding users for societe: {}", societeId);
    return dsl.select(USERS.fields())
        .from(USER_SOCIETES)
        .join(USERS)
        .on(USER_SOCIETES.USER_ID.eq(USERS.ID))
        .where(USER_SOCIETES.SOCIETE_ID.eq(societeId))
        .fetch()
        .into(Users.class);
  }

  public void assignUserToSociete(
      Long userId, Long societeId, Boolean isOwner, LocalDate dateDebut, LocalDate dateFin) {
    log.debug("Assigning user {} to societe {} (owner: {})", userId, societeId, isOwner);

    dsl.insertInto(USER_SOCIETES)
        .set(USER_SOCIETES.USER_ID, userId)
        .set(USER_SOCIETES.SOCIETE_ID, societeId)
        .set(USER_SOCIETES.IS_OWNER, isOwner != null ? isOwner : false)
        .set(USER_SOCIETES.DATE_DEBUT, dateDebut)
        .set(USER_SOCIETES.DATE_FIN, dateFin)
        .set(USER_SOCIETES.IS_ACTIVE, true)
        .set(USER_SOCIETES.CREATED_AT, LocalDateTime.now())
        .execute();
  }

  public void removeUserFromSociete(Long userId, Long societeId) {
    log.debug("Removing user {} from societe {}", userId, societeId);

    dsl.deleteFrom(USER_SOCIETES)
        .where(USER_SOCIETES.USER_ID.eq(userId).and(USER_SOCIETES.SOCIETE_ID.eq(societeId)))
        .execute();
  }
}
