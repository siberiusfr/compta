package tn.cyberious.compta.repository;

import static tn.cyberious.compta.auth.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.auth.generated.tables.pojos.Roles;
import tn.cyberious.compta.auth.generated.tables.records.RolesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepository {

  private final DSLContext dsl;

  public Roles insert(Roles role) {
    log.debug("Inserting role: {}", role.getName());

    RolesRecord record =
        dsl.insertInto(ROLES)
            .set(ROLES.NAME, role.getName())
            .set(ROLES.DESCRIPTION, role.getDescription())
            .set(ROLES.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(Roles.class) : null;
  }

  public Roles update(Roles role) {
    log.debug("Updating role: {}", role.getId());

    RolesRecord record =
        dsl.update(ROLES)
            .set(ROLES.NAME, role.getName())
            .set(ROLES.DESCRIPTION, role.getDescription())
            .where(ROLES.ID.eq(role.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Roles.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting role: {}", id);
    int deleted = dsl.deleteFrom(ROLES).where(ROLES.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Roles> findById(Long id) {
    log.debug("Finding role by id: {}", id);
    return dsl.selectFrom(ROLES)
        .where(ROLES.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Roles.class));
  }

  public Optional<Roles> findByName(String name) {
    log.debug("Finding role by name: {}", name);
    return dsl.selectFrom(ROLES)
        .where(ROLES.NAME.eq(name))
        .fetchOptional()
        .map(record -> record.into(Roles.class));
  }

  public List<Roles> findAll() {
    log.debug("Finding all roles");
    return dsl.selectFrom(ROLES).fetch().into(Roles.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if role exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(ROLES).where(ROLES.ID.eq(id)));
  }

  public boolean existsByName(String name) {
    return dsl.fetchExists(dsl.selectFrom(ROLES).where(ROLES.NAME.eq(name)));
  }
}
