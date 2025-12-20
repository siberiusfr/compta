package tn.cyberious.compta.repository;

import static tn.cyberious.compta.auth.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.auth.generated.tables.pojos.Employees;
import tn.cyberious.compta.auth.generated.tables.records.EmployeesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EmployeeRepository {

  private final DSLContext dsl;

  public Employees insert(Employees employee) {
    log.debug(
        "Inserting employee: userId={}, societeId={}",
        employee.getUserId(),
        employee.getSocieteId());

    EmployeesRecord record =
        dsl.insertInto(EMPLOYEES)
            .set(EMPLOYEES.USER_ID, employee.getUserId())
            .set(EMPLOYEES.SOCIETE_ID, employee.getSocieteId())
            .set(EMPLOYEES.MATRICULE_EMPLOYEE, employee.getMatriculeEmployee())
            .set(EMPLOYEES.POSTE, employee.getPoste())
            .set(EMPLOYEES.DEPARTEMENT, employee.getDepartement())
            .set(EMPLOYEES.DATE_EMBAUCHE, employee.getDateEmbauche())
            .set(EMPLOYEES.DATE_FIN_CONTRAT, employee.getDateFinContrat())
            .set(EMPLOYEES.TYPE_CONTRAT, employee.getTypeContrat())
            .set(
                EMPLOYEES.IS_ACTIVE, employee.getIsActive() != null ? employee.getIsActive() : true)
            .set(EMPLOYEES.CREATED_AT, LocalDateTime.now())
            .set(EMPLOYEES.UPDATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(Employees.class) : null;
  }

  public Employees update(Employees employee) {
    log.debug("Updating employee: {}", employee.getId());

    EmployeesRecord record =
        dsl.update(EMPLOYEES)
            .set(EMPLOYEES.USER_ID, employee.getUserId())
            .set(EMPLOYEES.SOCIETE_ID, employee.getSocieteId())
            .set(EMPLOYEES.MATRICULE_EMPLOYEE, employee.getMatriculeEmployee())
            .set(EMPLOYEES.POSTE, employee.getPoste())
            .set(EMPLOYEES.DEPARTEMENT, employee.getDepartement())
            .set(EMPLOYEES.DATE_EMBAUCHE, employee.getDateEmbauche())
            .set(EMPLOYEES.DATE_FIN_CONTRAT, employee.getDateFinContrat())
            .set(EMPLOYEES.TYPE_CONTRAT, employee.getTypeContrat())
            .set(EMPLOYEES.IS_ACTIVE, employee.getIsActive())
            .set(EMPLOYEES.UPDATED_AT, LocalDateTime.now())
            .where(EMPLOYEES.ID.eq(employee.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Employees.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting employee: {}", id);
    int deleted = dsl.deleteFrom(EMPLOYEES).where(EMPLOYEES.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Employees> findById(Long id) {
    log.debug("Finding employee by id: {}", id);
    return dsl.selectFrom(EMPLOYEES)
        .where(EMPLOYEES.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Employees.class));
  }

  public Optional<Employees> findByUserId(Long userId) {
    log.debug("Finding employee by userId: {}", userId);
    return dsl.selectFrom(EMPLOYEES)
        .where(EMPLOYEES.USER_ID.eq(userId))
        .fetchOptional()
        .map(record -> record.into(Employees.class));
  }

  public List<Employees> findBySocieteId(Long societeId) {
    log.debug("Finding employees by societeId: {}", societeId);
    return dsl.selectFrom(EMPLOYEES)
        .where(EMPLOYEES.SOCIETE_ID.eq(societeId))
        .fetch()
        .into(Employees.class);
  }

  public List<Employees> findAll() {
    log.debug("Finding all employees");
    return dsl.selectFrom(EMPLOYEES).fetch().into(Employees.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if employee exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(EMPLOYEES).where(EMPLOYEES.ID.eq(id)));
  }

  public boolean existsByUserId(Long userId) {
    return dsl.fetchExists(dsl.selectFrom(EMPLOYEES).where(EMPLOYEES.USER_ID.eq(userId)));
  }
}
