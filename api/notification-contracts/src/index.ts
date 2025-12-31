/**
 * COMPTA Notification Contracts
 *
 * Types TypeScript pour la communication asynchrone entre services COMPTA
 * via Redis/BullMQ.
 */

/**
 * Payload du message de demande de verification d'email.
 *
 * Utilise pour la communication asynchrone entre oauth2-server et notification-service
 * via Redis/BullMQ.
 */
export interface EmailVerificationPayload {
  /** Identifiant unique de l'utilisateur (UUID) */
  userId: string;

  /** Adresse email a verifier */
  email: string;

  /** Nom d'utilisateur pour personnaliser l'email */
  username: string;

  /** Token de verification unique */
  token: string;

  /** Lien complet de verification a inclure dans l'email */
  verificationLink: string;

  /** Date et heure d'expiration du token (ISO 8601) */
  expiresAt: string;
}

/**
 * Payload du message de demande de reinitialisation de mot de passe.
 *
 * Utilise pour la communication asynchrone entre oauth2-server et notification-service
 * via Redis/BullMQ.
 */
export interface PasswordResetPayload {
  /** Identifiant unique de l'utilisateur (UUID) */
  userId: string;

  /** Adresse email de l'utilisateur */
  email: string;

  /** Nom d'utilisateur pour personnaliser l'email */
  username: string;

  /** Token de reset unique */
  token: string;

  /** Lien complet de reset a inclure dans l'email */
  resetLink: string;

  /** Date et heure d'expiration du token (ISO 8601) - 1 heure apres creation */
  expiresAt: string;
}

/**
 * Constantes pour les noms de queues BullMQ.
 */
export const NotificationQueues = {
  /** Queue pour les demandes de verification d'email */
  EMAIL_VERIFICATION: 'email-verification',

  /** Queue pour les demandes de reinitialisation de mot de passe */
  PASSWORD_RESET: 'password-reset',

  /** Prefixe Redis pour les queues BullMQ */
  BULL_PREFIX: 'bull',
} as const;

/**
 * Noms des jobs BullMQ.
 */
export const JobNames = {
  /** Job de demande de verification d'email */
  EMAIL_VERIFICATION_REQUESTED: 'email-verification-requested',

  /** Job de demande de reinitialisation de mot de passe */
  PASSWORD_RESET_REQUESTED: 'password-reset-requested',
} as const;

/**
 * Type utilitaire pour les valeurs de NotificationQueues.
 */
export type NotificationQueueName =
  (typeof NotificationQueues)[keyof typeof NotificationQueues];

/**
 * Type utilitaire pour les noms de jobs.
 */
export type JobName = (typeof JobNames)[keyof typeof JobNames];
