package tn.cyberious.compta.oauth2.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tn.cyberious.compta.oauth2.jti.TokenBlacklistService;

/** Unit tests for TokenBlacklistService. */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Token Blacklist Service Tests")
class TokenBlacklistServiceTest {

  @Autowired private TokenBlacklistService tokenBlacklistService;

  @Nested
  @DisplayName("Add to Blacklist")
  class AddToBlacklistTests {

    @Test
    @DisplayName("Should add JTI to blacklist")
    void shouldAddJtiToBlacklist() {
      String jti = UUID.randomUUID().toString();
      Instant expiration = Instant.now().plusSeconds(3600);

      tokenBlacklistService.addToBlacklist(jti, expiration);

      assertTrue(tokenBlacklistService.isBlacklisted(jti));
    }

    @Test
    @DisplayName("Should handle duplicate JTI gracefully")
    void shouldHandleDuplicateJtiGracefully() {
      String jti = UUID.randomUUID().toString();
      Instant expiration = Instant.now().plusSeconds(3600);

      tokenBlacklistService.addToBlacklist(jti, expiration);
      assertDoesNotThrow(() -> tokenBlacklistService.addToBlacklist(jti, expiration));

      assertTrue(tokenBlacklistService.isBlacklisted(jti));
    }
  }

  @Nested
  @DisplayName("Check Blacklist")
  class CheckBlacklistTests {

    @Test
    @DisplayName("Should return true for blacklisted JTI")
    void shouldReturnTrueForBlacklistedJti() {
      String jti = UUID.randomUUID().toString();
      Instant expiration = Instant.now().plusSeconds(3600);

      tokenBlacklistService.addToBlacklist(jti, expiration);

      assertTrue(tokenBlacklistService.isBlacklisted(jti));
    }

    @Test
    @DisplayName("Should return false for non-blacklisted JTI")
    void shouldReturnFalseForNonBlacklistedJti() {
      String jti = UUID.randomUUID().toString();

      assertFalse(tokenBlacklistService.isBlacklisted(jti));
    }

    @Test
    @DisplayName("Should return false for null JTI")
    void shouldReturnFalseForNullJti() {
      assertFalse(tokenBlacklistService.isBlacklisted(null));
    }

    @Test
    @DisplayName("Should return false for empty JTI")
    void shouldReturnFalseForEmptyJti() {
      assertFalse(tokenBlacklistService.isBlacklisted(""));
    }
  }

  @Nested
  @DisplayName("Token Expiration Handling")
  class ExpirationTests {

    @Test
    @DisplayName("Should handle token with past expiration")
    void shouldHandleTokenWithPastExpiration() {
      String jti = UUID.randomUUID().toString();
      Instant pastExpiration = Instant.now().minusSeconds(3600);

      tokenBlacklistService.addToBlacklist(jti, pastExpiration);

      // Expired tokens should still be queryable until cleanup
      // The actual behavior depends on implementation
      assertDoesNotThrow(() -> tokenBlacklistService.isBlacklisted(jti));
    }

    @Test
    @DisplayName("Should handle token with far future expiration")
    void shouldHandleTokenWithFarFutureExpiration() {
      String jti = UUID.randomUUID().toString();
      Instant farFuture = Instant.now().plusSeconds(365 * 24 * 3600); // 1 year

      tokenBlacklistService.addToBlacklist(jti, farFuture);

      assertTrue(tokenBlacklistService.isBlacklisted(jti));
    }
  }

  @Nested
  @DisplayName("Concurrent Access")
  class ConcurrentAccessTests {

    @Test
    @DisplayName("Should handle concurrent blacklist additions")
    void shouldHandleConcurrentBlacklistAdditions() throws InterruptedException {
      int threadCount = 10;
      Thread[] threads = new Thread[threadCount];
      String[] jtis = new String[threadCount];

      for (int i = 0; i < threadCount; i++) {
        jtis[i] = UUID.randomUUID().toString();
        final String jti = jtis[i];
        threads[i] =
            new Thread(
                () -> {
                  tokenBlacklistService.addToBlacklist(jti, Instant.now().plusSeconds(3600));
                });
      }

      for (Thread thread : threads) {
        thread.start();
      }

      for (Thread thread : threads) {
        thread.join();
      }

      for (String jti : jtis) {
        assertTrue(tokenBlacklistService.isBlacklisted(jti));
      }
    }

    @Test
    @DisplayName("Should handle concurrent blacklist checks")
    void shouldHandleConcurrentBlacklistChecks() throws InterruptedException {
      String jti = UUID.randomUUID().toString();
      tokenBlacklistService.addToBlacklist(jti, Instant.now().plusSeconds(3600));

      int threadCount = 10;
      Thread[] threads = new Thread[threadCount];
      boolean[] results = new boolean[threadCount];

      for (int i = 0; i < threadCount; i++) {
        final int index = i;
        threads[i] =
            new Thread(
                () -> {
                  results[index] = tokenBlacklistService.isBlacklisted(jti);
                });
      }

      for (Thread thread : threads) {
        thread.start();
      }

      for (Thread thread : threads) {
        thread.join();
      }

      for (boolean result : results) {
        assertTrue(result);
      }
    }
  }
}
