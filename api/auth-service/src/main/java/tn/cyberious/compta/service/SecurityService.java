package tn.cyberious.compta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.enums.Role;
import tn.cyberious.compta.exception.ForbiddenException;
import tn.cyberious.compta.repository.*;
import tn.cyberious.compta.security.CustomUserDetails;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;
    private final SocieteRepository societeRepository;
    private final ComptableSocieteRepository comptableSocieteRepository;
    private final UserSocieteRepository userSocieteRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Vérifie si l'utilisateur courant a accès à une société
     */
    public void checkSocieteAccess(CustomUserDetails currentUser, Long societeId) {
        List<Role> roles = userRepository.findRolesByUserId(currentUser.getId());

        // ADMIN a accès à tout
        if (roles.contains(Role.ADMIN)) {
            return;
        }

        // COMPTABLE : vérifier l'attribution (requête directe)
        if (roles.contains(Role.COMPTABLE)) {
            List<Long> accessibleSocietes = societeRepository.findSocieteIdsByComptableId(currentUser.getId());

            if (!accessibleSocietes.contains(societeId)) {
                log.warn("Comptable {} attempted to access societe {} without permission",
                        currentUser.getId(), societeId);
                throw new ForbiddenException("Vous n'avez pas accès à cette société");
            }
            return;
        }

        // SOCIETE : vérifier si c'est sa société (requête directe)
        if (roles.contains(Role.SOCIETE)) {
            List<Long> accessibleSocietes = societeRepository.findSocieteIdsByUserSocieteId(currentUser.getId());

            if (!accessibleSocietes.contains(societeId)) {
                log.warn("User societe {} attempted to access societe {} without permission",
                        currentUser.getId(), societeId);
                throw new ForbiddenException("Vous n'avez pas accès à cette société");
            }
            return;
        }

        // EMPLOYEE : vérifier si c'est la société de son emploi (requête directe)
        if (roles.contains(Role.EMPLOYEE)) {
            List<Long> accessibleSocietes = societeRepository.findSocieteIdsByEmployeeId(currentUser.getId());

            if (!accessibleSocietes.contains(societeId)) {
                log.warn("Employee {} attempted to access societe {} without permission",
                        currentUser.getId(), societeId);
                throw new ForbiddenException("Vous n'avez pas accès à cette société");
            }
            return;
        }

        // Aucun rôle approprié
        throw new ForbiddenException("Vous n'avez pas les permissions nécessaires");
    }

    /**
     * Vérifie si l'utilisateur courant peut modifier une société
     */
    public void checkSocieteWriteAccess(CustomUserDetails currentUser, Long societeId) {
        List<Role> roles = userRepository.findRolesByUserId(currentUser.getId());

        // ADMIN peut tout modifier
        if (roles.contains(Role.ADMIN)) {
            return;
        }

        // COMPTABLE : vérifier l'attribution (requête directe)
        if (roles.contains(Role.COMPTABLE)) {
            List<Long> accessibleSocietes = societeRepository.findSocieteIdsByComptableId(currentUser.getId());

            if (!accessibleSocietes.contains(societeId)) {
                log.warn("Comptable {} attempted to modify societe {} without permission",
                        currentUser.getId(), societeId);
                throw new ForbiddenException("Vous n'avez pas le droit de modifier cette société");
            }
            return;
        }

        // Les autres rôles n'ont pas le droit de modifier
        throw new ForbiddenException("Vous n'avez pas les permissions pour modifier cette société");
    }

    /**
     * Retourne la liste des IDs de sociétés accessibles par l'utilisateur
     */
    public List<Long> getAccessibleSocieteIds(CustomUserDetails currentUser) {
        List<Role> roles = userRepository.findRolesByUserId(currentUser.getId());

        // ADMIN a accès à tout
        if (roles.contains(Role.ADMIN)) {
            return null; // null signifie "toutes les sociétés"
        }

        // COMPTABLE : sociétés attribuées (requête directe)
        if (roles.contains(Role.COMPTABLE)) {
            return societeRepository.findSocieteIdsByComptableId(currentUser.getId());
        }

        // SOCIETE : ses sociétés (requête directe)
        if (roles.contains(Role.SOCIETE)) {
            return societeRepository.findSocieteIdsByUserSocieteId(currentUser.getId());
        }

        // EMPLOYEE : sa société (requête directe)
        if (roles.contains(Role.EMPLOYEE)) {
            return societeRepository.findSocieteIdsByEmployeeId(currentUser.getId());
        }

        return List.of(); // Aucun accès par défaut
    }

    /**
     * Vérifie si l'utilisateur courant a accès à un autre utilisateur
     * (basé sur les sociétés communes)
     */
    public void checkUserAccess(CustomUserDetails currentUser, Long targetUserId) {
        List<Role> roles = userRepository.findRolesByUserId(currentUser.getId());

        // ADMIN a accès à tout
        if (roles.contains(Role.ADMIN)) {
            return;
        }

        // COMPTABLE : peut voir les utilisateurs des sociétés qu'il gère
        if (roles.contains(Role.COMPTABLE)) {
            // Récupérer les IDs des sociétés du comptable (requête directe)
            List<Long> comptableSocietes = societeRepository.findSocieteIdsByComptableId(currentUser.getId());

            // Récupérer les IDs des sociétés de l'utilisateur cible (requête directe)
            List<Long> targetUserSocietes = societeRepository.findSocieteIdsByUserSocieteId(targetUserId);

            boolean hasCommonSociete = comptableSocietes.stream()
                    .anyMatch(targetUserSocietes::contains);

            if (!hasCommonSociete) {
                // Vérifier aussi si c'est un employé d'une des sociétés du comptable (requête directe)
                List<Long> targetEmployeeSocietes = societeRepository.findSocieteIdsByEmployeeId(targetUserId);

                boolean isEmployeeInComptableSocietes = comptableSocietes.stream()
                        .anyMatch(targetEmployeeSocietes::contains);

                if (!isEmployeeInComptableSocietes) {
                    log.warn("Comptable {} attempted to access user {} without common societe",
                            currentUser.getId(), targetUserId);
                    throw new ForbiddenException("Vous n'avez pas accès à cet utilisateur");
                }
            }
            return;
        }

        // Pour les autres rôles, accès refusé par défaut
        throw new ForbiddenException("Vous n'avez pas les permissions nécessaires");
    }

    /**
     * Vérifie si l'utilisateur est ADMIN
     */
    public boolean isAdmin(CustomUserDetails currentUser) {
        List<Role> roles = userRepository.findRolesByUserId(currentUser.getId());
        return roles.contains(Role.ADMIN);
    }

    /**
     * Vérifie si l'utilisateur est COMPTABLE
     */
    public boolean isComptable(CustomUserDetails currentUser) {
        List<Role> roles = userRepository.findRolesByUserId(currentUser.getId());
        return roles.contains(Role.COMPTABLE);
    }
}
