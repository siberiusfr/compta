package tn.cyberious.compta.hr.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.authz.client.AuthzAccessClient;
import tn.cyberious.compta.authz.client.AuthzComptableSocietesClient;
import tn.cyberious.compta.authz.client.AuthzPermissionClient;
import tn.cyberious.compta.authz.client.AuthzSocieteComptableClient;
import tn.cyberious.compta.authz.client.AuthzUserSocieteComptableClient;
import tn.cyberious.compta.authz.dto.ComptableSocietesDto;
import tn.cyberious.compta.authz.dto.SocieteAccessDto;
import tn.cyberious.compta.authz.dto.UserAccessDto;

@Service
@RequiredArgsConstructor
public class HrAuthService {

  private final AuthzPermissionClient permissionClient;
  private final AuthzAccessClient accessClient;
  private final AuthzComptableSocietesClient comptableSocietesClient;
  private final AuthzUserSocieteComptableClient userSocieteComptableClient;
  private final AuthzSocieteComptableClient societeComptableClient;

  public void checkAccess(Long userId, Long societeId) {
    UserAccessDto userAccess = accessClient.getUserAccess(userId, societeId);
    List<SocieteAccessDto> societeAccess = accessClient.getAccessibleSocietes(userId);
    ComptableSocietesDto comptableAccess =
        comptableSocietesClient.findByUserIdAndSocieteId(userId, societeId);
  }
}
