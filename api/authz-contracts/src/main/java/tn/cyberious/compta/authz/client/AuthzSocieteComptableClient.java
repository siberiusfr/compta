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
import tn.cyberious.compta.authz.dto.SocieteComptableDto;
import tn.cyberious.compta.authz.dto.request.CreateSocieteComptableRequest;
import tn.cyberious.compta.authz.dto.request.UpdateSocieteComptableRequest;

@FeignClient(
    name = "authz-service",
    contextId = "authzSocieteComptableClient",
    url = "${authz.service.url:http://localhost:8085}")
public interface AuthzSocieteComptableClient {

  @GetMapping("/api/societes-comptables/{id}")
  SocieteComptableDto findById(Long id);

  @GetMapping("/api/societes-comptables/matricule/{matriculeFiscale}")
  SocieteComptableDto findByMatriculeFiscale(@PathVariable String matriculeFiscale);

  @GetMapping("/api/societes-comptables")
  List<SocieteComptableDto> findAll();

  @GetMapping("/api/societes-comptables/active")
  List<SocieteComptableDto> findAllActive();

  @GetMapping("/api/societes-comptables/search")
  List<SocieteComptableDto> search(@RequestParam String q);

  @PostMapping("/api/societes-comptables")
  SocieteComptableDto create(@RequestBody CreateSocieteComptableRequest request);

  @PutMapping("/api/societes-comptables/{id}")
  SocieteComptableDto update(
      @PathVariable Long id, @RequestBody UpdateSocieteComptableRequest request);

  @DeleteMapping("/api/societes-comptables/{id}")
  void delete(@PathVariable Long id);
}
