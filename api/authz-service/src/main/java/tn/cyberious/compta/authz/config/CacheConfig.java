package tn.cyberious.compta.authz.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String USER_ACCESS_CACHE = "userAccessCache";
  public static final String USER_PERMISSIONS_CACHE = "userPermissionsCache";

  @Bean
  public Caffeine<Object, Object> caffeineConfig() {
    return Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .recordStats();
  }

  @Bean
  public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
    CaffeineCacheManager cacheManager =
        new CaffeineCacheManager(USER_ACCESS_CACHE, USER_PERMISSIONS_CACHE);
    cacheManager.setCaffeine(caffeine);
    return cacheManager;
  }
}
