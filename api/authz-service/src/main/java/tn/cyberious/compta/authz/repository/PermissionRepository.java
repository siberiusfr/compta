package tn.cyberious.compta.authz.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static tn.cyberious.compta.authz.generated.Tables.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.authz.generated.tables.pojos.Permissions;
import tn.cyberious.compta.authz.generated.tables.records.PermissionsRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PermissionRepository {

  private final DSLContext dsl;

  public Permissions insert(Permissions permission) {
    PermissionsRecord record =
        dsl.insertInto(PERMISSIONS)
            .set(PERMISSIONS.CODE, permission.getCode())
            .set(PERMISSIONS.RESOURCE, permission.getResource())
            .set(PERMISSIONS.ACTION, permission.getAction())
            .set(PERMISSIONS.DESCRIPTION, permission.getDescription())
            .set(PERMISSIONS.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();
    return record != null ? record.into(Permissions.class) : null;
  }

  public Permissions update(Permissions permission) {
    PermissionsRecord record =
        dsl.update(PERMISSIONS)
            .set(PERMISSIONS.DESCRIPTION, permission.getDescription())
            .where(PERMISSIONS.ID.eq(permission.getId()))
            .returning()
            .fetchOne();
    return record != null ? record.into(Permissions.class) : null;
  }

  public boolean delete(Long id) {
    return dsl.deleteFrom(PERMISSIONS).where(PERMISSIONS.ID.eq(id)).execute() > 0;
  }

  public Optional<Permissions> findById(Long id) {
    return dsl.selectFrom(PERMISSIONS)
        .where(PERMISSIONS.ID.eq(id))
        .fetchOptional()
        .map(r -> r.into(Permissions.class));
  }

  public Optional<Permissions> findByCode(String code) {
    return dsl.selectFrom(PERMISSIONS)
        .where(PERMISSIONS.CODE.eq(code))
        .fetchOptional()
        .map(r -> r.into(Permissions.class));
  }

  public List<Permissions> findAll() {
    return dsl.selectFrom(PERMISSIONS)
        .orderBy(PERMISSIONS.RESOURCE, PERMISSIONS.ACTION)
        .fetch()
        .into(Permissions.class);
  }

  public List<Permissions> findByResource(String resource) {
    return dsl.selectFrom(PERMISSIONS)
        .where(PERMISSIONS.RESOURCE.eq(resource))
        .orderBy(PERMISSIONS.ACTION)
        .fetch()
        .into(Permissions.class);
  }

  public List<Permissions> findByAction(String action) {
    return dsl.selectFrom(PERMISSIONS)
        .where(PERMISSIONS.ACTION.eq(action))
        .orderBy(PERMISSIONS.RESOURCE)
        .fetch()
        .into(Permissions.class);
  }

  public List<Permissions> findByRole(String role) {
    return dsl.select(PERMISSIONS.asterisk())
        .from(PERMISSIONS)
        .join(ROLE_PERMISSIONS)
        .on(ROLE_PERMISSIONS.PERMISSION_ID.eq(PERMISSIONS.ID))
        .where(ROLE_PERMISSIONS.ROLE.eq(role))
        .orderBy(PERMISSIONS.RESOURCE, PERMISSIONS.ACTION)
        .fetchInto(Permissions.class);
  }

  public List<String> findDistinctResources() {
    return dsl.selectDistinct(PERMISSIONS.RESOURCE)
        .from(PERMISSIONS)
        .orderBy(PERMISSIONS.RESOURCE)
        .fetch(PERMISSIONS.RESOURCE);
  }

  public List<String> findDistinctActions() {
    return dsl.selectDistinct(PERMISSIONS.ACTION)
        .from(PERMISSIONS)
        .orderBy(PERMISSIONS.ACTION)
        .fetch(PERMISSIONS.ACTION);
  }

  public boolean existsById(Long id) {
    return dsl.fetchExists(dsl.selectOne().from(PERMISSIONS).where(PERMISSIONS.ID.eq(id)));
  }

  public boolean existsByCode(String code) {
    return dsl.fetchExists(dsl.selectOne().from(PERMISSIONS).where(PERMISSIONS.CODE.eq(code)));
  }

  public boolean existsByResourceAndAction(String resource, String action) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(PERMISSIONS)
            .where(PERMISSIONS.RESOURCE.eq(resource))
            .and(PERMISSIONS.ACTION.eq(action)));
  }
}
