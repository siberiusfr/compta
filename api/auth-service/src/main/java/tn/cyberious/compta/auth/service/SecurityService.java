package tn.cyberious.compta.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.auth.enums.Role;
import tn.cyberious.compta.auth.repository.*;
import tn.cyberious.compta.auth.security.CustomUserDetails;

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
