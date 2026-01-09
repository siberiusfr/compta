package tn.cyberious.compta.oauth2.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.oauth2.dto.ChangePasswordRequest;
import tn.cyberious.compta.oauth2.dto.CreateUserRequest;
import tn.cyberious.compta.oauth2.dto.UpdateUserRequest;
import tn.cyberious.compta.oauth2.dto.UserResponse;
import tn.cyberious.compta.oauth2.generated.tables.records.UsersRecord;
import tn.cyberious.compta.oauth2.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    log.info("Creating new user with username: {}", request.getUsername());

    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists: " + request.getUsername());
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists: " + request.getEmail());
    }

    UsersRecord userRecord =
        userRepository.insertUser(
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName());

    if (request.getRoles() != null && !request.getRoles().isEmpty()) {
      assignRolesToUser(userRecord.getId(), request.getRoles());
    }

    log.info("Successfully created user with username: {}", request.getUsername());
    return toUserResponse(userRecord);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    log.debug("Retrieving all users");
    return userRepository.findAll().stream().map(this::toUserResponse).toList();
  }

  @Transactional(readOnly = true)
  public UserResponse getUserById(UUID userId) {
    log.debug("Retrieving user with id: {}", userId);
    var userRecord =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    return toUserResponse(userRecord);
  }

  @Transactional(readOnly = true)
  public UserResponse getUserByUsername(String username) {
    log.debug("Retrieving user with username: {}", username);
    var userRecord =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new IllegalArgumentException("User not found with username: " + username));

    return toUserResponse(userRecord);
  }

  @Transactional
  public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
    log.info("Updating user with id: {}", userId);

    var existingUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
      if (userRepository.existsByEmailExcludingId(request.getEmail(), userId)) {
        throw new IllegalArgumentException("Email already exists: " + request.getEmail());
      }
    }

    userRepository.updateFields(
        userId, request.getFirstName(), request.getLastName(), request.getEmail());

    if (request.getRoles() != null) {
      userRepository.deleteUserRoles(userId);
      assignRolesToUser(userId, request.getRoles());
    }

    var updatedUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found after update"));

    log.info("Successfully updated user with id: {}", userId);
    return toUserResponse(updatedUser);
  }

  @Transactional
  public void deleteUser(UUID userId) {
    log.info("Deleting user with id: {}", userId);

    if (userRepository.findById(userId).isEmpty()) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    userRepository.delete(userId);

    log.info("Successfully deleted user with id: {}", userId);
  }

  @Transactional
  public void disableUser(UUID userId) {
    log.info("Disabling user with id: {}", userId);

    int updated = userRepository.updateEnabled(userId, false);

    if (updated == 0) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    log.info("Successfully disabled user with id: {}", userId);
  }

  @Transactional
  public void enableUser(UUID userId) {
    log.info("Enabling user with id: {}", userId);

    int updated = userRepository.updateEnabled(userId, true);

    if (updated == 0) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    log.info("Successfully enabled user with id: {}", userId);
  }

  @Transactional(readOnly = true)
  public List<String> getUserRoles(UUID userId) {
    return userRepository.getUserRoles(userId);
  }

  @Transactional
  public void assignRolesToUser(UUID userId, List<String> roleNames) {
    log.info("Assigning roles to user with id: {}", userId);

    userRepository.deleteUserRoles(userId);

    for (String roleName : roleNames) {
      var roleId = userRepository.findRoleIdByName(roleName);

      if (roleId.isPresent()) {
        userRepository.insertUserRole(userId, roleId.get());
      } else {
        log.warn("Role not found: {}", roleName);
      }
    }

    log.info("Successfully assigned roles to user with id: {}", userId);
  }

  @Transactional
  public void removeRoleFromUser(UUID userId, UUID roleId) {
    log.info("Removing role {} from user with id: {}", roleId, userId);

    int deleted = userRepository.deleteUserRole(userId, roleId);

    if (deleted == 0) {
      throw new IllegalArgumentException("User role not found");
    }

    log.info("Successfully removed role {} from user with id: {}", roleId, userId);
  }

  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequest request) {
    log.info("Changing password for user with id: {}", userId);

    var userRecord =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    if (!passwordEncoder.matches(request.getCurrentPassword(), userRecord.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }

    userRepository.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));

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
