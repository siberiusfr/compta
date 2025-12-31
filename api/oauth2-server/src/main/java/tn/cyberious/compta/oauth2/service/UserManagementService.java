package tn.cyberious.compta.oauth2.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.oauth2.dto.ChangePasswordRequest;
import tn.cyberious.compta.oauth2.dto.CreateUserRequest;
import tn.cyberious.compta.oauth2.dto.UpdateUserRequest;
import tn.cyberious.compta.oauth2.dto.UserResponse;
import tn.cyberious.compta.oauth2.generated.tables.Roles;
import tn.cyberious.compta.oauth2.generated.tables.UserRoles;
import tn.cyberious.compta.oauth2.generated.tables.Users;
import tn.cyberious.compta.oauth2.generated.tables.records.UsersRecord;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

  private final DSLContext dsl;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    log.info("Creating new user with username: {}", request.getUsername());

    // Check if username already exists
    if (dsl.fetchExists(
        dsl.selectFrom(Users.USERS).where(Users.USERS.USERNAME.eq(request.getUsername())))) {
      throw new IllegalArgumentException("Username already exists: " + request.getUsername());
    }

    // Check if email already exists
    if (dsl.fetchExists(
        dsl.selectFrom(Users.USERS).where(Users.USERS.EMAIL.eq(request.getEmail())))) {
      throw new IllegalArgumentException("Email already exists: " + request.getEmail());
    }

    // Insert user
    UsersRecord userRecord =
        dsl.insertInto(Users.USERS)
            .set(Users.USERS.USERNAME, request.getUsername())
            .set(Users.USERS.PASSWORD, passwordEncoder.encode(request.getPassword()))
            .set(Users.USERS.EMAIL, request.getEmail())
            .set(Users.USERS.FIRST_NAME, request.getFirstName())
            .set(Users.USERS.LAST_NAME, request.getLastName())
            .set(Users.USERS.ENABLED, true)
            .set(Users.USERS.ACCOUNT_NON_EXPIRED, true)
            .set(Users.USERS.ACCOUNT_NON_LOCKED, true)
            .set(Users.USERS.CREDENTIALS_NON_EXPIRED, true)
            .returning()
            .fetchOne();

    // Assign roles if provided
    if (request.getRoles() != null && !request.getRoles().isEmpty()) {
      assignRolesToUser(userRecord.getId(), request.getRoles());
    }

    log.info("Successfully created user with username: {}", request.getUsername());
    return toUserResponse(userRecord);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    log.debug("Retrieving all users");
    return dsl.selectFrom(Users.USERS).fetch().stream().map(this::toUserResponse).toList();
  }

  @Transactional(readOnly = true)
  public UserResponse getUserById(UUID userId) {
    log.debug("Retrieving user with id: {}", userId);
    UsersRecord userRecord =
        dsl.selectFrom(Users.USERS).where(Users.USERS.ID.eq(userId)).fetchOne();

    if (userRecord == null) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    return toUserResponse(userRecord);
  }

  @Transactional(readOnly = true)
  public UserResponse getUserByUsername(String username) {
    log.debug("Retrieving user with username: {}", username);
    UsersRecord userRecord =
        dsl.selectFrom(Users.USERS).where(Users.USERS.USERNAME.eq(username)).fetchOne();

    if (userRecord == null) {
      throw new IllegalArgumentException("User not found with username: " + username);
    }

    return toUserResponse(userRecord);
  }

  @Transactional
  public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
    log.info("Updating user with id: {}", userId);

    UsersRecord existingUser =
        dsl.selectFrom(Users.USERS).where(Users.USERS.ID.eq(userId)).fetchOne();

    if (existingUser == null) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    // Check if email is being updated and if it already exists
    if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
      if (dsl.fetchExists(
          dsl.selectFrom(Users.USERS)
              .where(Users.USERS.EMAIL.eq(request.getEmail()))
              .and(Users.USERS.ID.ne(userId)))) {
        throw new IllegalArgumentException("Email already exists: " + request.getEmail());
      }
    }

    // Build update query
    var updateStep = dsl.update(Users.USERS).set(Users.USERS.UPDATED_AT, LocalDateTime.now());

    if (request.getFirstName() != null) {
      updateStep.set(Users.USERS.FIRST_NAME, request.getFirstName());
    }
    if (request.getLastName() != null) {
      updateStep.set(Users.USERS.LAST_NAME, request.getLastName());
    }
    if (request.getEmail() != null) {
      updateStep.set(Users.USERS.EMAIL, request.getEmail());
    }

    updateStep.where(Users.USERS.ID.eq(userId)).execute();

    // Update roles if provided
    if (request.getRoles() != null) {
      // Remove existing roles
      dsl.deleteFrom(UserRoles.USER_ROLES).where(UserRoles.USER_ROLES.USER_ID.eq(userId)).execute();

      // Assign new roles
      assignRolesToUser(userId, request.getRoles());
    }

    UsersRecord updatedUser =
        dsl.selectFrom(Users.USERS).where(Users.USERS.ID.eq(userId)).fetchOne();

    log.info("Successfully updated user with id: {}", userId);
    return toUserResponse(updatedUser);
  }

  @Transactional
  public void deleteUser(UUID userId) {
    log.info("Deleting user with id: {}", userId);

    UsersRecord userRecord =
        dsl.selectFrom(Users.USERS).where(Users.USERS.ID.eq(userId)).fetchOne();

    if (userRecord == null) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    // Delete user (cascade will delete user_roles)
    dsl.deleteFrom(Users.USERS).where(Users.USERS.ID.eq(userId)).execute();

    log.info("Successfully deleted user with id: {}", userId);
  }

  @Transactional
  public void disableUser(UUID userId) {
    log.info("Disabling user with id: {}", userId);

    int updated =
        dsl.update(Users.USERS)
            .set(Users.USERS.ENABLED, false)
            .set(Users.USERS.UPDATED_AT, LocalDateTime.now())
            .where(Users.USERS.ID.eq(userId))
            .execute();

    if (updated == 0) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    log.info("Successfully disabled user with id: {}", userId);
  }

  @Transactional
  public void enableUser(UUID userId) {
    log.info("Enabling user with id: {}", userId);

    int updated =
        dsl.update(Users.USERS)
            .set(Users.USERS.ENABLED, true)
            .set(Users.USERS.UPDATED_AT, LocalDateTime.now())
            .where(Users.USERS.ID.eq(userId))
            .execute();

    if (updated == 0) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    log.info("Successfully enabled user with id: {}", userId);
  }

  @Transactional(readOnly = true)
  public List<String> getUserRoles(UUID userId) {
    return dsl.select(Roles.ROLES.NAME)
        .from(UserRoles.USER_ROLES)
        .join(Roles.ROLES)
        .on(UserRoles.USER_ROLES.ROLE_ID.eq(Roles.ROLES.ID))
        .where(UserRoles.USER_ROLES.USER_ID.eq(userId))
        .fetch(Roles.ROLES.NAME);
  }

  @Transactional
  public void assignRolesToUser(UUID userId, List<String> roleNames) {
    log.info("Assigning roles to user with id: {}", userId);

    // Remove existing roles
    dsl.deleteFrom(UserRoles.USER_ROLES).where(UserRoles.USER_ROLES.USER_ID.eq(userId)).execute();

    // Assign new roles
    for (String roleName : roleNames) {
      UUID roleId =
          dsl.select(Roles.ROLES.ID)
              .from(Roles.ROLES)
              .where(Roles.ROLES.NAME.eq(roleName))
              .fetchOne(Roles.ROLES.ID);

      if (roleId != null) {
        dsl.insertInto(UserRoles.USER_ROLES)
            .set(UserRoles.USER_ROLES.USER_ID, userId)
            .set(UserRoles.USER_ROLES.ROLE_ID, roleId)
            .execute();
      } else {
        log.warn("Role not found: {}", roleName);
      }
    }

    log.info("Successfully assigned roles to user with id: {}", userId);
  }

  @Transactional
  public void removeRoleFromUser(UUID userId, UUID roleId) {
    log.info("Removing role {} from user with id: {}", roleId, userId);

    int deleted =
        dsl.deleteFrom(UserRoles.USER_ROLES)
            .where(UserRoles.USER_ROLES.USER_ID.eq(userId))
            .and(UserRoles.USER_ROLES.ROLE_ID.eq(roleId))
            .execute();

    if (deleted == 0) {
      throw new IllegalArgumentException("User role not found");
    }

    log.info("Successfully removed role {} from user with id: {}", roleId, userId);
  }

  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequest request) {
    log.info("Changing password for user with id: {}", userId);

    UsersRecord userRecord =
        dsl.selectFrom(Users.USERS).where(Users.USERS.ID.eq(userId)).fetchOne();

    if (userRecord == null) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    // Verify current password
    if (!passwordEncoder.matches(request.getCurrentPassword(), userRecord.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }

    // Update password
    dsl.update(Users.USERS)
        .set(Users.USERS.PASSWORD, passwordEncoder.encode(request.getNewPassword()))
        .set(Users.USERS.UPDATED_AT, LocalDateTime.now())
        .where(Users.USERS.ID.eq(userId))
        .execute();

    log.info("Successfully changed password for user with id: {}", userId);
  }

  private UserResponse toUserResponse(UsersRecord userRecord) {
    List<String> roles = getUserRoles(userRecord.getId());

    return UserResponse.builder()
        .id(userRecord.getId().toString())
        .username(userRecord.getUsername())
        .email(userRecord.getEmail())
        .firstName(userRecord.getFirstName())
        .lastName(userRecord.getLastName())
        .enabled(userRecord.getEnabled())
        .accountNonExpired(userRecord.getAccountNonExpired())
        .accountNonLocked(userRecord.getAccountNonLocked())
        .credentialsNonExpired(userRecord.getCredentialsNonExpired())
        .roles(roles)
        .createdAt(userRecord.getCreatedAt())
        .updatedAt(userRecord.getUpdatedAt())
        .build();
  }
}
