package tn.cyberious.compta.authz.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import tn.cyberious.compta.authz.dto.PermissionDto;
import tn.cyberious.compta.authz.dto.RolePermissionDto;

@FeignClient(name = "authz-service", url = "${authz.service.url:http://localhost:8085}")
public interface AuthzPermissionClient {

  @GetMapping("/api/permissions/{id}")
  PermissionDto findById(@PathVariable Long id);

  @GetMapping("/api/permissions/code/{code}")
  PermissionDto findByCode(@PathVariable String code);

  @GetMapping("/api/permissions")
  List<PermissionDto> findAll();

  @GetMapping("/api/permissions/resource/{resource}")
  List<PermissionDto> findByResource(@PathVariable String resource);

  @GetMapping("/api/permissions/action/{action}")
  List<PermissionDto> findByAction(@PathVariable String action);

  @GetMapping("/api/permissions/resources")
  List<String> findDistinctResources();

  @GetMapping("/api/permissions/actions")
  List<String> findDistinctActions();

  @GetMapping("/api/permissions/role/{role}")
  List<PermissionDto> findPermissionsByRole(@PathVariable String role);

  @GetMapping("/api/permissions/role/{role}/assignments")
  List<RolePermissionDto> findRolePermissionsByRole(@PathVariable String role);

  @GetMapping("/api/permissions/roles")
  List<String> findDistinctRoles();

  @GetMapping("/api/permissions/check")
  Boolean hasPermission(@RequestParam String role, @RequestParam String permissionCode);

  @GetMapping("/api/permissions/check/resource")
  Boolean hasPermissionOnResource(
      @RequestParam String role, @RequestParam String resource, @RequestParam String action);
}
