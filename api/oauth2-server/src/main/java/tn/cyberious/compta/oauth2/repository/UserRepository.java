package tn.cyberious.compta.oauth2.repository;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.oauth2.generated.tables.UserRoles;
import tn.cyberious.compta.oauth2.generated.tables.Users;
import tn.cyberious.compta.oauth2.generated.tables.records.UsersRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final DSLContext dsl;

  public Optional<UsersRecord> findByUsername(String username) {
    return dsl
      .selectFrom(Users.USERS)
      .where(Users.USERS.USERNAME.eq(username))
      .fetchOptional();
  }

  public Optional<UsersRecord> findByEmail(String email) {
    return dsl
      .selectFrom(Users.USERS)
      .where(Users.USERS.EMAIL.eq(email))
      .fetchOptional();
  }

  public boolean existsByUsername(String username) {
    return dsl
      .fetchExists(
        dsl
          .selectFrom(Users.USERS)
          .where(Users.USERS.USERNAME.eq(username))
      );
  }

  public boolean existsByEmail(String email) {
    return dsl
      .fetchExists(
        dsl
          .selectFrom(Users.USERS)
          .where(Users.USERS.EMAIL.eq(email))
      );
  }

  public UUID insertUser(
    String username,
    String password,
    String email,
    String firstName,
    String lastName
  ) {
    UsersRecord record = dsl
      .insertInto(Users.USERS)
      .set(Users.USERS.USERNAME, username)
      .set(Users.USERS.PASSWORD, password)
      .set(Users.USERS.EMAIL, email)
      .set(Users.USERS.FIRST_NAME, firstName)
      .set(Users.USERS.LAST_NAME, lastName)
      .set(Users.USERS.ENABLED, true)
      .set(Users.USERS.ACCOUNT_NON_EXPIRED, true)
      .set(Users.USERS.ACCOUNT_NON_LOCKED, true)
      .set(Users.USERS.CREDENTIALS_NON_EXPIRED, true)
      .returning(Users.USERS.ID)
      .fetchOne();
    return record.getId();
  }
}
