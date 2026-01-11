package tn.cyberious.compta.authz.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static tn.cyberious.compta.authz.generated.Tables.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.authz.generated.tables.pojos.RolePermissions;
import tn.cyberious.compta.authz.generated.tables.records.RolePermissionsRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RolePermissionRepository {

  private final DSLContext dsl;

  public RolePermissions insert(RolePermissions rolePermission) {
    RolePermissionsRecord record =
        dsl.insertInto(ROLE_PERMISSIONS)
            .set(ROLE_PERMISSIONS.ROLE, rolePermission.getRole())
            .set(ROLE_PERMISSIONS.PERMISSION_ID, rolePermission.getPermissionId())
            .set(ROLE_PERMISSIONS.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();
    return record != null ? record.into(RolePermissions.class) : null;
  }

  public boolean delete(Long id) {
    return dsl.deleteFrom(ROLE_PERMISSIONS).where(ROLE_PERMISSIONS.ID.eq(id)).execute() > 0;
  }

  public boolean deleteByRoleAndPermissionId(String role, Long permissionId) {
    return dsl.deleteFrom(ROLE_PERMISSIONS)
            .where(ROLE_PERMISSIONS.ROLE.eq(role))
            .and(ROLE_PERMISSIONS.PERMISSION_ID.eq(permissionId))
            .execute()
        > 0;
  }

  public int deleteByRole(String role) {
    return dsl.deleteFrom(ROLE_PERMISSIONS).where(ROLE_PERMISSIONS.ROLE.eq(role)).execute();
  }

  public int deleteByPermissionId(Long permissionId) {
    return dsl.deleteFrom(ROLE_PERMISSIONS)
        .where(ROLE_PERMISSIONS.PERMISSION_ID.eq(permissionId))
        .execute();
  }

  public Optional<RolePermissions> findById(Long id) {
    return dsl.selectFrom(ROLE_PERMISSIONS)
        .where(ROLE_PERMISSIONS.ID.eq(id))
        .fetchOptional()
        .map(r -> r.into(RolePermissions.class));
  }

  public Optional<RolePermissions> findByRoleAndPermissionId(String role, Long permissionId) {
    return dsl.selectFrom(ROLE_PERMISSIONS)
        .where(ROLE_PERMISSIONS.ROLE.eq(role))
        .and(ROLE_PERMISSIONS.PERMISSION_ID.eq(permissionId))
        .fetchOptional()
        .map(r -> r.into(RolePermissions.class));
  }

  public List<RolePermissions> findAll() {
    return dsl.selectFrom(ROLE_PERMISSIONS)
        .orderBy(ROLE_PERMISSIONS.ROLE, ROLE_PERMISSIONS.PERMISSION_ID)
        .fetch()
        .into(RolePermissions.class);
  }

  public List<RolePermissions> findByRole(String role) {
    return dsl.selectFrom(ROLE_PERMISSIONS)
        .where(ROLE_PERMISSIONS.ROLE.eq(role))
        .orderBy(ROLE_PERMISSIONS.PERMISSION_ID)
        .fetch()
        .into(RolePermissions.class);
  }

  public List<RolePermissions> findByPermissionId(Long permissionId) {
    return dsl.selectFrom(ROLE_PERMISSIONS)
        .where(ROLE_PERMISSIONS.PERMISSION_ID.eq(permissionId))
        .orderBy(ROLE_PERMISSIONS.ROLE)
        .fetch()
        .into(RolePermissions.class);
  }

  public List<String> findDistinctRoles() {
    return dsl.selectDistinct(ROLE_PERMISSIONS.ROLE)
        .from(ROLE_PERMISSIONS)
        .orderBy(ROLE_PERMISSIONS.ROLE)
        .fetch(ROLE_PERMISSIONS.ROLE);
  }

  public List<Long> findPermissionIdsByRole(String role) {
    return dsl.select(ROLE_PERMISSIONS.PERMISSION_ID)
        .from(ROLE_PERMISSIONS)
        .where(ROLE_PERMISSIONS.ROLE.eq(role))
        .fetch(ROLE_PERMISSIONS.PERMISSION_ID);
  }

  public boolean existsByRoleAndPermissionId(String role, Long permissionId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(ROLE_PERMISSIONS)
            .where(ROLE_PERMISSIONS.ROLE.eq(role))
            .and(ROLE_PERMISSIONS.PERMISSION_ID.eq(permissionId)));
  }

  public boolean hasPermission(String role, String permissionCode) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(ROLE_PERMISSIONS)
            .join(PERMISSIONS)
            .on(PERMISSIONS.ID.eq(ROLE_PERMISSIONS.PERMISSION_ID))
            .where(ROLE_PERMISSIONS.ROLE.eq(role))
            .and(PERMISSIONS.CODE.eq(permissionCode)));
  }

  public boolean hasPermissionOnResource(String role, String resource, String action) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(ROLE_PERMISSIONS)
            .join(PERMISSIONS)
            .on(PERMISSIONS.ID.eq(ROLE_PERMISSIONS.PERMISSION_ID))
            .where(ROLE_PERMISSIONS.ROLE.eq(role))
            .and(PERMISSIONS.RESOURCE.eq(resource))
            .and(PERMISSIONS.ACTION.eq(action)));
  }

  public long countByRole(String role) {
    return dsl.selectCount()
        .from(ROLE_PERMISSIONS)
        .where(ROLE_PERMISSIONS.ROLE.eq(role))
        .fetchOne(0, Long.class);
  }
}
