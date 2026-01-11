package tn.cyberious.compta.authz.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.authz.config.CacheConfig;
import tn.cyberious.compta.authz.dto.SocieteAccessDto;
import tn.cyberious.compta.authz.dto.UserAccessDto;
import tn.cyberious.compta.authz.dto.UserAccessDto.AccessType;
import tn.cyberious.compta.authz.generated.tables.pojos.ComptableSocietes;
import tn.cyberious.compta.authz.generated.tables.pojos.Permissions;
import tn.cyberious.compta.authz.repository.ComptableSocietesRepository;
import tn.cyberious.compta.authz.repository.PermissionRepository;
import tn.cyberious.compta.authz.repository.RolePermissionRepository;
import tn.cyberious.compta.authz.repository.SocieteRepository;
import tn.cyberious.compta.authz.repository.UserSocieteComptableRepository;
import tn.cyberious.compta.authz.repository.UserSocietesRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessService {

    private final ComptableSocietesRepository comptableSocietesRepository;
    private final UserSocietesRepository userSocietesRepository;
    private final UserSocieteComptableRepository userSocieteComptableRepository;
    private final SocieteRepository societeRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * Verifie si un utilisateur a acces a une societe.
     * L'acces peut etre:
     * - Via comptable_societes (comptable avec acces a la societe cliente)
     * - Via user_societes (employe/manager de la societe)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_ACCESS_CACHE, key = "#userId + '-' + #societeId")
    public UserAccessDto getUserAccess(Long userId, Long societeId) {
        log.debug("Verification acces pour userId={}, societeId={}", userId, societeId);

        // 1. Verifier si l'utilisateur est membre de la societe (user_societes)
        Optional<String> memberRole = userSocietesRepository.findRoleByUserIdAndSocieteId(userId, societeId);
        if (memberRole.isPresent()) {
            log.debug("Utilisateur {} est membre de la societe {} avec role {}",
                    userId, societeId, memberRole.get());
            return UserAccessDto.builder()
                    .userId(userId)
                    .societeId(societeId)
                    .hasAccess(true)
                    .accessType(AccessType.MEMBRE)
                    .role(memberRole.get())
                    .canRead(true)
                    .canWrite(true)
                    .canValidate("MANAGER".equals(memberRole.get()) || "FINANCE".equals(memberRole.get()))
                    .build();
        }

        // 2. Verifier si l'utilisateur est un comptable avec acces (comptable_societes)
        Optional<ComptableSocietes> comptableAccess =
                comptableSocietesRepository.findActiveAccessByUserIdAndSocieteId(userId, societeId);
        if (comptableAccess.isPresent()) {
            ComptableSocietes access = comptableAccess.get();
            // Recuperer le role du comptable dans son cabinet
            String comptableRole = userSocieteComptableRepository.findByUserId(userId)
                    .map(usc -> usc.getRole())
                    .orElse("COMPTABLE");

            log.debug("Utilisateur {} est comptable avec acces a la societe {}", userId, societeId);
            return UserAccessDto.builder()
                    .userId(userId)
                    .societeId(societeId)
                    .hasAccess(true)
                    .accessType(AccessType.COMPTABLE)
                    .role(comptableRole)
                    .canRead(access.getCanRead())
                    .canWrite(access.getCanWrite())
                    .canValidate(access.getCanValidate())
                    .build();
        }

        // 3. Pas d'acces
        log.debug("Utilisateur {} n'a pas acces a la societe {}", userId, societeId);
        return UserAccessDto.noAccess(userId, societeId);
    }

    /**
     * Verifie rapidement si un utilisateur a acces a une societe (sans details).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_ACCESS_CACHE, key = "'quick-' + #userId + '-' + #societeId")
    public boolean hasAccess(Long userId, Long societeId) {
        // Verifier membre de la societe
        if (userSocietesRepository.hasActiveAccess(userId, societeId)) {
            return true;
        }
        // Verifier comptable avec acces
        return comptableSocietesRepository.hasActiveAccess(userId, societeId);
    }

    /**
     * Verifie si un utilisateur a le droit d'ecriture sur une societe.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_ACCESS_CACHE, key = "'write-' + #userId + '-' + #societeId")
    public boolean hasWriteAccess(Long userId, Long societeId) {
        // Un membre de la societe a toujours le droit d'ecriture
        if (userSocietesRepository.hasActiveAccess(userId, societeId)) {
            return true;
        }
        // Verifier droit d'ecriture du comptable
        return comptableSocietesRepository.hasWriteAccess(userId, societeId);
    }

    /**
     * Verifie si un utilisateur a le droit de validation sur une societe.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_ACCESS_CACHE, key = "'validate-' + #userId + '-' + #societeId")
    public boolean hasValidateAccess(Long userId, Long societeId) {
        // Verifier si membre avec role MANAGER ou FINANCE
        Optional<String> memberRole = userSocietesRepository.findRoleByUserIdAndSocieteId(userId, societeId);
        if (memberRole.isPresent()) {
            String role = memberRole.get();
            return "MANAGER".equals(role) || "FINANCE".equals(role);
        }
        // Verifier droit de validation du comptable
        return comptableSocietesRepository.hasValidateAccess(userId, societeId);
    }

    /**
     * Recupere toutes les permissions d'un utilisateur pour une societe.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId + '-' + #societeId")
    public List<String> getUserPermissions(Long userId, Long societeId) {
        UserAccessDto access = getUserAccess(userId, societeId);
        if (!access.isHasAccess()) {
            return List.of();
        }

        List<String> permissions = new ArrayList<>();

        // Recuperer les permissions du role
        List<Permissions> rolePermissions = permissionRepository.findByRole(access.getRole());
        rolePermissions.forEach(p -> permissions.add(p.getCode()));

        // Ajouter les permissions basees sur les droits specifiques
        if (Boolean.TRUE.equals(access.getCanRead())) {
            permissions.add("READ");
        }
        if (Boolean.TRUE.equals(access.getCanWrite())) {
            permissions.add("WRITE");
        }
        if (Boolean.TRUE.equals(access.getCanValidate())) {
            permissions.add("VALIDATE");
        }

        return permissions;
    }

    /**
     * Verifie si un utilisateur a une permission specifique sur une societe.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long societeId, String permissionCode) {
        UserAccessDto access = getUserAccess(userId, societeId);
        if (!access.isHasAccess()) {
            return false;
        }

        // Verifier les permissions speciales
        if ("READ".equals(permissionCode)) {
            return Boolean.TRUE.equals(access.getCanRead());
        }
        if ("WRITE".equals(permissionCode)) {
            return Boolean.TRUE.equals(access.getCanWrite());
        }
        if ("VALIDATE".equals(permissionCode)) {
            return Boolean.TRUE.equals(access.getCanValidate());
        }

        // Verifier les permissions du role
        return rolePermissionRepository.hasPermission(access.getRole(), permissionCode);
    }

    /**
     * Recupere toutes les societes auxquelles un utilisateur a acces.
     * Combine les acces via comptable_societes et user_societes.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_ACCESS_CACHE, key = "'societes-' + #userId")
    public List<SocieteAccessDto> getAccessibleSocietes(Long userId) {
        List<SocieteAccessDto> result = new ArrayList<>();

        // 1. Societe dont l'utilisateur est membre (via user_societes)
        userSocietesRepository.findSocieteByUserId(userId).ifPresent(societe -> {
            String role = userSocietesRepository.findRoleByUserIdAndSocieteId(userId, societe.getId())
                    .orElse("VIEWER");
            result.add(SocieteAccessDto.builder()
                    .societeId(societe.getId())
                    .raisonSociale(societe.getRaisonSociale())
                    .matriculeFiscale(societe.getMatriculeFiscale())
                    .accessType(AccessType.MEMBRE)
                    .role(role)
                    .canRead(true)
                    .canWrite(true)
                    .canValidate("MANAGER".equals(role) || "FINANCE".equals(role))
                    .build());
        });

        // 2. Societes accessibles en tant que comptable (via comptable_societes)
        List<ComptableSocietes> comptableAccesses = comptableSocietesRepository.findActiveByUserId(userId);
        for (ComptableSocietes access : comptableAccesses) {
            // Eviter les doublons si l'utilisateur est aussi membre
            boolean alreadyAdded = result.stream()
                    .anyMatch(s -> s.getSocieteId().equals(access.getSocieteId()));
            if (!alreadyAdded) {
                // Recuperer les infos de la societe
                societeRepository.findById(access.getSocieteId()).ifPresent(societe -> {
                    String comptableRole = userSocieteComptableRepository.findByUserId(userId)
                            .map(usc -> usc.getRole())
                            .orElse("COMPTABLE");
                    result.add(SocieteAccessDto.builder()
                            .societeId(societe.getId())
                            .raisonSociale(societe.getRaisonSociale())
                            .matriculeFiscale(societe.getMatriculeFiscale())
                            .accessType(AccessType.COMPTABLE)
                            .role(comptableRole)
                            .canRead(access.getCanRead())
                            .canWrite(access.getCanWrite())
                            .canValidate(access.getCanValidate())
                            .build());
                });
            }
        }

        log.debug("Utilisateur {} a acces a {} societes", userId, result.size());
        return result;
    }

    /**
     * Recupere uniquement les societes sur lesquelles l'utilisateur a le droit d'ecriture.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_ACCESS_CACHE, key = "'societes-write-' + #userId")
    public List<SocieteAccessDto> getWriteAccessibleSocietes(Long userId) {
        return getAccessibleSocietes(userId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getCanWrite()))
                .toList();
    }

    /**
     * Invalide le cache d'acces pour un utilisateur.
     */
    @CacheEvict(value = CacheConfig.USER_ACCESS_CACHE, allEntries = true)
    public void evictUserAccessCache() {
        log.info("Cache d'acces utilisateur invalide");
    }

    /**
     * Invalide le cache d'acces pour un utilisateur specifique.
     */
    @CacheEvict(value = CacheConfig.USER_ACCESS_CACHE, key = "#userId + '-' + #societeId")
    public void evictUserAccess(Long userId, Long societeId) {
        log.info("Cache d'acces invalide pour userId={}, societeId={}", userId, societeId);
    }

    /**
     * Invalide le cache des permissions.
     */
    @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, allEntries = true)
    public void evictPermissionsCache() {
        log.info("Cache des permissions invalide");
    }
}
