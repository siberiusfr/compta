package tn.cyberious.compta.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.enums.Role;
import tn.cyberious.compta.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Loading user by username: {}", username);

    // Charger l'utilisateur
    Users user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> {
                  log.error("User not found: {}", username);
                  return new UsernameNotFoundException("User not found: " + username);
                });

    // Charger les rôles de l'utilisateur
    List<Role> roles = userRepository.findRolesByUserId(user.getId());

    log.debug("User {} has roles: {}", username, roles);

    return new CustomUserDetails(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        user.getIsActive(),
        user.getIsLocked(),
        roles);
  }

  public UserDetails loadUserById(Long userId) {
    log.debug("Loading user by ID: {}", userId);

    Users user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

    List<Role> roles = userRepository.findRolesByUserId(user.getId());

    return new CustomUserDetails(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        user.getIsActive(),
        user.getIsLocked(),
        roles);
  }
}
