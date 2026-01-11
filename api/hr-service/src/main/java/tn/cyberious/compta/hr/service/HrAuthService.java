package tn.cyberious.compta.hr.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.authz.client.AuthzPermissionClient;
import tn.cyberious.compta.authz.dto.PermissionDto;

@Service
@RequiredArgsConstructor
public class HrAuthService {

  private final AuthzPermissionClient permissionClient;

  public PermissionDto getPermission(Long id) {
    return permissionClient.findById(id);
  }

  public PermissionDto getPermissionByCode(String code) {
    return permissionClient.findByCode(code);
  }

  public Boolean checkPermission(String role, String permissionCode) {
    return permissionClient.hasPermission(role, permissionCode);
  }

  public Boolean checkResourceAccess(String role, String resource, String action) {
    return permissionClient.hasPermissionOnResource(role, resource, action);
  }
}
