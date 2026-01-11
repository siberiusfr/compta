package tn.cyberious.compta.authz.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import tn.cyberious.compta.authz.dto.SocieteDto;
import tn.cyberious.compta.authz.dto.UserSocietesDto;

@FeignClient(name = "authz-service", url = "${authz.service.url:http://localhost:8085}")
public interface AuthzUserSocietesClient {

  @GetMapping("/api/user-societes/{id}")
  UserSocietesDto findById(@PathVariable Long id);

  @GetMapping("/api/user-societes/user/{userId}")
  UserSocietesDto findByUserId(@PathVariable Long userId);

  @GetMapping("/api/user-societes/user/{userId}/active")
  UserSocietesDto findActiveByUserId(@PathVariable Long userId);

  @GetMapping("/api/user-societes/societe/{societeId}")
  List<UserSocietesDto> findBySocieteId(@PathVariable Long societeId);

  @GetMapping("/api/user-societes/societe/{societeId}/active")
  List<UserSocietesDto> findActiveBySocieteId(@PathVariable Long societeId);

  @GetMapping("/api/user-societes/societe/{societeId}/manager")
  UserSocietesDto findManagerBySocieteId(@PathVariable Long societeId);

  @GetMapping("/api/user-societes/user/{userId}/societe")
  SocieteDto findSocieteByUserId(@PathVariable Long userId);
}
