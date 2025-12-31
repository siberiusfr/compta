package tn.cyberious.compta.oauth2.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.oauth2.dto.PasswordResetMessage;

/**
 * Publisher pour envoyer des messages de reset de mot de passe vers la queue BullMQ.
 *
 * <p>Cette classe publie des jobs compatibles avec BullMQ/NestJS dans Redis. Le format du job
 * respecte la structure attendue par @nestjs/bullmq.
 */
@Service
public class PasswordResetQueuePublisher {

  private static final Logger log = LoggerFactory.getLogger(PasswordResetQueuePublisher.class);

  private static final String QUEUE_NAME = "password-reset";
  private static final String JOB_NAME = "password-reset-requested";
  private static final String BULL_PREFIX = "bull";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Value("${notification.queue.enabled:true}")
  private boolean queueEnabled;

  public PasswordResetQueuePublisher(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Publie un message de demande de reset de mot de passe dans la queue BullMQ.
   *
   * @param message le message contenant les informations de reset
   * @return l'ID du job cree
   * @throws RuntimeException si la publication echoue ou si Redis est indisponible
   */
  public String publishPasswordResetRequested(PasswordResetMessage message) {
    if (!queueEnabled) {
      log.warn("Queue is disabled, password reset message will not be published");
      throw new IllegalStateException("Notification queue is disabled");
    }

    try {
      String jobId = generateJobId();
      String queueKey = BULL_PREFIX + ":" + QUEUE_NAME;

      // Creer la structure du job BullMQ
      Map<String, Object> jobData = createBullMQJob(jobId, message);
      String jobJson = objectMapper.writeValueAsString(jobData);

      // Stocker le job dans le hash Redis
      String jobHashKey = queueKey + ":" + jobId;
      redisTemplate.opsForHash().put(jobHashKey, "data", jobJson);
      redisTemplate.opsForHash().put(jobHashKey, "name", JOB_NAME);
      redisTemplate.opsForHash().put(jobHashKey, "opts", createJobOptions());
      redisTemplate
          .opsForHash()
          .put(jobHashKey, "timestamp", String.valueOf(System.currentTimeMillis()));
      redisTemplate.opsForHash().put(jobHashKey, "attemptsMade", "0");
      redisTemplate.opsForHash().put(jobHashKey, "processedOn", "0");
      redisTemplate.opsForHash().put(jobHashKey, "finishedOn", "0");

      // Ajouter le job a la liste d'attente
      String waitKey = queueKey + ":wait";
      redisTemplate.opsForList().leftPush(waitKey, jobId);

      // Publier un evenement pour reveiller les workers
      String eventChannel = queueKey + ":waiting";
      redisTemplate.convertAndSend(eventChannel, jobId);

      log.info(
          "Published password reset job {} for user {} (email: {})",
          jobId,
          message.getUsername(),
          message.getEmail());

      return jobId;
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize password reset message", e);
      throw new RuntimeException("Failed to publish password reset message", e);
    } catch (Exception e) {
      log.error("Failed to publish password reset message to Redis", e);
      throw new RuntimeException("Redis unavailable - cannot publish password reset message", e);
    }
  }

  /**
   * Cree la structure du job BullMQ.
   *
   * @param jobId l'ID du job
   * @param message le message de reset
   * @return la map representant le job
   */
  private Map<String, Object> createBullMQJob(String jobId, PasswordResetMessage message) {
    Map<String, Object> job = new HashMap<>();
    job.put("id", jobId);
    job.put("name", JOB_NAME);
    job.put("data", createJobData(message));
    job.put("opts", createJobOptionsMap());
    job.put("timestamp", System.currentTimeMillis());
    job.put("attemptsMade", 0);
    return job;
  }

  /**
   * Cree les donnees du job a partir du message.
   *
   * @param message le message de reset
   * @return la map des donnees
   */
  private Map<String, Object> createJobData(PasswordResetMessage message) {
    Map<String, Object> data = new HashMap<>();
    data.put("userId", message.getUserId());
    data.put("email", message.getEmail());
    data.put("username", message.getUsername());
    data.put("token", message.getToken());
    data.put("resetLink", message.getResetLink());
    data.put(
        "expiresAt", message.getExpiresAt() != null ? message.getExpiresAt().toString() : null);
    return data;
  }

  /**
   * Cree les options du job en format JSON.
   *
   * @return les options serialisees en JSON
   */
  private String createJobOptions() {
    try {
      return objectMapper.writeValueAsString(createJobOptionsMap());
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  /**
   * Cree la map des options du job BullMQ.
   *
   * @return la map des options
   */
  private Map<String, Object> createJobOptionsMap() {
    Map<String, Object> opts = new HashMap<>();
    opts.put("attempts", 3);
    opts.put("removeOnComplete", true);
    opts.put("removeOnFail", false);

    Map<String, Object> backoff = new HashMap<>();
    backoff.put("type", "exponential");
    backoff.put("delay", 1000);
    opts.put("backoff", backoff);

    return opts;
  }

  /**
   * Genere un ID unique pour le job.
   *
   * @return l'ID du job
   */
  private String generateJobId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
  }
}
