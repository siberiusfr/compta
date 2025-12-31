/**
 * COMPTA Notification Contracts
 *
 * Source de verite pour les contrats de communication asynchrone
 * entre oauth2-server (Spring Boot) et notification-service (NestJS)
 * via Redis/BullMQ.
 *
 * Ces schemas Zod sont utilises pour:
 * - Validation runtime dans notification-service
 * - Generation de JSON Schema pour oauth2-server (Java)
 * - Documentation automatique des contrats
 */

import { z } from 'zod';

// =============================================================================
// QUEUES ET JOBS NAMES
// =============================================================================

/**
 * Noms des queues BullMQ
 */
export const QueueNames = {
  EMAIL_VERIFICATION: 'email-verification',
  PASSWORD_RESET: 'password-reset',
} as const;

export type QueueName = (typeof QueueNames)[keyof typeof QueueNames];

/**
 * Noms des jobs BullMQ
 */
export const JobNames = {
  SEND_VERIFICATION_EMAIL: 'send-verification-email',
  SEND_PASSWORD_RESET_EMAIL: 'send-password-reset-email',
  // Events (retour notification-service -> oauth2-server)
  EMAIL_VERIFICATION_SENT: 'email-verification-sent',
  EMAIL_VERIFICATION_FAILED: 'email-verification-failed',
  PASSWORD_RESET_SENT: 'password-reset-sent',
  PASSWORD_RESET_FAILED: 'password-reset-failed',
} as const;

export type JobName = (typeof JobNames)[keyof typeof JobNames];

// =============================================================================
// BASE SCHEMAS
// =============================================================================

/**
 * Schema de base pour les metadonnees de job
 */
export const JobMetadataSchema = z.object({
  /** ID unique du job (genere par le producer) */
  jobId: z.string().min(1),
  /** Timestamp de creation du job (ISO 8601) */
  createdAt: z.string().datetime(),
  /** Nombre de tentatives (gere par BullMQ) */
  attemptNumber: z.number().int().min(0).optional(),
});

export type JobMetadata = z.infer<typeof JobMetadataSchema>;

/**
 * Options BullMQ pour les jobs
 */
export const BullMQJobOptionsSchema = z.object({
  /** Nombre maximum de tentatives */
  attempts: z.number().int().min(1).default(3),
  /** Supprimer le job apres succes */
  removeOnComplete: z.boolean().default(true),
  /** Conserver le job en cas d'echec */
  removeOnFail: z.boolean().default(false),
  /** Configuration du backoff */
  backoff: z
    .object({
      type: z.enum(['exponential', 'fixed']).default('exponential'),
      delay: z.number().int().min(100).default(1000),
    })
    .optional(),
});

export type BullMQJobOptions = z.infer<typeof BullMQJobOptionsSchema>;

// =============================================================================
// EMAIL VERIFICATION CONTRACTS
// =============================================================================

/**
 * Job: Demande d'envoi d'email de verification
 * Producer: oauth2-server
 * Consumer: notification-service
 * Queue: email-verification
 */
export const SendVerificationEmailJobSchema = z.object({
  /** Identifiant unique de l'utilisateur */
  userId: z.string().uuid(),
  /** Adresse email a verifier */
  email: z.string().email(),
  /** Nom d'utilisateur pour personnaliser l'email */
  username: z.string().min(1).max(255),
  /** Token de verification unique */
  token: z.string().min(32).max(64),
  /** Lien complet de verification */
  verificationLink: z.string().url(),
  /** Date d'expiration du token (ISO 8601) */
  expiresAt: z.string().datetime(),
  /** Locale pour le template (defaut: fr) */
  locale: z.enum(['fr', 'en', 'ar']).default('fr'),
});

export type SendVerificationEmailJob = z.infer<
  typeof SendVerificationEmailJobSchema
>;

/**
 * Event: Email de verification envoye avec succes
 * Producer: notification-service
 * Consumer: oauth2-server (optionnel)
 */
export const EmailVerificationSentEventSchema = z.object({
  /** ID du job original */
  jobId: z.string().min(1),
  /** ID utilisateur */
  userId: z.string().uuid(),
  /** Email destinataire */
  email: z.string().email(),
  /** ID du message SMTP */
  messageId: z.string().optional(),
  /** Timestamp d'envoi */
  sentAt: z.string().datetime(),
});

export type EmailVerificationSentEvent = z.infer<
  typeof EmailVerificationSentEventSchema
>;

/**
 * Event: Echec d'envoi de l'email de verification
 * Producer: notification-service
 * Consumer: oauth2-server (optionnel)
 */
export const EmailVerificationFailedEventSchema = z.object({
  /** ID du job original */
  jobId: z.string().min(1),
  /** ID utilisateur */
  userId: z.string().uuid(),
  /** Email destinataire */
  email: z.string().email(),
  /** Code d'erreur */
  errorCode: z.string().optional(),
  /** Message d'erreur */
  errorMessage: z.string(),
  /** Nombre de tentatives effectuees */
  attemptsMade: z.number().int().min(1),
  /** Timestamp de l'echec */
  failedAt: z.string().datetime(),
});

