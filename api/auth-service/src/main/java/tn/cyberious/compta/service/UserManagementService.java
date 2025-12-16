package tn.cyberious.compta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.auth.generated.tables.pojos.Societes;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.auth.generated.tables.records.EmployeesRecord;
import tn.cyberious.compta.auth.generated.tables.records.SocietesRecord;
import tn.cyberious.compta.auth.generated.tables.records.UserRolesRecord;
import tn.cyberious.compta.auth.generated.tables.records.UsersRecord;
import tn.cyberious.compta.dto.CreateEmployeeRequest;
import tn.cyberious.compta.dto.CreateSocieteRequest;
import tn.cyberious.compta.dto.CreateUserRequest;
import tn.cyberious.compta.security.CustomUserDetails;

import java.time.LocalDateTime;

import static tn.cyberious.compta.auth.generated.Tables.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Users createComptable(CreateUserRequest request, CustomUserDetails currentUser) {
        log.info("Creating comptable user: {} by user: {}", request.getUsername(), currentUser.getUsername());

        // Vérifier que l'utilisateur n'existe pas déjà
        if (userExists(request.getUsername(), request.getEmail())) {
            throw new RuntimeException("User with username or email already exists");
        }

        // Créer l'utilisateur
        UsersRecord userRecord = dsl.newRecord(USERS);
        userRecord.setUsername(request.getUsername());
        userRecord.setEmail(request.getEmail());
        userRecord.setPassword(passwordEncoder.encode(request.getPassword()));
        userRecord.setFirstName(request.getFirstName());
        userRecord.setLastName(request.getLastName());
        userRecord.setPhone(request.getPhone());
        userRecord.setIsActive(true);
        userRecord.setIsLocked(false);
        userRecord.setCreatedBy(currentUser.getId());
        userRecord.store();

        // Assigner le rôle COMPTABLE
        assignRole(userRecord.getId(), "COMPTABLE");

        log.info("Comptable user created successfully: {}", request.getUsername());
        return userRecord.into(Users.class);
    }

    @Transactional
    public Users createSocieteUser(CreateUserRequest request, CustomUserDetails currentUser) {
        log.info("Creating societe user: {} by user: {}", request.getUsername(), currentUser.getUsername());

        // Vérifier que l'utilisateur n'existe pas déjà
        if (userExists(request.getUsername(), request.getEmail())) {
            throw new RuntimeException("User with username or email already exists");
        }

        // Créer l'utilisateur
        UsersRecord userRecord = dsl.newRecord(USERS);
        userRecord.setUsername(request.getUsername());
        userRecord.setEmail(request.getEmail());
        userRecord.setPassword(passwordEncoder.encode(request.getPassword()));
        userRecord.setFirstName(request.getFirstName());
        userRecord.setLastName(request.getLastName());
        userRecord.setPhone(request.getPhone());
        userRecord.setIsActive(true);
        userRecord.setIsLocked(false);
        userRecord.setCreatedBy(currentUser.getId());
        userRecord.store();

        // Assigner le rôle SOCIETE
        assignRole(userRecord.getId(), "SOCIETE");

        log.info("Societe user created successfully: {}", request.getUsername());
        return userRecord.into(Users.class);
    }

    @Transactional
    public Users createEmployeeUser(CreateUserRequest request, CustomUserDetails currentUser) {
        log.info("Creating employee user: {} by user: {}", request.getUsername(), currentUser.getUsername());

        // Vérifier que l'utilisateur n'existe pas déjà
        if (userExists(request.getUsername(), request.getEmail())) {
            throw new RuntimeException("User with username or email already exists");
        }

        // Créer l'utilisateur
        UsersRecord userRecord = dsl.newRecord(USERS);
        userRecord.setUsername(request.getUsername());
        userRecord.setEmail(request.getEmail());
        userRecord.setPassword(passwordEncoder.encode(request.getPassword()));
        userRecord.setFirstName(request.getFirstName());
        userRecord.setLastName(request.getLastName());
        userRecord.setPhone(request.getPhone());
        userRecord.setIsActive(true);
        userRecord.setIsLocked(false);
        userRecord.setCreatedBy(currentUser.getId());
        userRecord.store();

        // Assigner le rôle EMPLOYEE
        assignRole(userRecord.getId(), "EMPLOYEE");

        log.info("Employee user created successfully: {}", request.getUsername());
        return userRecord.into(Users.class);
    }

    @Transactional
    public Societes createSociete(CreateSocieteRequest request, CustomUserDetails currentUser) {
        log.info("Creating societe: {} by user: {}", request.getRaisonSociale(), currentUser.getUsername());

        // Vérifier que le matricule fiscal n'existe pas déjà
        var existing = dsl.selectFrom(SOCIETES)
                .where(SOCIETES.MATRICULE_FISCALE.eq(request.getMatriculeFiscale()))
                .fetchOne();

        if (existing != null) {
            throw new RuntimeException("Societe with this matricule fiscale already exists");
        }

        // Créer la société
        SocietesRecord societeRecord = dsl.newRecord(SOCIETES);
        societeRecord.setRaisonSociale(request.getRaisonSociale());
        societeRecord.setMatriculeFiscale(request.getMatriculeFiscale());
        societeRecord.setCodeTva(request.getCodeTva());
        societeRecord.setCodeDouane(request.getCodeDouane());
        societeRecord.setRegistreCommerce(request.getRegistreCommerce());
        societeRecord.setFormeJuridique(request.getFormeJuridique());
        societeRecord.setCapitalSocial(request.getCapitalSocial());
        societeRecord.setDateCreation(request.getDateCreation());
        societeRecord.setAdresse(request.getAdresse());
        societeRecord.setVille(request.getVille());
        societeRecord.setCodePostal(request.getCodePostal());
        societeRecord.setTelephone(request.getTelephone());
        societeRecord.setFax(request.getFax());
        societeRecord.setEmail(request.getEmail());
        societeRecord.setSiteWeb(request.getSiteWeb());
        societeRecord.setActivite(request.getActivite());
        societeRecord.setSecteur(request.getSecteur());
        societeRecord.setIsActive(true);
        societeRecord.setCreatedBy(currentUser.getId());
        societeRecord.store();

        log.info("Societe created successfully: {}", request.getRaisonSociale());
        return societeRecord.into(Societes.class);
    }

    @Transactional
    public void createEmployee(CreateEmployeeRequest request, CustomUserDetails currentUser) {
        log.info("Creating employee link for user: {} to societe: {}", request.getUserId(), request.getSocieteId());

        // Vérifier que l'utilisateur existe et a le rôle EMPLOYEE
        var userRecord = dsl.selectFrom(USERS)
                .where(USERS.ID.eq(request.getUserId()))
                .fetchOne();

        if (userRecord == null) {
            throw new RuntimeException("User not found");
        }

        // Vérifier que la société existe
        var societeRecord = dsl.selectFrom(SOCIETES)
                .where(SOCIETES.ID.eq(request.getSocieteId()))
                .fetchOne();

        if (societeRecord == null) {
            throw new RuntimeException("Societe not found");
        }

        // Créer l'association employee
        EmployeesRecord employeeRecord = dsl.newRecord(EMPLOYEES);
        employeeRecord.setUserId(request.getUserId());
        employeeRecord.setSocieteId(request.getSocieteId());
        employeeRecord.setMatriculeEmployee(request.getMatriculeEmployee());
        employeeRecord.setPoste(request.getPoste());
        employeeRecord.setDepartement(request.getDepartement());
        employeeRecord.setDateEmbauche(request.getDateEmbauche());
        employeeRecord.setDateFinContrat(request.getDateFinContrat());
        employeeRecord.setTypeContrat(request.getTypeContrat());
        employeeRecord.setIsActive(true);
        employeeRecord.store();

        log.info("Employee link created successfully");
    }

    private boolean userExists(String username, String email) {
        return dsl.fetchExists(
                dsl.selectFrom(USERS)
                        .where(USERS.USERNAME.eq(username).or(USERS.EMAIL.eq(email)))
        );
    }

    private void assignRole(Long userId, String roleName) {
        var roleRecord = dsl.selectFrom(ROLES)
                .where(ROLES.NAME.eq(roleName))
                .fetchOne();

        if (roleRecord == null) {
            throw new RuntimeException("Role not found: " + roleName);
        }

        UserRolesRecord userRoleRecord = dsl.newRecord(USER_ROLES);
        userRoleRecord.setUserId(userId);
        userRoleRecord.setRoleId(roleRecord.getId());
        userRoleRecord.store();
    }
}
