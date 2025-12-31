package tn.cyberious.compta.oauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configuration Redis pour la communication asynchrone avec notification-service via BullMQ.
 *
 * <p>Cette configuration cree les beans necessaires pour publier des messages dans les queues
 * BullMQ.
 */
@Configuration
public class RedisConfig {

  /**
   * Configure un ObjectMapper specifique pour la serialisation Redis/BullMQ.
   *
   * @return ObjectMapper configure pour les dates Java 8+
   */
  @Bean
  public ObjectMapper redisObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.activateDefaultTyping(
        mapper.getPolymorphicTypeValidator(),
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);
    return mapper;
  }

  /**
   * Configure le RedisTemplate pour les operations Redis generales.
   *
   * @param connectionFactory la factory de connexion Redis
   * @param redisObjectMapper l'ObjectMapper pour la serialisation JSON
   * @return RedisTemplate configure
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
    template.afterPropertiesSet();
    return template;
  }

  /**
   * Configure le StringRedisTemplate pour les operations sur les streams BullMQ.
   *
   * @param connectionFactory la factory de connexion Redis
   * @return StringRedisTemplate configure
   */
  @Bean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }
}
