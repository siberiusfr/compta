package tn.cyberious.compta.oauth2.security;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UsersRecord userRecord =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

    List<String> roles = userRepository.getUserRoles(userRecord.getId());

    return User.builder()
        .username(userRecord.getUsername())
        .password(userRecord.getPassword())
        .authorities(roles.toArray(new String[0]))
        .accountExpired(!userRecord.getAccountNonExpired())
        .accountLocked(!userRecord.getAccountNonLocked())
        .credentialsExpired(!userRecord.getCredentialsNonExpired())
        .disabled(!userRecord.getEnabled())
        .build();
  }
}
