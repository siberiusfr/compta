package tn.cyberious.compta.oauth2.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.oauth2.generated.tables.Roles;
import tn.cyberious.compta.oauth2.generated.tables.UserRoles;
import tn.cyberious.compta.oauth2.generated.tables.records.UsersRecord;
import tn.cyberious.compta.oauth2.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username)
    throws UsernameNotFoundException {
    UsersRecord userRecord = userRepository
      .findByUsername(username)
      .orElseThrow(() ->
        new UsernameNotFoundException("User not found with username: " + username)
      );

    return new CustomUserDetails(userRecord, getUserRoles(userRecord.getId()));
  }

  private List<String> getUserRoles(UUID userId) {
    return userRepository
      .getDsl()
      .select(Roles.ROLES.NAME)
      .from(UserRoles.USER_ROLES)
      .join(Roles.ROLES)
      .on(UserRoles.USER_ROLES.ROLE_ID.eq(Roles.ROLES.ID))
      .where(UserRoles.USER_ROLES.USER_ID.eq(userId))
      .fetch(Roles.ROLES.NAME);
  }
}
