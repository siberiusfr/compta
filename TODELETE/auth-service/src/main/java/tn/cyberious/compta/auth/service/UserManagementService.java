package tn.cyberious.compta.auth.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.auth.dto.*;
import tn.cyberious.compta.auth.enums.Role;
import tn.cyberious.compta.auth.generated.tables.pojos.Roles;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.auth.repository.*;
import tn.cyberious.compta.auth.security.CustomUserDetails;
import tn.cyberious.compta.exception.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;
  private final SecurityService securityService;

  @Transactional
  public Users createComptable(CreateUserRequest request, CustomUserDetails currentUser) {
    log.info(
        "Creating comptable user: {} by user: {}",
        request.getUsername(),
        currentUser.getUsername());

    // Vérifier que l'utilisateur n'existe pas déjà
    if (userExists(request.getUsername(), request.getEmail())) {
      throw new RuntimeException("User with username or email already exists");
    }

    // Créer l'utilisateur
    Users user = new Users();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setPhone(request.getPhone());
    user.setIsActive(true);
    user.setIsLocked(false);
    user.setCreatedBy(currentUser.getId());
    Users createdUser = userRepository.insert(user);

    // Assigner le rôle COMPTABLE
    assignRole(createdUser.getId(), Role.COMPTABLE);

    log.info("Comptable user created successfully: {}", request.getUsername());
    return createdUser;
  }

  @Transactional
  public Users createSocieteUser(CreateUserRequest request, CustomUserDetails currentUser) {
    log.info(
        "Creating societe user: {} by user: {}", request.getUsername(), currentUser.getUsername());

    // Vérifier que l'utilisateur n'existe pas déjà
    if (userExists(request.getUsername(), request.getEmail())) {
      throw new RuntimeException("User with username or email already exists");
    }

    // Créer l'utilisateur
    Users user = new Users();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setPhone(request.getPhone());
    user.setIsActive(true);
    user.setIsLocked(false);
    user.setCreatedBy(currentUser.getId());
    Users createdUser = userRepository.insert(user);

    // Assigner le rôle SOCIETE
    assignRole(createdUser.getId(), Role.SOCIETE);

    log.info("Societe user created successfully: {}", request.getUsername());
    return createdUser;
  }

  @Transactional
  public Users createEmployeeUser(CreateUserRequest request, CustomUserDetails currentUser) {
    log.info(
        "Creating employee user: {} by user: {}", request.getUsername(), currentUser.getUsername());

    // Vérifier que l'utilisateur n'existe pas déjà
    if (userExists(request.getUsername(), request.getEmail())) {
      throw new RuntimeException("User with username or email already exists");
    }

    // Créer l'utilisateur
    Users user = new Users();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setPhone(request.getPhone());
    user.setIsActive(true);
    user.setIsLocked(false);
    user.setCreatedBy(currentUser.getId());
    Users createdUser = userRepository.insert(user);

    // Assigner le rôle EMPLOYEE
    assignRole(createdUser.getId(), Role.EMPLOYEE);

    log.info("Employee user created successfully: {}", request.getUsername());
    return createdUser;
  }

  private boolean userExists(String username, String email) {
    return userRepository.findByUsername(username).isPresent()
        || userRepository.findByEmail(email).isPresent();
  }

  private void assignRole(Long userId, Role role) {
    Roles roleEntity =
        roleRepository
            .findByName(role.getName())
            .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName()));

    userRoleRepository.assignRole(userId, roleEntity.getId());
  }

  // ==================== User CRUD Operations ====================

  public List<UserResponse> getAllUsers(CustomUserDetails currentUser) {
    log.info("Getting all users by user: {}", currentUser.getId());

    // ADMIN voit tous les utilisateurs
    if (securityService.isAdmin(currentUser)) {
      List<Users> users = userRepository.findAll();
      return users.stream().map(this::toUserResponse).toList();
    }
    // Autres rôles : accès refusé
    return List.of();
  }

  public UserResponse getUserById(Long id, CustomUserDetails currentUser) {
    log.info("Getting user {} by user: {}", id, currentUser.getId());

    // Vérifier les permissions
    if (!securityService.isAdmin(currentUser)) {
      return null;
    }

    Users user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    return toUserResponse(user);
  }

  @Transactional
  public UserResponse updateUser(
      Long id, UpdateUserRequest request, CustomUserDetails currentUser) {
    log.info("Updating user {} by {}", id, currentUser.getUsername());

    Users user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    if (request.getEmail() != null && !request.getEmail().isEmpty()) {
      if (userRepository.existsByEmail(request.getEmail())) {
        var existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
          throw new RuntimeException("Email already in use");
        }
      }
      user.setEmail(request.getEmail());
    }

    if (request.getFirstName() != null) {
      user.setFirstName(request.getFirstName());
    }
    if (request.getLastName() != null) {
      user.setLastName(request.getLastName());
    }
    if (request.getPhone() != null) {
      user.setPhone(request.getPhone());
    }

    user.setUpdatedBy(currentUser.getId());

    Users updatedUser = userRepository.update(user);
    return toUserResponse(updatedUser);
  }

  @Transactional
  public void deleteUser(Long id) {
    log.info("Deleting user: {}", id);
    if (!userRepository.exists(id)) {
      throw new RuntimeException("User not found");
    }
    userRepository.delete(id);
  }

  @Transactional
  public void activateUser(Long id) {
    log.info("Activating user: {}", id);
    Users user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    user.setIsActive(true);
    userRepository.update(user);
  }

  @Transactional
  public void deactivateUser(Long id) {
    log.info("Deactivating user: {}", id);
    Users user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    user.setIsActive(false);
    userRepository.update(user);
  }

  @Transactional
  public void unlockUser(Long id) {
    log.info("Unlocking user: {}", id);
    Users user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    user.setIsLocked(false);
    user.setFailedLoginAttempts(0);
    userRepository.update(user);
  }

  // ==================== Role Management ====================

  public List<String> getUserRoles(Long userId) {
    log.info("Getting roles for user: {}", userId);
    List<Role> roles = userRepository.findRolesByUserId(userId);
    return roles.stream().map(Role::getName).collect(Collectors.toList());
  }

  @Transactional
  public void assignRole(Long userId, String roleName, CustomUserDetails currentUser) {
    log.info("Assigning role {} to user {} by {}", roleName, userId, currentUser.getUsername());

    Users user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    Role role = Role.fromName(roleName);
    assignRole(userId, role);
  }

  @Transactional
  public void removeRole(Long userId, Long roleId) {
    log.info("Removing role {} from user {}", roleId, userId);
    userRoleRepository.removeRole(userId, roleId);
  }

  // ==================== Helper Methods ====================

  private UserResponse toUserResponse(Users user) {
    List<String> roles = getUserRoles(user.getId());

    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .phone(user.getPhone())
        .isActive(user.getIsActive())
        .isLocked(user.getIsLocked())
        .failedLoginAttempts(user.getFailedLoginAttempts())
        .lastLoginAt(user.getLastLoginAt())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .roles(roles)
        .build();
  }
}
