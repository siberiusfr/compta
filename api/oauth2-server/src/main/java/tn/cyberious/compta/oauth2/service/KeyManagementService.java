package tn.cyberious.compta.oauth2.service;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.oauth2.generated.tables.Oauth2Keys;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyManagementService {

  private final DSLContext dsl;

  @Value("${oauth2.key.rotation.enabled:true}")
  private boolean keyRotationEnabled;

  @Value("${oauth2.key.rotation.schedule:0 0 2 * * *}")
  private String keyRotationSchedule;

  @Value("${oauth2.key.rotation.key-lifetime-days:90}")
  private int keyLifetimeDays;

  @Value("${oauth2.key.rotation.grace-period-days:7}")
  private int gracePeriodDays;

  @Value("${oauth2.key.rotation.retention-days:30}")
  private int retentionDays;

  /**
   * Get the current active JWK source
   */
  public JWKSource<SecurityContext> getJWKSource() {
    return (jwkSelector, context) -> {
      List<JWK> keys = getActiveKeys();
      return jwkSelector.select(new JWKSet(keys));
    };
  }

  /**
   * Get all active keys from the database
   */
  @Transactional(readOnly = true)
  public List<RSAKey> getActiveKeys() {
    return dsl.selectFrom(Oauth2Keys.OAUTH2_KEYS)
        .where(Oauth2Keys.OAUTH2_KEYS.ACTIVE.eq(true))
        .fetch()
        .stream()
        .map(this::toRSAKey)
        .toList();
  }

  /**
   * Get the primary active key (the most recently created active key)
   */
  @Transactional(readOnly = true)
  public RSAKey getPrimaryActiveKey() {
    return dsl.selectFrom(Oauth2Keys.OAUTH2_KEYS)
        .where(Oauth2Keys.OAUTH2_KEYS.ACTIVE.eq(true))
        .orderBy(Oauth2Keys.OAUTH2_KEYS.CREATED_AT.desc())
        .limit(1)
        .fetchOne(this::toRSAKey);
  }

  /**
   * Generate and store a new RSA key pair
   */
  @Transactional
  public RSAKey generateAndStoreKey() {
    log.info("Generating new RSA key pair");

    try {
      KeyPair keyPair = generateRSAKeyPair();
      RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
      RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

      String keyId = UUID.randomUUID().toString();
      Instant now = Instant.now();
      Instant expiresAt = now.plus(Duration.ofDays(keyLifetimeDays));
      Instant gracePeriodEndsAt = expiresAt.plus(Duration.ofDays(gracePeriodDays));

      RSAKey rsaKey = new RSAKey.Builder(publicKey)
          .privateKey(privateKey)
          .keyID(keyId)
          .build();

      // Store the key in the database
      dsl.insertInto(Oauth2Keys.OAUTH2_KEYS)
          .set(Oauth2Keys.OAUTH2_KEYS.KEY_ID, keyId)
          .set(Oauth2Keys.OAUTH2_KEYS.PUBLIC_KEY, rsaKey.toPublicJWK().toJSONString())
          .set(Oauth2Keys.OAUTH2_KEYS.PRIVATE_KEY, rsaKey.toJSONString())
          .set(Oauth2Keys.OAUTH2_KEYS.KEY_ALGORITHM, "RSA")
          .set(Oauth2Keys.OAUTH2_KEYS.KEY_SIZE, 2048)
          .set(Oauth2Keys.OAUTH2_KEYS.ACTIVE, true)
          .set(Oauth2Keys.OAUTH2_KEYS.EXPIRES_AT, LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()))
          .set(Oauth2Keys.OAUTH2_KEYS.GRACE_PERIOD_ENDS_AT, LocalDateTime.ofInstant(gracePeriodEndsAt, ZoneId.systemDefault()))
          .execute();

      log.info("Successfully generated and stored new RSA key with id: {}", keyId);
      return rsaKey;
    } catch (Exception e) {
      log.error("Failed to generate RSA key pair", e);
      throw new RuntimeException("Failed to generate RSA key pair", e);
    }
  }

  /**
   * Rotate keys: deactivate expired keys and generate new key if needed
   */
  @Scheduled(cron = "${oauth2.key.rotation.schedule:0 0 2 * * *}")
  @Transactional
  public void rotateKeys() {
    if (!keyRotationEnabled) {
      log.debug("Key rotation is disabled, skipping");
      return;
    }

    log.info("Starting key rotation");

    LocalDateTime now = LocalDateTime.now();

    // Deactivate keys that are past their grace period
    int deactivated = dsl.update(Oauth2Keys.OAUTH2_KEYS)
        .set(Oauth2Keys.OAUTH2_KEYS.ACTIVE, false)
        .where(Oauth2Keys.OAUTH2_KEYS.ACTIVE.eq(true))
        .and(Oauth2Keys.OAUTH2_KEYS.GRACE_PERIOD_ENDS_AT.lessThan(now))
        .execute();

    if (deactivated > 0) {
      log.info("Deactivated {} expired keys", deactivated);
    }

    // Delete keys that are past retention period
    int deleted = dsl.deleteFrom(Oauth2Keys.OAUTH2_KEYS)
        .where(Oauth2Keys.OAUTH2_KEYS.ACTIVE.eq(false))
        .and(Oauth2Keys.OAUTH2_KEYS.GRACE_PERIOD_ENDS_AT.lessThan(now.minusDays(retentionDays)))
        .execute();

    if (deleted > 0) {
      log.info("Deleted {} old keys past retention period", deleted);
    }

    // Check if we need to generate a new key
    RSAKey primaryActiveKey = getPrimaryActiveKey();
    if (primaryActiveKey == null || isKeyExpiringSoon(primaryActiveKey)) {
      log.info("Generating new key as primary key is expiring soon or does not exist");
      generateAndStoreKey();
    }

    log.info("Key rotation completed");
  }

  /**
   * Initialize keys on startup if no active keys exist
   */
  @Transactional
  public void initializeKeys() {
    log.info("Initializing OAuth2 keys");

    RSAKey primaryActiveKey = getPrimaryActiveKey();
    if (primaryActiveKey == null) {
      log.info("No active keys found, generating initial key");
      generateAndStoreKey();
    } else {
      log.info("Found existing active key with id: {}", primaryActiveKey.getKeyID());
    }
  }

  /**
   * Check if a key is expiring soon (within 7 days)
   */
  private boolean isKeyExpiringSoon(RSAKey rsaKey) {
    LocalDateTime expiresAt = dsl.select(Oauth2Keys.OAUTH2_KEYS.EXPIRES_AT)
        .from(Oauth2Keys.OAUTH2_KEYS)
        .where(Oauth2Keys.OAUTH2_KEYS.KEY_ID.eq(rsaKey.getKeyID()))
        .fetchOne(Oauth2Keys.OAUTH2_KEYS.EXPIRES_AT);

    if (expiresAt == null) {
      return true;
    }

    return expiresAt.isBefore(LocalDateTime.now().plusDays(7));
  }

  /**
   * Generate an RSA key pair
   */
  private KeyPair generateRSAKeyPair() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
  }

  /**
   * Convert database record to RSAKey
   */
  private RSAKey toRSAKey(tn.cyberious.compta.oauth2.generated.tables.records.Oauth2KeysRecord record) {
    try {
      return RSAKey.parse(record.getPrivateKey());
    } catch (Exception e) {
      log.error("Failed to parse RSA key from database record", e);
      throw new RuntimeException("Failed to parse RSA key", e);
    }
  }
}
