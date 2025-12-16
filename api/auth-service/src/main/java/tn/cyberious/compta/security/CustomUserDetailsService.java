package tn.cyberious.compta.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.enums.Role;

import java.util.List;
import java.util.stream.Collectors;

import static tn.cyberious.compta.auth.generated.Tables.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final DSLContext dsl;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Charger l'utilisateur
        var userRecord = dsl.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOne();

        if (userRecord == null) {
            log.error("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Charger les rôles de l'utilisateur
        List<Role> roles = dsl.select(ROLES.NAME)
                .from(USER_ROLES)
                .join(ROLES).on(USER_ROLES.ROLE_ID.eq(ROLES.ID))
                .where(USER_ROLES.USER_ID.eq(userRecord.getId()))
                .fetch(ROLES.NAME)
                .stream()
                .map(Role::fromName)
                .collect(Collectors.toList());

        log.debug("User {} has roles: {}", username, roles);

        return new CustomUserDetails(
                userRecord.getId(),
                userRecord.getUsername(),
                userRecord.getEmail(),
                userRecord.getPassword(),
                userRecord.getIsActive(),
                userRecord.getIsLocked(),
                roles
        );
    }

    public UserDetails loadUserById(Long userId) {
        log.debug("Loading user by ID: {}", userId);

        var userRecord = dsl.selectFrom(USERS)
                .where(USERS.ID.eq(userId))
                .fetchOne();

        if (userRecord == null) {
            throw new UsernameNotFoundException("User not found with ID: " + userId);
        }

        List<Role> roles = dsl.select(ROLES.NAME)
                .from(USER_ROLES)
                .join(ROLES).on(USER_ROLES.ROLE_ID.eq(ROLES.ID))
                .where(USER_ROLES.USER_ID.eq(userRecord.getId()))
                .fetch(ROLES.NAME)
                .stream()
                .map(Role::fromName)
                .collect(Collectors.toList());

        return new CustomUserDetails(
                userRecord.getId(),
                userRecord.getUsername(),
                userRecord.getEmail(),
                userRecord.getPassword(),
                userRecord.getIsActive(),
                userRecord.getIsLocked(),
                roles
        );
    }
}
