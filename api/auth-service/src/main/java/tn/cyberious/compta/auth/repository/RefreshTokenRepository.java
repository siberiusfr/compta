package tn.cyberious.compta.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static tn.cyberious.compta.auth.generated.Tables.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.auth.generated.tables.pojos.RefreshTokens;
import tn.cyberious.compta.auth.generated.tables.records.RefreshTokensRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

  private final DSLContext dsl;

  public RefreshTokens insert(RefreshTokens refreshToken) {
    log.debug("Inserting refresh token for userId: {}", refreshToken.getUserId());

    RefreshTokensRecord record =
        dsl.insertInto(REFRESH_TOKENS)
            .set(REFRESH_TOKENS.USER_ID, refreshToken.getUserId())
            .set(REFRESH_TOKENS.TOKEN, refreshToken.getToken())
            .set(REFRESH_TOKENS.EXPIRES_AT, refreshToken.getExpiresAt())
            .set(REFRESH_TOKENS.CREATED_AT, LocalDateTime.now())
            .set(REFRESH_TOKENS.IP_ADDRESS, refreshToken.getIpAddress())
            .set(REFRESH_TOKENS.USER_AGENT, refreshToken.getUserAgent())
            .returning()
            .fetchOne();

    return record != null ? record.into(RefreshTokens.class) : null;
  }

  public RefreshTokens update(RefreshTokens refreshToken) {
    log.debug("Updating refresh token: {}", refreshToken.getId());

    RefreshTokensRecord record =
        dsl.update(REFRESH_TOKENS)
            .set(REFRESH_TOKENS.USER_ID, refreshToken.getUserId())
            .set(REFRESH_TOKENS.TOKEN, refreshToken.getToken())
            .set(REFRESH_TOKENS.EXPIRES_AT, refreshToken.getExpiresAt())
            .set(REFRESH_TOKENS.IP_ADDRESS, refreshToken.getIpAddress())
            .set(REFRESH_TOKENS.USER_AGENT, refreshToken.getUserAgent())
            .where(REFRESH_TOKENS.ID.eq(refreshToken.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(RefreshTokens.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting refresh token: {}", id);
    int deleted = dsl.deleteFrom(REFRESH_TOKENS).where(REFRESH_TOKENS.ID.eq(id)).execute();
    return deleted > 0;
  }

  public int deleteByUserId(Long userId) {
    log.debug("Deleting all refresh tokens for userId: {}", userId);
    return dsl.deleteFrom(REFRESH_TOKENS).where(REFRESH_TOKENS.USER_ID.eq(userId)).execute();
  }

  public int deleteByToken(String token) {
    log.debug("Deleting refresh token by token");
    return dsl.deleteFrom(REFRESH_TOKENS).where(REFRESH_TOKENS.TOKEN.eq(token)).execute();
  }

  public int deleteExpiredTokens() {
    log.debug("Deleting expired refresh tokens");
    return dsl.deleteFrom(REFRESH_TOKENS)
        .where(REFRESH_TOKENS.EXPIRES_AT.lt(LocalDateTime.now()))
        .execute();
  }

  public Optional<RefreshTokens> findById(Long id) {
    log.debug("Finding refresh token by id: {}", id);
    return dsl.selectFrom(REFRESH_TOKENS)
        .where(REFRESH_TOKENS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(RefreshTokens.class));
  }

  public Optional<RefreshTokens> findByToken(String token) {
    log.debug("Finding refresh token by token");
    return dsl.selectFrom(REFRESH_TOKENS)
        .where(REFRESH_TOKENS.TOKEN.eq(token))
        .fetchOptional()
        .map(record -> record.into(RefreshTokens.class));
  }

  public List<RefreshTokens> findByUserId(Long userId) {
    log.debug("Finding refresh tokens by userId: {}", userId);
    return dsl.selectFrom(REFRESH_TOKENS)
        .where(REFRESH_TOKENS.USER_ID.eq(userId))
        .fetch()
        .into(RefreshTokens.class);
  }

  public List<RefreshTokens> findAll() {
    log.debug("Finding all refresh tokens");
    return dsl.selectFrom(REFRESH_TOKENS).fetch().into(RefreshTokens.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if refresh token exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(REFRESH_TOKENS).where(REFRESH_TOKENS.ID.eq(id)));
  }
}
