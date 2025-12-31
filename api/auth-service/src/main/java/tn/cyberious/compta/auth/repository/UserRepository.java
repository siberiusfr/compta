package tn.cyberious.compta.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static tn.cyberious.compta.auth.generated.Tables.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.auth.enums.Role;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.auth.generated.tables.records.UsersRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final DSLContext dsl;

  public Users insert(Users user) {
    log.debug("Inserting user: {}", user.getUsername());

    UsersRecord record =
        dsl.insertInto(USERS)
            .set(USERS.USERNAME, user.getUsername())
            .set(USERS.EMAIL, user.getEmail())
            .set(USERS.PASSWORD, user.getPassword())
            .set(USERS.FIRST_NAME, user.getFirstName())
            .set(USERS.LAST_NAME, user.getLastName())
            .set(USERS.PHONE, user.getPhone())
            .set(USERS.IS_ACTIVE, user.getIsActive() != null ? user.getIsActive() : true)
            .set(USERS.IS_LOCKED, user.getIsLocked() != null ? user.getIsLocked() : false)
            .set(
                USERS.FAILED_LOGIN_ATTEMPTS,
                user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0)
            .set(USERS.LAST_LOGIN_AT, user.getLastLoginAt())
            .set(USERS.PASSWORD_CHANGED_AT, user.getPasswordChangedAt())
            .set(USERS.CREATED_AT, LocalDateTime.now())
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .set(USERS.CREATED_BY, user.getCreatedBy())
            .set(USERS.UPDATED_BY, user.getUpdatedBy())
            .returning()
            .fetchOne();

    return record != null ? record.into(Users.class) : null;
  }

  public Users update(Users user) {
    log.debug("Updating user: {}", user.getId());

    UsersRecord record =
        dsl.update(USERS)
            .set(USERS.USERNAME, user.getUsername())
            .set(USERS.EMAIL, user.getEmail())
            .set(USERS.PASSWORD, user.getPassword())
            .set(USERS.FIRST_NAME, user.getFirstName())
            .set(USERS.LAST_NAME, user.getLastName())
            .set(USERS.PHONE, user.getPhone())
            .set(USERS.IS_ACTIVE, user.getIsActive())
            .set(USERS.IS_LOCKED, user.getIsLocked())
            .set(USERS.FAILED_LOGIN_ATTEMPTS, user.getFailedLoginAttempts())
            .set(USERS.LAST_LOGIN_AT, user.getLastLoginAt())
            .set(USERS.PASSWORD_CHANGED_AT, user.getPasswordChangedAt())
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .set(USERS.UPDATED_BY, user.getUpdatedBy())
            .where(USERS.ID.eq(user.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Users.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting user: {}", id);
    int deleted = dsl.deleteFrom(USERS).where(USERS.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Users> findById(Long id) {
    log.debug("Finding user by id: {}", id);
    return dsl.selectFrom(USERS)
        .where(USERS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Users.class));
  }

  public Optional<Users> findByUsername(String username) {
    log.debug("Finding user by username: {}", username);
    return dsl.selectFrom(USERS)
        .where(USERS.USERNAME.eq(username))
        .fetchOptional()
        .map(record -> record.into(Users.class));
  }

  public Optional<Users> findByEmail(String email) {
    log.debug("Finding user by email: {}", email);
    return dsl.selectFrom(USERS)
        .where(USERS.EMAIL.eq(email))
        .fetchOptional()
        .map(record -> record.into(Users.class));
  }

  public List<Users> findAll() {
    log.debug("Finding all users");
    return dsl.selectFrom(USERS).fetch().into(Users.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if user exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(USERS).where(USERS.ID.eq(id)));
  }

  public boolean existsByUsername(String username) {
    return dsl.fetchExists(dsl.selectFrom(USERS).where(USERS.USERNAME.eq(username)));
  }

  public boolean existsByEmail(String email) {
    return dsl.fetchExists(dsl.selectFrom(USERS).where(USERS.EMAIL.eq(email)));
  }

  public boolean existsByUsernameOrEmail(String username, String email) {
    return dsl.fetchExists(
        dsl.selectFrom(USERS).where(USERS.USERNAME.eq(username).or(USERS.EMAIL.eq(email))));
  }

  public void updateLastLogin(Long userId, LocalDateTime lastLogin) {
    log.debug("Updating last login for user: {}", userId);
    dsl.update(USERS)
        .set(USERS.LAST_LOGIN_AT, lastLogin)
        .set(USERS.FAILED_LOGIN_ATTEMPTS, 0)
        .where(USERS.ID.eq(userId))
        .execute();
  }

  public void incrementFailedLoginAttempts(Long userId) {
    log.debug("Incrementing failed login attempts for user: {}", userId);
    dsl.update(USERS)
        .set(USERS.FAILED_LOGIN_ATTEMPTS, USERS.FAILED_LOGIN_ATTEMPTS.plus(1))
        .where(USERS.ID.eq(userId))
        .execute();
  }

  public void lockAccount(Long userId) {
    log.debug("Locking account for user: {}", userId);
    dsl.update(USERS).set(USERS.IS_LOCKED, true).where(USERS.ID.eq(userId)).execute();
  }

  public List<Role> findRolesByUserId(Long userId) {
    log.debug("Finding roles for user: {}", userId);
    return dsl
        .select(ROLES.NAME)
        .from(USER_ROLES)
        .join(ROLES)
        .on(USER_ROLES.ROLE_ID.eq(ROLES.ID))
        .where(USER_ROLES.USER_ID.eq(userId))
        .fetch(ROLES.NAME)
        .stream()
        .map(Role::fromName)
        .toList();
  }
}
