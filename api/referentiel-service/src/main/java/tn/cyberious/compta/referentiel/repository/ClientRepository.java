package tn.cyberious.compta.referentiel.repository;

import static tn.cyberious.compta.referentiel.generated.Tables.CLIENTS;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.referentiel.generated.tables.pojos.Clients;
import tn.cyberious.compta.referentiel.generated.tables.records.ClientsRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ClientRepository {

  private final DSLContext dsl;

  public Clients insert(Clients client) {
    log.debug("Inserting client: {}", client.getCode());

    ClientsRecord record =
        dsl.insertInto(CLIENTS)
            .set(CLIENTS.CODE, client.getCode())
            .set(CLIENTS.RAISON_SOCIALE, client.getRaisonSociale())
            .set(CLIENTS.MATRICULE_FISCAL, client.getMatriculeFiscal())
            .set(CLIENTS.ADRESSE, client.getAdresse())
            .set(CLIENTS.VILLE, client.getVille())
            .set(CLIENTS.CODE_POSTAL, client.getCodePostal())
            .set(CLIENTS.TELEPHONE, client.getTelephone())
            .set(CLIENTS.EMAIL, client.getEmail())
            .set(CLIENTS.TYPE_CLIENT, client.getTypeClient())
            .set(CLIENTS.ENTREPRISE_ID, client.getEntrepriseId())
            .set(CLIENTS.ACTIF, client.getActif() != null ? client.getActif() : true)
            .returning()
            .fetchOne();

    return record != null ? record.into(Clients.class) : null;
  }

  public Clients update(Clients client) {
    log.debug("Updating client: {}", client.getId());

    ClientsRecord record =
        dsl.update(CLIENTS)
            .set(CLIENTS.CODE, client.getCode())
            .set(CLIENTS.RAISON_SOCIALE, client.getRaisonSociale())
            .set(CLIENTS.MATRICULE_FISCAL, client.getMatriculeFiscal())
            .set(CLIENTS.ADRESSE, client.getAdresse())
            .set(CLIENTS.VILLE, client.getVille())
            .set(CLIENTS.CODE_POSTAL, client.getCodePostal())
            .set(CLIENTS.TELEPHONE, client.getTelephone())
            .set(CLIENTS.EMAIL, client.getEmail())
            .set(CLIENTS.TYPE_CLIENT, client.getTypeClient())
            .set(CLIENTS.ACTIF, client.getActif())
            .where(CLIENTS.ID.eq(client.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Clients.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting client: {}", id);
    int deleted = dsl.deleteFrom(CLIENTS).where(CLIENTS.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Clients> findById(Long id) {
    log.debug("Finding client by id: {}", id);
    return dsl.selectFrom(CLIENTS)
        .where(CLIENTS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Clients.class));
  }

  public Optional<Clients> findByCodeAndEntrepriseId(String code, Long entrepriseId) {
    log.debug("Finding client by code: {} and entreprise: {}", code, entrepriseId);
    return dsl.selectFrom(CLIENTS)
        .where(CLIENTS.CODE.eq(code).and(CLIENTS.ENTREPRISE_ID.eq(entrepriseId)))
        .fetchOptional()
        .map(record -> record.into(Clients.class));
  }

  public List<Clients> findAllByEntrepriseId(Long entrepriseId) {
    log.debug("Finding all clients for entreprise: {}", entrepriseId);
    return dsl.selectFrom(CLIENTS)
        .where(CLIENTS.ENTREPRISE_ID.eq(entrepriseId))
        .fetch()
        .into(Clients.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if client exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(CLIENTS).where(CLIENTS.ID.eq(id)));
  }

  public boolean existsByCodeAndEntrepriseId(String code, Long entrepriseId) {
    return dsl.fetchExists(
        dsl.selectFrom(CLIENTS)
            .where(CLIENTS.CODE.eq(code).and(CLIENTS.ENTREPRISE_ID.eq(entrepriseId))));
  }
}
