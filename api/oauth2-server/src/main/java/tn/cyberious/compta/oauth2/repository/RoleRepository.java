package tn.cyberious.compta.oauth2.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.oauth2.generated.tables.Roles;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepository {

  private final DSLContext dsl;

  public Optional<tn.cyberious.compta.oauth2.generated.tables.records.RolesRecord> findById(UUID id) {
    return dsl.selectFrom(Roles.ROLES).where(Roles.ROLES.ID.eq(id)).fetchOptional();
  }

  public Optional<tn.cyberious.compta.oauth2.generated.tables.records.RolesRecord> findByName(String name) {
    return dsl.selectFrom(Roles.ROLES).where(Roles.ROLES.NAME.eq(name)).fetchOptional();
  }

  public boolean existsByName(String name) {
    return dsl.fetchExists(dsl.selectFrom(Roles.ROLES).where(Roles.ROLES.NAME.eq(name)));
  }

  public List<tn.cyberious.compta.oauth2.generated.tables.records.RolesRecord> findAll() {
    return dsl.selectFrom(Roles.ROLES).fetch();
  }

  public tn.cyberious.compta.oauth2.generated.tables.records.RolesRecord insert(String name, String description) {
    return dsl.insertInto(Roles.ROLES)
        .set(Roles.ROLES.NAME, name)
        .set(Roles.ROLES.DESCRIPTION, description)
        .returning()
        .fetchOne();
  }

  public int update(UUID id, String description) {
    return dsl.update(Roles.ROLES)
        .set(Roles.ROLES.DESCRIPTION, description)
        .where(Roles.ROLES.ID.eq(id))
        .execute();
  }

  public int delete(UUID id) {
    return dsl.deleteFrom(Roles.ROLES).where(Roles.ROLES.ID.eq(id)).execute();
  }
}