export type EmailVerificationFailedEvent = z.infer<
  typeof EmailVerificationFailedEventSchema
>;

// =============================================================================
// PASSWORD RESET CONTRACTS
// =============================================================================

/**
 * Job: Demande d'envoi d'email de reset de mot de passe
 * Producer: oauth2-server
 * Consumer: notification-service
 * Queue: password-reset
 */
export const SendPasswordResetEmailJobSchema = z.object({
  /** Identifiant unique de l'utilisateur */
  userId: z.string().uuid(),
  /** Adresse email de l'utilisateur */
  email: z.string().email(),
  /** Nom d'utilisateur pour personnaliser l'email */
  username: z.string().min(1).max(255),
  /** Token de reset unique */
  token: z.string().min(32).max(64),
  /** Lien complet de reset */
  resetLink: z.string().url(),
  /** Date d'expiration du token (ISO 8601) - 1 heure */
  expiresAt: z.string().datetime(),
  /** Locale pour le template (defaut: fr) */
  locale: z.enum(['fr', 'en', 'ar']).default('fr'),
});

export type SendPasswordResetEmailJob = z.infer<
  typeof SendPasswordResetEmailJobSchema
>;

/**
 * Event: Email de reset envoye avec succes
 * Producer: notification-service
 * Consumer: oauth2-server (optionnel)
 */
export const PasswordResetSentEventSchema = z.object({
  /** ID du job original */
  jobId: z.string().min(1),
  /** ID utilisateur */
  userId: z.string().uuid(),
  /** Email destinataire */
  email: z.string().email(),
  /** ID du message SMTP */
  messageId: z.string().optional(),
  /** Timestamp d'envoi */
  sentAt: z.string().datetime(),
});

export type PasswordResetSentEvent = z.infer<
  typeof PasswordResetSentEventSchema
>;

/**
 * Event: Echec d'envoi de l'email de reset
 * Producer: notification-service
 * Consumer: oauth2-server (optionnel)
 */
export const PasswordResetFailedEventSchema = z.object({
  /** ID du job original */
  jobId: z.string().min(1),
  /** ID utilisateur */
  userId: z.string().uuid(),
  /** Email destinataire */
  email: z.string().email(),
  /** Code d'erreur */
  errorCode: z.string().optional(),
  /** Message d'erreur */
  errorMessage: z.string(),
  /** Nombre de tentatives effectuees */
  attemptsMade: z.number().int().min(1),
  /** Timestamp de l'echec */
  failedAt: z.string().datetime(),
});

export type PasswordResetFailedEvent = z.infer<
  typeof PasswordResetFailedEventSchema
>;

// =============================================================================
// VALIDATION HELPERS
// =============================================================================

/**
 * Valide un payload de job SendVerificationEmail
 */
export function validateSendVerificationEmailJob(
  data: unknown,
): SendVerificationEmailJob {
  return SendVerificationEmailJobSchema.parse(data);
}

/**
 * Valide un payload de job SendPasswordResetEmail
 */
export function validateSendPasswordResetEmailJob(
  data: unknown,
): SendPasswordResetEmailJob {
  return SendPasswordResetEmailJobSchema.parse(data);
}

/**
 * Safe parse pour SendVerificationEmailJob (retourne success/error)
 */
export function safeParseSendVerificationEmailJob(data: unknown) {
  return SendVerificationEmailJobSchema.safeParse(data);
}

/**
 * Safe parse pour SendPasswordResetEmailJob (retourne success/error)
 */
export function safeParseSendPasswordResetEmailJob(data: unknown) {
  return SendPasswordResetEmailJobSchema.safeParse(data);
}

// =============================================================================
// EXPORTS GROUPES
// =============================================================================

/**
 * Tous les schemas de jobs (oauth2-server -> notification-service)
 */
export const JobSchemas = {
  SendVerificationEmailJob: SendVerificationEmailJobSchema,
  SendPasswordResetEmailJob: SendPasswordResetEmailJobSchema,
} as const;

/**
 * Tous les schemas d'events (notification-service -> oauth2-server)
 */
export const EventSchemas = {
  EmailVerificationSent: EmailVerificationSentEventSchema,
  EmailVerificationFailed: EmailVerificationFailedEventSchema,
  PasswordResetSent: PasswordResetSentEventSchema,
  PasswordResetFailed: PasswordResetFailedEventSchema,
} as const;

/**
 * Configuration par defaut des jobs BullMQ
 */
export const DefaultJobOptions: BullMQJobOptions = {
  attempts: 3,
  removeOnComplete: true,
  removeOnFail: false,
  backoff: {
    type: 'exponential',
    delay: 1000,
  },
};
