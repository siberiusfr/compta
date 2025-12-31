package tn.cyberious.compta.oauth2.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyManagementService {

  private final JdbcTemplate jdbcTemplate;

  @org.springframework.beans.factory.annotation.Value("${oauth2.key.rotation.enabled:true}")
  private boolean keyRotationEnabled;

  @org.springframework.beans.factory.annotation.Value("${oauth2.key.rotation.schedule:0 0 2 * * *}")
  private String keyRotationSchedule;

  @org.springframework.beans.factory.annotation.Value("${oauth2.key.rotation.key-lifetime-days:90}")
  private int keyLifetimeDays;

  @org.springframework.beans.factory.annotation.Value("${oauth2.key.rotation.grace-period-days:7}")
  private int gracePeriodDays;

  @org.springframework.beans.factory.annotation.Value("${oauth2.key.rotation.retention-days:30}")
  private int retentionDays;

  /** Get the current active JWK source */
  @SuppressWarnings("unchecked")
  public JWKSource<SecurityContext> getJWKSource() {
    return (jwkSelector, context) -> {
      List<RSAKey> rsaKeys = getActiveKeys();
      List<JWK> keys = (List<JWK>) (List<?>) rsaKeys;
      return jwkSelector.select(new JWKSet(keys));
    };
  }

  /** Get all active keys from the database */
  @Transactional(readOnly = true)
  public List<RSAKey> getActiveKeys() {
    String sql =
        "SELECT public_key, private_key FROM oauth2.oauth2_keys WHERE active = true ORDER BY created_at DESC";
    return jdbcTemplate.query(sql, (rs, rowNum) -> toRSAKey(rs)).stream()
        .filter(key -> key != null)
        .toList();
  }

  /** Get the primary active key (the most recently created active key) */
  @Transactional(readOnly = true)
  public RSAKey getPrimaryActiveKey() {
    String sql =
        "SELECT public_key, private_key FROM oauth2.oauth2_keys "
            + "WHERE active = true ORDER BY created_at DESC LIMIT 1";
    List<RSAKey> keys =
        jdbcTemplate.query(sql, (rs, rowNum) -> toRSAKey(rs)).stream()
            .filter(key -> key != null)
            .toList();
    return keys.isEmpty() ? null : keys.get(0);
  }

  /** Generate and store a new RSA key pair */
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

      RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId).build();

      // Store the key in the database
      String sql =
          "INSERT INTO oauth2.oauth2_keys "
              + "(key_id, public_key, private_key, key_algorithm, key_size, active, expires_at, grace_period_ends_at) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      jdbcTemplate.update(
          sql,
          keyId,
          rsaKey.toPublicJWK().toJSONString(),
          rsaKey.toJSONString(),
          "RSA",
          2048,
          true,
          LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()),
          LocalDateTime.ofInstant(gracePeriodEndsAt, ZoneId.systemDefault()));

      log.info("Successfully generated and stored new RSA key with id: {}", keyId);
      return rsaKey;
    } catch (Exception e) {
      log.error("Failed to generate RSA key pair", e);
      throw new RuntimeException("Failed to generate RSA key pair", e);
    }
  }

  /** Rotate keys: deactivate expired keys and generate new key if needed */
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
    String deactivateSql =
        "UPDATE oauth2.oauth2_keys SET active = false "
            + "WHERE active = true AND grace_period_ends_at < ?";
    int deactivated = jdbcTemplate.update(deactivateSql, now);

    if (deactivated > 0) {
      log.info("Deactivated {} expired keys", deactivated);
    }

    // Delete keys that are past retention period
    String deleteSql =
        "DELETE FROM oauth2.oauth2_keys " + "WHERE active = false AND grace_period_ends_at < ?";
    int deleted = jdbcTemplate.update(deleteSql, now.minusDays(retentionDays));

    if (deleted > 0) {
      log.info("Deleted {} old keys past retention period", deleted);
    }

    // Check if we need to generate a new key
    RSAKey primaryActiveKey = getPrimaryActiveKey();
    if (primaryActiveKey == null || isKeyExpiringSoon(primaryActiveKey.getKeyID())) {
      log.info("Generating new key as primary key is expiring soon or does not exist");
      generateAndStoreKey();
    }

    log.info("Key rotation completed");
  }

  /** Initialize keys on startup if no active keys exist */
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

  /** Check if a key is expiring soon (within 7 days) */
  private boolean isKeyExpiringSoon(String keyId) {
    String sql = "SELECT expires_at FROM oauth2.oauth2_keys WHERE key_id = ?";
    LocalDateTime expiresAt = jdbcTemplate.queryForObject(sql, LocalDateTime.class, keyId);

    if (expiresAt == null) {
      return true;
    }

    return expiresAt.isBefore(LocalDateTime.now().plusDays(7));
  }

  /** Generate an RSA key pair */
  private KeyPair generateRSAKeyPair() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
  }

  /** Convert database record to RSAKey */
  private RSAKey toRSAKey(ResultSet rs) {
    try {
      String privateKeyJson = rs.getString("private_key");
      if (privateKeyJson == null || privateKeyJson.trim().isEmpty()) {
        log.warn("Found empty private_key in database record");
        return null;
      }
      return RSAKey.parse(privateKeyJson);
    } catch (Exception e) {
      log.error("Failed to parse RSA key from database record, skipping", e);
      return null;
    }
  }
}
