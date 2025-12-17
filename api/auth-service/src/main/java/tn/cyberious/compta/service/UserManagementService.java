package tn.cyberious.compta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.auth.generated.tables.pojos.Employees;
import tn.cyberious.compta.auth.generated.tables.pojos.Roles;
import tn.cyberious.compta.auth.generated.tables.pojos.Societes;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.dto.*;
import tn.cyberious.compta.enums.Role;
import tn.cyberious.compta.repository.*;
import tn.cyberious.compta.security.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final SocieteRepository societeRepository;
    private final EmployeeRepository employeeRepository;
    private final ComptableSocieteRepository comptableSocieteRepository;
    private final UserSocieteRepository userSocieteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Users createComptable(CreateUserRequest request, CustomUserDetails currentUser) {
        log.info("Creating comptable user: {} by user: {}", request.getUsername(), currentUser.getUsername());

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
        log.info("Creating societe user: {} by user: {}", request.getUsername(), currentUser.getUsername());

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
        log.info("Creating employee user: {} by user: {}", request.getUsername(), currentUser.getUsername());

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

    @Transactional
    public Societes createSociete(CreateSocieteRequest request, CustomUserDetails currentUser) {
        log.info("Creating societe: {} by user: {}", request.getRaisonSociale(), currentUser.getUsername());

        // Vérifier que le matricule fiscal n'existe pas déjà
        if (societeRepository.existsByMatriculeFiscale(request.getMatriculeFiscale())) {
            throw new RuntimeException("Societe with this matricule fiscale already exists");
        }

        // Créer la société
        Societes societe = new Societes();
        societe.setRaisonSociale(request.getRaisonSociale());
        societe.setMatriculeFiscale(request.getMatriculeFiscale());
        societe.setCodeTva(request.getCodeTva());
        societe.setCodeDouane(request.getCodeDouane());
        societe.setRegistreCommerce(request.getRegistreCommerce());
        societe.setFormeJuridique(request.getFormeJuridique());
        societe.setCapitalSocial(request.getCapitalSocial());
        societe.setDateCreation(request.getDateCreation());
        societe.setAdresse(request.getAdresse());
        societe.setVille(request.getVille());
        societe.setCodePostal(request.getCodePostal());
        societe.setTelephone(request.getTelephone());
        societe.setFax(request.getFax());
        societe.setEmail(request.getEmail());
        societe.setSiteWeb(request.getSiteWeb());
        societe.setActivite(request.getActivite());
        societe.setSecteur(request.getSecteur());
        societe.setIsActive(true);
        societe.setCreatedBy(currentUser.getId());
        Societes createdSociete = societeRepository.insert(societe);

        log.info("Societe created successfully: {}", request.getRaisonSociale());
        return createdSociete;
    }

    @Transactional
    public void createEmployee(CreateEmployeeRequest request, CustomUserDetails currentUser) {
        log.info("Creating employee link for user: {} to societe: {}", request.getUserId(), request.getSocieteId());

        // Vérifier que l'utilisateur existe et a le rôle EMPLOYEE
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier que la société existe
        Societes societe = societeRepository.findById(request.getSocieteId())
                .orElseThrow(() -> new RuntimeException("Societe not found"));

        // Créer l'association employee
        Employees employee = new Employees();
        employee.setUserId(request.getUserId());
        employee.setSocieteId(request.getSocieteId());
        employee.setMatriculeEmployee(request.getMatriculeEmployee());
        employee.setPoste(request.getPoste());
        employee.setDepartement(request.getDepartement());
        employee.setDateEmbauche(request.getDateEmbauche());
        employee.setDateFinContrat(request.getDateFinContrat());
        employee.setTypeContrat(request.getTypeContrat());
        employee.setIsActive(true);
        employeeRepository.insert(employee);

        log.info("Employee link created successfully");
    }

    private boolean userExists(String username, String email) {
        return userRepository.findByUsername(username).isPresent()
                || userRepository.findByEmail(email).isPresent();
    }

    private void assignRole(Long userId, Role role) {
        Roles roleEntity = roleRepository.findByName(role.getName())
                .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName()));

        userRoleRepository.assignRole(userId, roleEntity.getId());
    }

    // ==================== User CRUD Operations ====================

    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        List<Users> users = userRepository.findAll();
        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, CustomUserDetails currentUser) {
        log.info("Updating user {} by {}", id, currentUser.getUsername());

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                var existingUser = userRepository.findByEmail(request.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    throw new RuntimeException("Email already in use");
                }
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

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
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(true);
        userRepository.update(user);
    }

    @Transactional
    public void deactivateUser(Long id) {
        log.info("Deactivating user: {}", id);
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(false);
        userRepository.update(user);
    }

    @Transactional
    public void unlockUser(Long id) {
        log.info("Unlocking user: {}", id);
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.update(user);
    }

    // ==================== Role Management ====================

    public List<String> getUserRoles(Long userId) {
        log.info("Getting roles for user: {}", userId);
        List<Role> roles = userRepository.findRolesByUserId(userId);
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignRole(Long userId, String roleName, CustomUserDetails currentUser) {
        log.info("Assigning role {} to user {} by {}", roleName, userId, currentUser.getUsername());

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = Role.fromName(roleName);
        assignRole(userId, role);
    }

    @Transactional
    public void removeRole(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        userRoleRepository.removeRole(userId, roleId);
    }

    // ==================== Societe CRUD Operations ====================

    public List<Societes> getAllSocietes() {
        log.info("Getting all societes");
        return societeRepository.findAll();
    }

    public Societes getSocieteById(Long id) {
        log.info("Getting societe by id: {}", id);
        return societeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Societe not found"));
    }

    @Transactional
    public Societes updateSociete(Long id, UpdateSocieteRequest request, CustomUserDetails currentUser) {
        log.info("Updating societe {} by {}", id, currentUser.getUsername());

        Societes societe = societeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Societe not found"));

        if (request.getRaisonSociale() != null) societe.setRaisonSociale(request.getRaisonSociale());
        if (request.getCodeTva() != null) societe.setCodeTva(request.getCodeTva());
        if (request.getCodeDouane() != null) societe.setCodeDouane(request.getCodeDouane());
        if (request.getRegistreCommerce() != null) societe.setRegistreCommerce(request.getRegistreCommerce());
        if (request.getFormeJuridique() != null) societe.setFormeJuridique(request.getFormeJuridique());
        if (request.getCapitalSocial() != null) societe.setCapitalSocial(request.getCapitalSocial());
        if (request.getDateCreation() != null) societe.setDateCreation(request.getDateCreation());
        if (request.getAdresse() != null) societe.setAdresse(request.getAdresse());
        if (request.getVille() != null) societe.setVille(request.getVille());
        if (request.getCodePostal() != null) societe.setCodePostal(request.getCodePostal());
        if (request.getTelephone() != null) societe.setTelephone(request.getTelephone());
        if (request.getFax() != null) societe.setFax(request.getFax());
        if (request.getEmail() != null) societe.setEmail(request.getEmail());
        if (request.getSiteWeb() != null) societe.setSiteWeb(request.getSiteWeb());
        if (request.getActivite() != null) societe.setActivite(request.getActivite());
        if (request.getSecteur() != null) societe.setSecteur(request.getSecteur());
        if (request.getIsActive() != null) societe.setIsActive(request.getIsActive());

        societe.setUpdatedBy(currentUser.getId());

        return societeRepository.update(societe);
    }

    @Transactional
    public void deleteSociete(Long id) {
        log.info("Deleting societe: {}", id);
        if (!societeRepository.exists(id)) {
            throw new RuntimeException("Societe not found");
        }
        societeRepository.delete(id);
    }

    public List<UserResponse> getSocieteUsers(Long societeId) {
        log.info("Getting users for societe: {}", societeId);
        List<Users> users = userSocieteRepository.findUsersBySocieteId(societeId);
        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public List<Employees> getSocieteEmployees(Long societeId) {
        log.info("Getting employees for societe: {}", societeId);
        return employeeRepository.findBySocieteId(societeId);
    }

    public List<Societes> getUserSocietes(Long userId) {
        log.info("Getting societes for user: {}", userId);
        return userSocieteRepository.findSocietesByUserId(userId);
    }

    // ==================== Comptable-Societe Associations ====================

    @Transactional
    public void assignComptableToSociete(ComptableSocieteRequest request, CustomUserDetails currentUser) {
        log.info("Assigning comptable {} to societe {} by {}",
                request.getUserId(), request.getSocieteId(), currentUser.getUsername());

        // Verify user exists and has COMPTABLE role
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify societe exists
        Societes societe = societeRepository.findById(request.getSocieteId())
                .orElseThrow(() -> new RuntimeException("Societe not found"));

        comptableSocieteRepository.assignComptableToSociete(
                request.getUserId(),
                request.getSocieteId(),
                request.getDateDebut(),
                request.getDateFin()
        );
    }

    @Transactional
    public void removeComptableFromSociete(Long userId, Long societeId) {
        log.info("Removing comptable {} from societe {}", userId, societeId);
        comptableSocieteRepository.removeComptableFromSociete(userId, societeId);
    }

    // ==================== User-Societe Associations ====================

    @Transactional
    public void assignUserToSociete(UserSocieteRequest request, CustomUserDetails currentUser) {
        log.info("Assigning user {} to societe {} by {}",
                request.getUserId(), request.getSocieteId(), currentUser.getUsername());

        // Verify user exists
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify societe exists
        Societes societe = societeRepository.findById(request.getSocieteId())
                .orElseThrow(() -> new RuntimeException("Societe not found"));

        userSocieteRepository.assignUserToSociete(
                request.getUserId(),
                request.getSocieteId(),
                request.getIsOwner(),
                request.getDateDebut(),
                request.getDateFin()
        );
    }

    @Transactional
    public void removeUserFromSociete(Long userId, Long societeId) {
        log.info("Removing user {} from societe {}", userId, societeId);
        userSocieteRepository.removeUserFromSociete(userId, societeId);
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
