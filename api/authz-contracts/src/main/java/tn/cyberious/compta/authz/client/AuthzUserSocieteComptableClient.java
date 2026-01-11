package tn.cyberious.compta.authz.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tn.cyberious.compta.authz.dto.UserSocieteComptableDto;
import tn.cyberious.compta.authz.dto.request.AssignUserToSocieteComptableRequest;

@FeignClient(
    name = "authz-service",
    contextId = "authzUserSocieteComptableClient",
    url = "${authz.service.url:http://localhost:8085}")
public interface AuthzUserSocieteComptableClient {

  @GetMapping("/api/user-societe-comptable/{id}")
  UserSocieteComptableDto findById(Long id);

  @GetMapping("/api/user-societe-comptable/user/{userId}")
  UserSocieteComptableDto findByUserId(@PathVariable Long userId);

  @GetMapping("/api/user-societe-comptable/user/{userId}/active")
  UserSocieteComptableDto findActiveByUserId(@PathVariable Long userId);

  @GetMapping("/api/user-societe-comptable/cabinet/{societeComptableId}")
  List<UserSocieteComptableDto> findBySocieteComptableId(@PathVariable Long societeComptableId);

  @GetMapping("/api/user-societe-comptable/cabinet/{societeComptableId}/active")
  List<UserSocieteComptableDto> findActiveBySocieteComptableId(
      @PathVariable Long societeComptableId);

  @GetMapping("/api/user-societe-comptable/cabinet/{societeComptableId}/manager")
  UserSocieteComptableDto findManagerBySocieteComptableId(@PathVariable Long societeComptableId);

  @PostMapping("/api/user-societe-comptable")
  UserSocieteComptableDto assignUser(@RequestBody AssignUserToSocieteComptableRequest request);

  @PutMapping("/api/user-societe-comptable/{id}/role")
  UserSocieteComptableDto updateRole(@PathVariable Long id, @RequestParam String role);

  @PutMapping("/api/user-societe-comptable/{id}/deactivate")
  void deactivate(@PathVariable Long id);

  @DeleteMapping("/api/user-societe-comptable/{id}")
  void delete(@PathVariable Long id);
}
