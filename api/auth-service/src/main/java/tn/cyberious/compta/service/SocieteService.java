package tn.cyberious.compta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.auth.generated.tables.pojos.Employees;
import tn.cyberious.compta.auth.generated.tables.pojos.Societes;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.dto.*;
import tn.cyberious.compta.exception.ResourceNotFoundException;
import tn.cyberious.compta.repository.*;
import tn.cyberious.compta.security.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocieteService {

    private final SocieteRepository societeRepository;
    private final UserRepository userRepository;
    private final UserSocieteRepository userSocieteRepository;
    private final ComptableSocieteRepository comptableSocieteRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityService securityService;

    // ==================== Societe CRUD Operations ====================

    public List<Societes> getAllSocietes(CustomUserDetails currentUser) {
        log.info("Getting all societes by user: {}", currentUser.getId());

        // ADMIN voit toutes les sociétés
        List<Long> accessibleSocieteIds = securityService.getAccessibleSocieteIds(currentUser);
        if (accessibleSocieteIds == null) {
            return societeRepository.findAll();
        }

        // Filtrer par les sociétés accessibles
        return societeRepository.findAll().stream()
                .filter(s -> accessibleSocieteIds.contains(s.getId()))
                .collect(Collectors.toList());
    }

    public Societes getSocieteById(Long id, CustomUserDetails currentUser) {
        log.info("Getting societe {} by user: {}", id, currentUser.getId());

        // Vérifier les permissions
        securityService.checkSocieteAccess(currentUser, id);

        return societeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Société non trouvée"));
    }

    @Transactional
    public Societes createSociete(CreateSocieteRequest request, CustomUserDetails currentUser) {
        log.info("Creating societe: {} by user: {}", request.getRaisonSociale(), currentUser.getUsername());

        // Vérifier que le matricule fiscal n'existe pas déjà
        if (societeRepository.existsByMatriculeFiscale(request.getMatriculeFiscale())) {
            throw new RuntimeException("Une société avec ce matricule fiscal existe déjà");
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
    public Societes updateSociete(Long id, UpdateSocieteRequest request, CustomUserDetails currentUser) {
        log.info("Updating societe {} by {}", id, currentUser.getUsername());

        // Vérifier les permissions d'écriture
        securityService.checkSocieteWriteAccess(currentUser, id);

        Societes societe = societeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Société non trouvée"));

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
    public void deleteSociete(Long id, CustomUserDetails currentUser) {
        log.info("Deleting societe {} by {}", id, currentUser.getId());

        // Vérifier les permissions d'écriture
        securityService.checkSocieteWriteAccess(currentUser, id);

        if (!societeRepository.exists(id)) {
            throw new ResourceNotFoundException("Société non trouvée");
        }
        societeRepository.delete(id);
    }

    // ==================== Societe Relations ====================

    public List<UserResponse> getSocieteUsers(Long societeId, CustomUserDetails currentUser) {
        log.info("Getting users for societe {} by user: {}", societeId, currentUser.getId());

        // Vérifier les permissions
        securityService.checkSocieteAccess(currentUser, societeId);

        List<Users> users = userSocieteRepository.findUsersBySocieteId(societeId);
        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public List<Employees> getSocieteEmployees(Long societeId, CustomUserDetails currentUser) {
        log.info("Getting employees for societe {} by user: {}", societeId, currentUser.getId());

        // Vérifier les permissions
        securityService.checkSocieteAccess(currentUser, societeId);

        return employeeRepository.findBySocieteId(societeId);
    }

    public List<Societes> getUserSocietes(Long userId, CustomUserDetails currentUser) {
        log.info("Getting societes for user {} by user: {}", userId, currentUser.getId());

        // ADMIN peut voir toutes les sociétés de n'importe quel utilisateur
        if (!securityService.isAdmin(currentUser)) {
            // Les autres doivent avoir accès à l'utilisateur
            securityService.checkUserAccess(currentUser, userId);
        }

        return userSocieteRepository.findSocietesByUserId(userId);
    }

    // ==================== Comptable-Societe Associations ====================

    @Transactional
    public void assignComptableToSociete(ComptableSocieteRequest request, CustomUserDetails currentUser) {
        log.info("Assigning comptable {} to societe {} by {}",
                request.getUserId(), request.getSocieteId(), currentUser.getUsername());

        // Verify user exists and has COMPTABLE role
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Verify societe exists
        Societes societe = societeRepository.findById(request.getSocieteId())
                .orElseThrow(() -> new ResourceNotFoundException("Société non trouvée"));

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

        // Vérifier les permissions sur la société
        securityService.checkSocieteWriteAccess(currentUser, request.getSocieteId());

        // Verify user exists
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Verify societe exists
        Societes societe = societeRepository.findById(request.getSocieteId())
                .orElseThrow(() -> new ResourceNotFoundException("Société non trouvée"));

        userSocieteRepository.assignUserToSociete(
                request.getUserId(),
                request.getSocieteId(),
                request.getIsOwner(),
                request.getDateDebut(),
                request.getDateFin()
        );
    }

    @Transactional
    public void removeUserFromSociete(Long userId, Long societeId, CustomUserDetails currentUser) {
        log.info("Removing user {} from societe {} by {}", userId, societeId, currentUser.getId());

        // Vérifier les permissions sur la société
        securityService.checkSocieteWriteAccess(currentUser, societeId);

        userSocieteRepository.removeUserFromSociete(userId, societeId);
    }

    // ==================== Helper Methods ====================

    private UserResponse toUserResponse(Users user) {
        List<String> roles = userRepository.findRolesByUserId(user.getId())
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

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
