package tn.cyberious.compta.oauth2.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.oauth2.generated.tables.Roles;
import tn.cyberious.compta.oauth2.generated.tables.UserRoles;
import tn.cyberious.compta.oauth2.generated.tables.Users;
import tn.cyberious.compta.oauth2.generated.tables.records.UsersRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final DSLContext dsl;

  public Optional<UsersRecord> findByUsername(String username) {
    return dsl.selectFrom(Users.USERS).where(Users.USERS.USERNAME.eq(username)).fetchOptional();
  }

  public Optional<UsersRecord> findById(UUID id) {
    return dsl.selectFrom(Users.USERS).where(Users.USERS.ID.eq(id)).fetchOptional();
  }

  public Optional<UsersRecord> findByEmail(String email) {
    return dsl.selectFrom(Users.USERS).where(Users.USERS.EMAIL.eq(email)).fetchOptional();
  }

  public Optional<UsersRecord> findByIdAndEmail(UUID id, String email) {
    return dsl.selectFrom(Users.USERS)
        .where(Users.USERS.ID.eq(id))
        .and(Users.USERS.EMAIL.eq(email))
        .fetchOptional();
  }

  public boolean existsByUsername(String username) {
    return dsl.fetchExists(dsl.selectFrom(Users.USERS).where(Users.USERS.USERNAME.eq(username)));
  }

  public boolean existsByEmail(String email) {
    return dsl.fetchExists(dsl.selectFrom(Users.USERS).where(Users.USERS.EMAIL.eq(email)));
  }

  public boolean existsByEmailExcludingId(String email, UUID id) {
    return dsl.fetchExists(
        dsl.selectFrom(Users.USERS).where(Users.USERS.EMAIL.eq(email)).and(Users.USERS.ID.ne(id)));
  }

  public List<UsersRecord> findAll() {
    return dsl.selectFrom(Users.USERS).fetch();
  }

  public UsersRecord insertUser(
      String username, String password, String email, String firstName, String lastName) {
    return dsl.insertInto(Users.USERS)
        .set(Users.USERS.USERNAME, username)
        .set(Users.USERS.PASSWORD, password)
        .set(Users.USERS.EMAIL, email)
        .set(Users.USERS.FIRST_NAME, firstName)
        .set(Users.USERS.LAST_NAME, lastName)
        .set(Users.USERS.ENABLED, true)
        .set(Users.USERS.ACCOUNT_NON_EXPIRED, true)
        .set(Users.USERS.ACCOUNT_NON_LOCKED, true)
        .set(Users.USERS.CREDENTIALS_NON_EXPIRED, true)
        .returning()
        .fetchOne();
  }

  public int updateFields(UUID id, String firstName, String lastName, String email) {
    var updateStep = dsl.update(Users.USERS).set(Users.USERS.UPDATED_AT, LocalDateTime.now());

    if (firstName != null) {
      updateStep.set(Users.USERS.FIRST_NAME, firstName);
    }
    if (lastName != null) {
      updateStep.set(Users.USERS.LAST_NAME, lastName);
    }
    if (email != null) {
      updateStep.set(Users.USERS.EMAIL, email);
    }

    return updateStep.where(Users.USERS.ID.eq(id)).execute();
  }

  public int updatePassword(UUID id, String password) {
    return dsl.update(Users.USERS)
        .set(Users.USERS.PASSWORD, password)
        .set(Users.USERS.UPDATED_AT, LocalDateTime.now())
        .where(Users.USERS.ID.eq(id))
        .execute();
  }

  public int updateEnabled(UUID id, boolean enabled) {
    return dsl.update(Users.USERS)
        .set(Users.USERS.ENABLED, enabled)
        .set(Users.USERS.UPDATED_AT, LocalDateTime.now())
        .where(Users.USERS.ID.eq(id))
        .execute();
  }

  public int delete(UUID id) {
    return dsl.deleteFrom(Users.USERS).where(Users.USERS.ID.eq(id)).execute();
  }

  public List<String> getUserRoles(UUID userId) {
    return dsl.select(Roles.ROLES.NAME)
        .from(UserRoles.USER_ROLES)
        .join(Roles.ROLES)
        .on(UserRoles.USER_ROLES.ROLE_ID.eq(Roles.ROLES.ID))
        .where(UserRoles.USER_ROLES.USER_ID.eq(userId))
        .fetch(Roles.ROLES.NAME);
  }

  public Optional<UUID> findRoleIdByName(String roleName) {
    return dsl.select(Roles.ROLES.ID)
        .from(Roles.ROLES)
        .where(Roles.ROLES.NAME.eq(roleName))
        .fetchOptional(Roles.ROLES.ID);
  }

  public int insertUserRole(UUID userId, UUID roleId) {
    return dsl.insertInto(UserRoles.USER_ROLES)
        .set(UserRoles.USER_ROLES.USER_ID, userId)
        .set(UserRoles.USER_ROLES.ROLE_ID, roleId)
        .execute();
  }

  public int deleteUserRoles(UUID userId) {
    return dsl.deleteFrom(UserRoles.USER_ROLES)
        .where(UserRoles.USER_ROLES.USER_ID.eq(userId))
        .execute();
  }

  public int deleteUserRole(UUID userId, UUID roleId) {
    return dsl.deleteFrom(UserRoles.USER_ROLES)
        .where(UserRoles.USER_ROLES.USER_ID.eq(userId))
        .and(UserRoles.USER_ROLES.ROLE_ID.eq(roleId))
        .execute();
  }
}
