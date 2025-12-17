package tn.cyberious.compta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.enums.Role;
import tn.cyberious.compta.exception.ForbiddenException;
import tn.cyberious.compta.repository.ComptableSocieteRepository;
import tn.cyberious.compta.repository.EmployeeRepository;
import tn.cyberious.compta.repository.UserRepository;
import tn.cyberious.compta.repository.UserSocieteRepository;
import tn.cyberious.compta.security.CustomUserDetails;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;
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

        // COMPTABLE : vérifier l'attribution
        if (roles.contains(Role.COMPTABLE)) {
            boolean hasAccess = comptableSocieteRepository.findByUserId(currentUser.getId())
                    .stream()
                    .anyMatch(cs -> cs.getSocieteId().equals(societeId) &&
                                   (cs.getIsActive() == null || cs.getIsActive()));

            if (!hasAccess) {
                log.warn("Comptable {} attempted to access societe {} without permission",
                        currentUser.getId(), societeId);
                throw new ForbiddenException("Vous n'avez pas accès à cette société");
            }
            return;
        }

        // SOCIETE : vérifier si c'est sa société
        if (roles.contains(Role.SOCIETE)) {
            boolean hasAccess = userSocieteRepository.findByUserId(currentUser.getId())
                    .stream()
                    .anyMatch(us -> us.getSocieteId().equals(societeId) &&
                                   (us.getIsActive() == null || us.getIsActive()));

            if (!hasAccess) {
                log.warn("User societe {} attempted to access societe {} without permission",
                        currentUser.getId(), societeId);
                throw new ForbiddenException("Vous n'avez pas accès à cette société");
            }
            return;
        }

        // EMPLOYEE : vérifier si c'est la société de son emploi
        if (roles.contains(Role.EMPLOYEE)) {
            boolean hasAccess = employeeRepository.findByUserId(currentUser.getId())
                    .map(emp -> emp.getSocieteId().equals(societeId) &&
                               (emp.getIsActive() == null || emp.getIsActive()))
                    .orElse(false);

            if (!hasAccess) {
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

        // COMPTABLE : vérifier l'attribution
        if (roles.contains(Role.COMPTABLE)) {
            boolean hasAccess = comptableSocieteRepository.findByUserId(currentUser.getId())
                    .stream()
                    .anyMatch(cs -> cs.getSocieteId().equals(societeId) &&
                                   (cs.getIsActive() == null || cs.getIsActive()));

            if (!hasAccess) {
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

        // COMPTABLE : sociétés attribuées
        if (roles.contains(Role.COMPTABLE)) {
            return comptableSocieteRepository.findByUserId(currentUser.getId())
                    .stream()
                    .filter(cs -> cs.getIsActive() == null || cs.getIsActive())
                    .map(cs -> cs.getSocieteId())
                    .toList();
        }

        // SOCIETE : ses sociétés
        if (roles.contains(Role.SOCIETE)) {
            return userSocieteRepository.findByUserId(currentUser.getId())
                    .stream()
                    .filter(us -> us.getIsActive() == null || us.getIsActive())
                    .map(us -> us.getSocieteId())
                    .toList();
        }

        // EMPLOYEE : sa société
        if (roles.contains(Role.EMPLOYEE)) {
            return employeeRepository.findByUserId(currentUser.getId())
                    .filter(emp -> emp.getIsActive() == null || emp.getIsActive())
                    .map(emp -> List.of(emp.getSocieteId()))
                    .orElse(List.of());
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
            List<Long> comptableSocietes = comptableSocieteRepository.findByUserId(currentUser.getId())
                    .stream()
                    .filter(cs -> cs.getIsActive() == null || cs.getIsActive())
                    .map(cs -> cs.getSocieteId())
                    .toList();

            // Vérifier si l'utilisateur cible est lié à une de ces sociétés
            List<Long> targetUserSocietes = userSocieteRepository.findByUserId(targetUserId)
                    .stream()
                    .map(us -> us.getSocieteId())
                    .toList();

            boolean hasCommonSociete = comptableSocietes.stream()
                    .anyMatch(targetUserSocietes::contains);

            if (!hasCommonSociete) {
                // Vérifier aussi si c'est un employé d'une des sociétés du comptable
                boolean isEmployeeInComptableSocietes = employeeRepository.findByUserId(targetUserId)
                        .map(emp -> comptableSocietes.contains(emp.getSocieteId()))
                        .orElse(false);

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
