package tn.cyberious.compta.contracts.notification;

/**
 * Constantes pour les noms de queues BullMQ.
 *
 * <p>Ces constantes doivent etre utilisees par les producers (oauth2-server) et les consumers
 * (notification-service) pour garantir la coherence.
 */
public final class NotificationQueues {

  private NotificationQueues() {
    // Prevent instantiation
  }

  /** Queue pour les demandes de verification d'email. */
  public static final String EMAIL_VERIFICATION = "email-verification";

  /** Prefixe Redis pour les queues BullMQ. */
  public static final String BULL_PREFIX = "bull";

  /**
   * Retourne le nom complet de la queue Redis.
   *
   * @param queueName le nom de la queue
   * @return le nom complet avec le prefixe bull
   */
  public static String getFullQueueName(String queueName) {
    return BULL_PREFIX + ":" + queueName;
  }
}
