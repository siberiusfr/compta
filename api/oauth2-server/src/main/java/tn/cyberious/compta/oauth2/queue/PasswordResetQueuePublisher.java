package tn.cyberious.compta.oauth2.queue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import tn.cyberious.compta.contracts.notification.PasswordResetRequested;
import tn.cyberious.compta.contracts.notification.SendPasswordResetEmailPayload;

/**
 * Publisher pour envoyer des messages de reset de mot de passe vers la queue BullMQ.
 *
 * <p>Cette classe publie des jobs compatibles avec BullMQ/NestJS dans Redis. Le format du job
 * respecte la structure d'enveloppe standard definie dans notification-contracts:
 *
 * <pre>
 * {
 *   "eventId": "uuid",
 *   "eventType": "PasswordResetRequested",
 *   "eventVersion": 1,
 *   "occurredAt": "ISO8601",
 *   "producer": "oauth2-server",
 *   "payload": { ... }
 * }
 * </pre>
 */
@Service
public class PasswordResetQueuePublisher {

  private static final Logger log = LoggerFactory.getLogger(PasswordResetQueuePublisher.class);

  private static final String QUEUE_NAME = "password-reset";
  private static final String JOB_NAME = "send-password-reset-email";
  private static final String BULL_PREFIX = "bull";
  private static final String PRODUCER = "oauth2-server";
  private static final String EVENT_TYPE = "PasswordResetRequested";
  private static final int EVENT_VERSION = 1;

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
   * @param payload le payload contenant les informations de reset
   * @return l'ID de l'evenement (eventId)
   * @throws RuntimeException si la publication echoue ou si Redis est indisponible
   */
  public String publishPasswordResetRequested(SendPasswordResetEmailPayload payload) {
    if (!queueEnabled) {
      log.warn("Queue is disabled, password reset message will not be published");
      throw new IllegalStateException("Notification queue is disabled");
    }

    try {
      // Generer l'eventId
      UUID eventId = UUID.randomUUID();
      String jobId = eventId.toString().replace("-", "").substring(0, 16);

      // Creer le message avec enveloppe standard
      PasswordResetRequested message =
          new PasswordResetRequested()
              .withEventId(eventId)
              .withEventType(EVENT_TYPE)
              .withEventVersion(EVENT_VERSION)
              .withOccurredAt(Instant.now())
              .withProducer(PasswordResetRequested.Producer.OAUTH_2_SERVER)
              .withPayload(payload);

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
          "Published {} (eventId: {}, jobId: {}) for user {} (email: {})",
          EVENT_TYPE,
          eventId,
          jobId,
          payload.getUsername(),
          payload.getEmail());

      return eventId.toString();
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize password reset message", e);
      throw new RuntimeException("Failed to publish password reset message", e);
    } catch (Exception e) {
      log.error("Failed to publish password reset message to Redis", e);
      throw new RuntimeException("Redis unavailable - cannot publish password reset message", e);
    }
  }

  /**
   * Cree la structure du job BullMQ avec le message complet (enveloppe + payload).
   *
   * @param jobId l'ID du job
   * @param message le message avec enveloppe standard
   * @return la map representant le job
   */
  private Map<String, Object> createBullMQJob(String jobId, PasswordResetRequested message) {
    Map<String, Object> job = new HashMap<>();
    job.put("id", jobId);
    job.put("name", JOB_NAME);
    // Le data contient le message complet avec enveloppe
    job.put("data", createJobData(message));
    job.put("opts", createJobOptionsMap());
    job.put("timestamp", System.currentTimeMillis());
    job.put("attemptsMade", 0);
    return job;
  }

  /**
   * Cree les donnees du job avec le format d'enveloppe standard.
   *
   * @param message le message avec enveloppe
   * @return la map des donnees
   */
  private Map<String, Object> createJobData(PasswordResetRequested message) {
    Map<String, Object> data = new HashMap<>();

    // Enveloppe standard
    data.put("eventId", message.getEventId().toString());
    data.put("eventType", message.getEventType());
    data.put("eventVersion", message.getEventVersion());
    data.put("occurredAt", message.getOccurredAt().toString());
    data.put("producer", PRODUCER);

    // Payload
    Map<String, Object> payload = new HashMap<>();
    SendPasswordResetEmailPayload p = message.getPayload();
    payload.put("userId", p.getUserId().toString());
    payload.put("email", p.getEmail());
    payload.put("username", p.getUsername());
    payload.put("token", p.getToken());
    payload.put("resetLink", p.getResetLink().toString());
    payload.put("expiresAt", p.getExpiresAt().toString());
    payload.put("locale", p.getLocale() != null ? p.getLocale().value() : "fr");

    data.put("payload", payload);

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
}
