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
import tn.cyberious.compta.authz.dto.ComptableSocietesDto;
import tn.cyberious.compta.authz.dto.request.AssignComptableToSocieteRequest;
import tn.cyberious.compta.authz.dto.request.UpdateComptableSocieteAccessRequest;

@FeignClient(
    name = "authz-service",
    contextId = "authzComptableSocietesClient",
    url = "${authz.service.url:http://localhost:8085}")
public interface AuthzComptableSocietesClient {

  @GetMapping("/api/comptable-societes/{id}")
  ComptableSocietesDto findById(Long id);

  @GetMapping("/api/comptable-societes/user/{userId}/societe/{societeId}")
  ComptableSocietesDto findByUserIdAndSocieteId(
      @PathVariable Long userId, @PathVariable Long societeId);

  @GetMapping("/api/comptable-societes/user/{userId}")
  List<ComptableSocietesDto> findByUserId(@PathVariable Long userId);

  @GetMapping("/api/comptable-societes/user/{userId}/active")
  List<ComptableSocietesDto> findActiveByUserId(@PathVariable Long userId);

  @GetMapping("/api/comptable-societes/societe/{societeId}")
  List<ComptableSocietesDto> findBySocieteId(@PathVariable Long societeId);

  @GetMapping("/api/comptable-societes/user/{userId}/societes")
  List<tn.cyberious.compta.authz.dto.SocieteDto> findSocietesByUserId(@PathVariable Long userId);

  @GetMapping("/api/comptable-societes/user/{userId}/societes/write")
  List<tn.cyberious.compta.authz.dto.SocieteDto> findSocietesWithWriteAccessByUserId(
      @PathVariable Long userId);

  @GetMapping("/api/comptable-societes/check/access")
  Boolean hasAccess(@RequestParam Long userId, @RequestParam Long societeId);

  @GetMapping("/api/comptable-societes/check/write")
  Boolean hasWriteAccess(@RequestParam Long userId, @RequestParam Long societeId);

  @GetMapping("/api/comptable-societes/check/validate")
  Boolean hasValidateAccess(@RequestParam Long userId, @RequestParam Long societeId);

  @PostMapping("/api/comptable-societes")
  ComptableSocietesDto assignComptableToSociete(
      @RequestBody AssignComptableToSocieteRequest request);

  @PutMapping("/api/comptable-societes/{id}")
  ComptableSocietesDto updateAccess(
      @PathVariable Long id, @RequestBody UpdateComptableSocieteAccessRequest request);

  @DeleteMapping("/api/comptable-societes/user/{userId}/societe/{societeId}")
  void revokeAccess(@PathVariable Long userId, @PathVariable Long societeId);
}
