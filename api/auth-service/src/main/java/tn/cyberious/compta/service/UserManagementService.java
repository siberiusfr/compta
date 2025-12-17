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
import tn.cyberious.compta.dto.CreateEmployeeRequest;
import tn.cyberious.compta.dto.CreateSocieteRequest;
import tn.cyberious.compta.dto.CreateUserRequest;
import tn.cyberious.compta.enums.Role;
import tn.cyberious.compta.repository.*;
import tn.cyberious.compta.security.CustomUserDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final SocieteRepository societeRepository;
    private final EmployeeRepository employeeRepository;
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
}
