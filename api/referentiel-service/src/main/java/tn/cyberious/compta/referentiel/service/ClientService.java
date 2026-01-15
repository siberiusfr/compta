package tn.cyberious.compta.referentiel.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.referentiel.dto.ClientRequest;
import tn.cyberious.compta.referentiel.dto.ClientResponse;
import tn.cyberious.compta.referentiel.generated.tables.pojos.Clients;
import tn.cyberious.compta.referentiel.repository.ClientRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

  private final ClientRepository clientRepository;

  public ClientResponse create(ClientRequest request, Long companyId) {
    log.info("Creating client for company: {}", companyId);

    if (clientRepository.existsByCodeAndEntrepriseId(request.code(), companyId)) {
      throw new RuntimeException(
          "Client with code " + request.code() + " already exists for this company");
    }

    Clients client = new Clients();
    client.setCode(request.code());
    client.setRaisonSociale(request.raisonSociale());
    client.setMatriculeFiscal(request.matriculeFiscal());
    client.setAdresse(request.adresse());
    client.setVille(request.ville());
    client.setCodePostal(request.codePostal());
    client.setTelephone(request.telephone());
    client.setEmail(request.email());
    client.setTypeClient(request.typeClient());
    client.setEntrepriseId(companyId);
    client.setActif(request.actif() != null ? request.actif() : true);

    Clients saved = clientRepository.insert(client);
    return toResponse(saved);
  }

  public ClientResponse update(Long id, ClientRequest request, Long companyId) {
    log.info("Updating client {} for company: {}", id, companyId);

    Clients client =
        clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));

    if (!client.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Client does not belong to this company");
    }

    client.setCode(request.code());
    client.setRaisonSociale(request.raisonSociale());
    client.setMatriculeFiscal(request.matriculeFiscal());
    client.setAdresse(request.adresse());
    client.setVille(request.ville());
    client.setCodePostal(request.codePostal());
    client.setTelephone(request.telephone());
    client.setEmail(request.email());
    client.setTypeClient(request.typeClient());
    client.setActif(request.actif());

    Clients updated = clientRepository.update(client);
    return toResponse(updated);
  }

  public void delete(Long id, Long companyId) {
    log.info("Deleting client {} for company: {}", id, companyId);

    Clients client =
        clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));

    if (!client.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Client does not belong to this company");
    }

    clientRepository.delete(id);
  }

  public ClientResponse getById(Long id, Long companyId) {
    log.info("Getting client {} for company: {}", id, companyId);

    Clients client =
        clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));

    if (!client.getEntrepriseId().equals(companyId)) {
      throw new RuntimeException("Client does not belong to this company");
    }

    return toResponse(client);
  }

  public List<ClientResponse> getAllByCompany(Long companyId) {
    log.info("Getting all clients for company: {}", companyId);
    return clientRepository.findAllByEntrepriseId(companyId).stream()
        .map(this::toResponse)
        .toList();
  }

  private ClientResponse toResponse(Clients client) {
    return new ClientResponse(
        client.getId(),
        client.getCode(),
        client.getRaisonSociale(),
        client.getMatriculeFiscal(),
        client.getAdresse(),
        client.getVille(),
        client.getCodePostal(),
        client.getTelephone(),
        client.getEmail(),
        client.getTypeClient(),
        client.getEntrepriseId(),
        client.getActif(),
        client.getCreatedAt(),
        client.getUpdatedAt());
  }
}
