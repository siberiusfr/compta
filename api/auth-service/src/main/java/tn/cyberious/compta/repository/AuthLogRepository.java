package tn.cyberious.compta.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.auth.generated.tables.pojos.AuthLogs;
import tn.cyberious.compta.auth.generated.tables.records.AuthLogsRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static tn.cyberious.compta.auth.generated.Tables.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthLogRepository {

    private final DSLContext dsl;

    public AuthLogs insert(AuthLogs authLog) {
        log.debug("Inserting auth log: action={}, username={}", authLog.getAction(), authLog.getUsername());

        AuthLogsRecord record = dsl.insertInto(AUTH_LOGS)
                .set(AUTH_LOGS.USER_ID, authLog.getUserId())
                .set(AUTH_LOGS.USERNAME, authLog.getUsername())
                .set(AUTH_LOGS.ACTION, authLog.getAction())
                .set(AUTH_LOGS.IP_ADDRESS, authLog.getIpAddress())
                .set(AUTH_LOGS.USER_AGENT, authLog.getUserAgent())
                .set(AUTH_LOGS.DETAILS, authLog.getDetails())
                .set(AUTH_LOGS.CREATED_AT, LocalDateTime.now())
                .returning()
                .fetchOne();

        return record != null ? record.into(AuthLogs.class) : null;
    }

    public AuthLogs update(AuthLogs authLog) {
        log.debug("Updating auth log: {}", authLog.getId());

        AuthLogsRecord record = dsl.update(AUTH_LOGS)
                .set(AUTH_LOGS.USER_ID, authLog.getUserId())
                .set(AUTH_LOGS.USERNAME, authLog.getUsername())
                .set(AUTH_LOGS.ACTION, authLog.getAction())
                .set(AUTH_LOGS.IP_ADDRESS, authLog.getIpAddress())
                .set(AUTH_LOGS.USER_AGENT, authLog.getUserAgent())
                .set(AUTH_LOGS.DETAILS, authLog.getDetails())
                .where(AUTH_LOGS.ID.eq(authLog.getId()))
                .returning()
                .fetchOne();

        return record != null ? record.into(AuthLogs.class) : null;
    }

    public boolean delete(Long id) {
        log.debug("Deleting auth log: {}", id);
        int deleted = dsl.deleteFrom(AUTH_LOGS)
                .where(AUTH_LOGS.ID.eq(id))
                .execute();
        return deleted > 0;
    }

    public Optional<AuthLogs> findById(Long id) {
        log.debug("Finding auth log by id: {}", id);
        return dsl.selectFrom(AUTH_LOGS)
                .where(AUTH_LOGS.ID.eq(id))
                .fetchOptional()
                .map(record -> record.into(AuthLogs.class));
    }

    public List<AuthLogs> findByUserId(Long userId) {
        log.debug("Finding auth logs by userId: {}", userId);
        return dsl.selectFrom(AUTH_LOGS)
                .where(AUTH_LOGS.USER_ID.eq(userId))
                .orderBy(AUTH_LOGS.CREATED_AT.desc())
                .fetch()
                .into(AuthLogs.class);
    }

    public List<AuthLogs> findByUsername(String username) {
        log.debug("Finding auth logs by username: {}", username);
        return dsl.selectFrom(AUTH_LOGS)
                .where(AUTH_LOGS.USERNAME.eq(username))
                .orderBy(AUTH_LOGS.CREATED_AT.desc())
                .fetch()
                .into(AuthLogs.class);
    }

    public List<AuthLogs> findByAction(String action) {
        log.debug("Finding auth logs by action: {}", action);
        return dsl.selectFrom(AUTH_LOGS)
                .where(AUTH_LOGS.ACTION.eq(action))
                .orderBy(AUTH_LOGS.CREATED_AT.desc())
                .fetch()
                .into(AuthLogs.class);
    }

    public List<AuthLogs> findAll() {
        log.debug("Finding all auth logs");
        return dsl.selectFrom(AUTH_LOGS)
                .orderBy(AUTH_LOGS.CREATED_AT.desc())
                .fetch()
                .into(AuthLogs.class);
    }

    public boolean exists(Long id) {
        log.debug("Checking if auth log exists: {}", id);
        return dsl.fetchExists(
                dsl.selectFrom(AUTH_LOGS)
                        .where(AUTH_LOGS.ID.eq(id))
        );
    }

    public void log(Long userId, String username, String action, String ipAddress, String userAgent, String details) {
        log.debug("Logging auth event: action={}, username={}", action, username);
        dsl.insertInto(AUTH_LOGS)
                .set(AUTH_LOGS.USER_ID, userId)
                .set(AUTH_LOGS.USERNAME, username)
                .set(AUTH_LOGS.ACTION, action)
                .set(AUTH_LOGS.IP_ADDRESS, ipAddress)
                .set(AUTH_LOGS.USER_AGENT, userAgent)
                .set(AUTH_LOGS.DETAILS, details)
                .set(AUTH_LOGS.CREATED_AT, LocalDateTime.now())
                .execute();
    }
}
