package tn.cyberious.compta.authz.repository;

import static tn.cyberious.compta.authz.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.authz.generated.tables.pojos.Societes;
import tn.cyberious.compta.authz.generated.tables.records.SocietesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SocieteRepository {

    private final DSLContext dsl;

    public Societes insert(Societes societe) {
        SocietesRecord record = dsl.insertInto(SOCIETES)
                .set(SOCIETES.RAISON_SOCIALE, societe.getRaisonSociale())
                .set(SOCIETES.MATRICULE_FISCALE, societe.getMatriculeFiscale())
                .set(SOCIETES.CODE_TVA, societe.getCodeTva())
                .set(SOCIETES.CODE_DOUANE, societe.getCodeDouane())
                .set(SOCIETES.REGISTRE_COMMERCE, societe.getRegistreCommerce())
                .set(SOCIETES.FORME_JURIDIQUE, societe.getFormeJuridique())
                .set(SOCIETES.CAPITAL_SOCIAL, societe.getCapitalSocial())
                .set(SOCIETES.DATE_CREATION, societe.getDateCreation())
                .set(SOCIETES.ADRESSE, societe.getAdresse())
                .set(SOCIETES.VILLE, societe.getVille())
                .set(SOCIETES.CODE_POSTAL, societe.getCodePostal())
                .set(SOCIETES.TELEPHONE, societe.getTelephone())
                .set(SOCIETES.FAX, societe.getFax())
                .set(SOCIETES.EMAIL, societe.getEmail())
                .set(SOCIETES.SITE_WEB, societe.getSiteWeb())
                .set(SOCIETES.ACTIVITE, societe.getActivite())
                .set(SOCIETES.SECTEUR, societe.getSecteur())
                .set(SOCIETES.SOCIETE_COMPTABLE_ID, societe.getSocieteComptableId())
                .set(SOCIETES.IS_ACTIVE, true)
                .set(SOCIETES.CREATED_AT, LocalDateTime.now())
                .set(SOCIETES.UPDATED_AT, LocalDateTime.now())
                .returning()
                .fetchOne();
        return record != null ? record.into(Societes.class) : null;
    }

    public Societes update(Societes societe) {
        SocietesRecord record = dsl.update(SOCIETES)
                .set(SOCIETES.RAISON_SOCIALE, societe.getRaisonSociale())
                .set(SOCIETES.CODE_TVA, societe.getCodeTva())
                .set(SOCIETES.CODE_DOUANE, societe.getCodeDouane())
                .set(SOCIETES.REGISTRE_COMMERCE, societe.getRegistreCommerce())
                .set(SOCIETES.FORME_JURIDIQUE, societe.getFormeJuridique())
                .set(SOCIETES.CAPITAL_SOCIAL, societe.getCapitalSocial())
                .set(SOCIETES.DATE_CREATION, societe.getDateCreation())
                .set(SOCIETES.ADRESSE, societe.getAdresse())
                .set(SOCIETES.VILLE, societe.getVille())
                .set(SOCIETES.CODE_POSTAL, societe.getCodePostal())
                .set(SOCIETES.TELEPHONE, societe.getTelephone())
                .set(SOCIETES.FAX, societe.getFax())
                .set(SOCIETES.EMAIL, societe.getEmail())
                .set(SOCIETES.SITE_WEB, societe.getSiteWeb())
                .set(SOCIETES.ACTIVITE, societe.getActivite())
                .set(SOCIETES.SECTEUR, societe.getSecteur())
                .set(SOCIETES.SOCIETE_COMPTABLE_ID, societe.getSocieteComptableId())
                .set(SOCIETES.IS_ACTIVE, societe.getIsActive())
                .set(SOCIETES.UPDATED_AT, LocalDateTime.now())
                .where(SOCIETES.ID.eq(societe.getId()))
                .returning()
                .fetchOne();
        return record != null ? record.into(Societes.class) : null;
    }

    public boolean delete(Long id) {
        return dsl.deleteFrom(SOCIETES)
                .where(SOCIETES.ID.eq(id))
                .execute() > 0;
    }

    public Optional<Societes> findById(Long id) {
        return dsl.selectFrom(SOCIETES)
                .where(SOCIETES.ID.eq(id))
                .fetchOptional()
                .map(r -> r.into(Societes.class));
    }

    public Optional<Societes> findByMatriculeFiscale(String matriculeFiscale) {
        return dsl.selectFrom(SOCIETES)
                .where(SOCIETES.MATRICULE_FISCALE.eq(matriculeFiscale))
                .fetchOptional()
                .map(r -> r.into(Societes.class));
    }

    public List<Societes> findAll() {
        return dsl.selectFrom(SOCIETES)
                .orderBy(SOCIETES.RAISON_SOCIALE)
                .fetch()
                .into(Societes.class);
    }

    public List<Societes> findAllActive() {
        return dsl.selectFrom(SOCIETES)
                .where(SOCIETES.IS_ACTIVE.eq(true))
                .orderBy(SOCIETES.RAISON_SOCIALE)
                .fetch()
                .into(Societes.class);
    }

    public List<Societes> findBySocieteComptableId(Long societeComptableId) {
        return dsl.selectFrom(SOCIETES)
                .where(SOCIETES.SOCIETE_COMPTABLE_ID.eq(societeComptableId))
                .orderBy(SOCIETES.RAISON_SOCIALE)
                .fetch()
                .into(Societes.class);
    }

    public List<Societes> findActiveBySocieteComptableId(Long societeComptableId) {
        return dsl.selectFrom(SOCIETES)
                .where(SOCIETES.SOCIETE_COMPTABLE_ID.eq(societeComptableId))
                .and(SOCIETES.IS_ACTIVE.eq(true))
                .orderBy(SOCIETES.RAISON_SOCIALE)
                .fetch()
                .into(Societes.class);
    }

    public List<Societes> searchByRaisonSociale(String searchTerm) {
        return dsl.selectFrom(SOCIETES)
                .where(SOCIETES.RAISON_SOCIALE.likeIgnoreCase("%" + searchTerm + "%"))
                .orderBy(SOCIETES.RAISON_SOCIALE)
                .fetch()
                .into(Societes.class);
    }

    public List<Societes> findBySecteur(String secteur) {
        return dsl.selectFrom(SOCIETES)
                .where(SOCIETES.SECTEUR.eq(secteur))
                .and(SOCIETES.IS_ACTIVE.eq(true))
                .orderBy(SOCIETES.RAISON_SOCIALE)
                .fetch()
                .into(Societes.class);
    }

    public boolean existsById(Long id) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(SOCIETES)
                        .where(SOCIETES.ID.eq(id)));
    }

    public boolean existsByMatriculeFiscale(String matriculeFiscale) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(SOCIETES)
                        .where(SOCIETES.MATRICULE_FISCALE.eq(matriculeFiscale)));
    }
}
