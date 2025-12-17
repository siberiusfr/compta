package tn.cyberious.compta.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.auth.generated.tables.pojos.UserRoles;
import tn.cyberious.compta.auth.generated.tables.records.UserRolesRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static tn.cyberious.compta.auth.generated.Tables.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRoleRepository {

    private final DSLContext dsl;

    public UserRoles insert(UserRoles userRole) {
        log.debug("Inserting user role: userId={}, roleId={}", userRole.getUserId(), userRole.getRoleId());

        UserRolesRecord record = dsl.insertInto(USER_ROLES)
                .set(USER_ROLES.USER_ID, userRole.getUserId())
                .set(USER_ROLES.ROLE_ID, userRole.getRoleId())
                .set(USER_ROLES.CREATED_AT, LocalDateTime.now())
                .returning()
                .fetchOne();

        return record != null ? record.into(UserRoles.class) : null;
    }

    public UserRoles update(UserRoles userRole) {
        log.debug("Updating user role: {}", userRole.getId());

        UserRolesRecord record = dsl.update(USER_ROLES)
                .set(USER_ROLES.USER_ID, userRole.getUserId())
                .set(USER_ROLES.ROLE_ID, userRole.getRoleId())
                .where(USER_ROLES.ID.eq(userRole.getId()))
                .returning()
                .fetchOne();

        return record != null ? record.into(UserRoles.class) : null;
    }

    public boolean delete(Long id) {
        log.debug("Deleting user role: {}", id);
        int deleted = dsl.deleteFrom(USER_ROLES)
                .where(USER_ROLES.ID.eq(id))
                .execute();
        return deleted > 0;
    }

    public boolean deleteByUserIdAndRoleId(Long userId, Long roleId) {
        log.debug("Deleting user role: userId={}, roleId={}", userId, roleId);
        int deleted = dsl.deleteFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(userId).and(USER_ROLES.ROLE_ID.eq(roleId)))
                .execute();
        return deleted > 0;
    }

    public Optional<UserRoles> findById(Long id) {
        log.debug("Finding user role by id: {}", id);
        return dsl.selectFrom(USER_ROLES)
                .where(USER_ROLES.ID.eq(id))
                .fetchOptional()
                .map(record -> record.into(UserRoles.class));
    }

    public List<UserRoles> findAll() {
        log.debug("Finding all user roles");
        return dsl.selectFrom(USER_ROLES)
                .fetch()
                .into(UserRoles.class);
    }

    public List<UserRoles> findByUserId(Long userId) {
        log.debug("Finding user roles by userId: {}", userId);
        return dsl.selectFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(userId))
                .fetch()
                .into(UserRoles.class);
    }

    public boolean exists(Long id) {
        log.debug("Checking if user role exists: {}", id);
        return dsl.fetchExists(
                dsl.selectFrom(USER_ROLES)
                        .where(USER_ROLES.ID.eq(id))
        );
    }

    public void assignRole(Long userId, Long roleId) {
        log.debug("Assigning role {} to user {}", roleId, userId);
        dsl.insertInto(USER_ROLES)
                .set(USER_ROLES.USER_ID, userId)
                .set(USER_ROLES.ROLE_ID, roleId)
                .set(USER_ROLES.CREATED_AT, LocalDateTime.now())
                .execute();
    }

    public void removeRole(Long userId, Long roleId) {
        log.debug("Removing role {} from user {}", roleId, userId);
        dsl.deleteFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(userId).and(USER_ROLES.ROLE_ID.eq(roleId)))
                .execute();
    }
}
