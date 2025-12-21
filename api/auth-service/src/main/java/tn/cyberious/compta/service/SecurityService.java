package tn.cyberious.compta.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.enums.Role;
import tn.cyberious.compta.repository.*;
import tn.cyberious.compta.security.CustomUserDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

  private final UserRepository userRepository;

  /** Vérifie si l'utilisateur est ADMIN */
  public boolean isAdmin(CustomUserDetails currentUser) {
    List<Role> roles = userRepository.findRolesByUserId(currentUser.getId());
    return roles.contains(Role.ADMIN);
  }
}
