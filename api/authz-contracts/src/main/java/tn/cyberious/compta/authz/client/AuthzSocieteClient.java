package tn.cyberious.compta.authz.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import tn.cyberious.compta.authz.dto.SocieteDto;

@FeignClient(
    name = "authz-service",
    contextId = "authzSocieteClient",
    url = "${authz.service.url:http://localhost:8085}")
public interface AuthzSocieteClient {

  @GetMapping("/api/societes/{id}")
  SocieteDto findById(@PathVariable Long id);

  @GetMapping("/api/societes/matricule/{matriculeFiscale}")
  SocieteDto findByMatriculeFiscale(@PathVariable String matriculeFiscale);

  @GetMapping("/api/societes")
  List<SocieteDto> findAll();

  @GetMapping("/api/societes/active")
  List<SocieteDto> findAllActive();

  @GetMapping("/api/societes/cabinet/{societeComptableId}")
  List<SocieteDto> findBySocieteComptableId(@PathVariable Long societeComptableId);

  @GetMapping("/api/societes/cabinet/{societeComptableId}/active")
  List<SocieteDto> findActiveBySocieteComptableId(@PathVariable Long societeComptableId);

  @GetMapping("/api/societes/search")
  List<SocieteDto> search(@RequestParam String q);

  @GetMapping("/api/societes/secteur/{secteur}")
  List<SocieteDto> findBySecteur(@PathVariable String secteur);
}
