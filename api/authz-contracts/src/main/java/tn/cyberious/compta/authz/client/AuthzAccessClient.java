package tn.cyberious.compta.authz.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import tn.cyberious.compta.authz.dto.SocieteAccessDto;
import tn.cyberious.compta.authz.dto.UserAccessDto;

@FeignClient(
    name = "authz-service",
    contextId = "authzAccessClient",
    url = "${authz.service.url:http://localhost:8085}")
public interface AuthzAccessClient {

  @GetMapping("/api/access/user/{userId}/societe/{societeId}")
  UserAccessDto getUserAccess(@PathVariable Long userId, @PathVariable Long societeId);

  @GetMapping("/api/access/check")
  Boolean hasAccess(@RequestParam Long userId, @RequestParam Long societeId);

  @GetMapping("/api/access/check/write")
  Boolean hasWriteAccess(@RequestParam Long userId, @RequestParam Long societeId);

  @GetMapping("/api/access/check/validate")
  Boolean hasValidateAccess(@RequestParam Long userId, @RequestParam Long societeId);

  @GetMapping("/api/access/check/permission")
  Boolean hasPermission(
      @RequestParam Long userId, @RequestParam Long societeId, @RequestParam String permissionCode);

  @GetMapping("/api/access/user/{userId}/societe/{societeId}/permissions")
  List<String> getUserPermissions(@PathVariable Long userId, @PathVariable Long societeId);

  @GetMapping("/api/access/user/{userId}/societes")
  List<SocieteAccessDto> getAccessibleSocietes(@PathVariable Long userId);

  @GetMapping("/api/access/user/{userId}/societes/write")
  List<SocieteAccessDto> getWriteAccessibleSocietes(@PathVariable Long userId);

  @DeleteMapping("/api/cache")
  void evictAllCache();

  @DeleteMapping("/api/cache/user/{userId}/societe/{societeId}")
  void evictUserCache(@PathVariable Long userId, @PathVariable Long societeId);
}
