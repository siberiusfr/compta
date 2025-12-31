/**
 * COMPTA Notification Contracts
 *
 * Source de verite pour les contrats de communication asynchrone
 * entre oauth2-server (Spring Boot) et notification-service (NestJS)
 * via Redis/BullMQ.
 *
 * IMPORTANT: Tous les messages suivent le format d'enveloppe standard:
 * {
 *   eventId: "uuid",
 *   eventType: "NomDuType",
 *   eventVersion: 1,
 *   occurredAt: "ISO8601",
 *   producer: "nom-du-service",
 *   payload: { ... }
 * }
 */

import { z } from 'zod';

// =============================================================================
// ENVELOPPE STANDARD
// =============================================================================

/**
 * Producteurs de messages connus
 */
export const Producers = {
  OAUTH2_SERVER: 'oauth2-server',
  NOTIFICATION_SERVICE: 'notification-service',
} as const;

export type Producer = (typeof Producers)[keyof typeof Producers];

/**
 * Types d'evenements connus
 */
export const EventTypes = {
  // Jobs (oauth2-server -> notification-service)
  EMAIL_VERIFICATION_REQUESTED: 'EmailVerificationRequested',
  PASSWORD_RESET_REQUESTED: 'PasswordResetRequested',
  // Events (notification-service -> oauth2-server)
  EMAIL_VERIFICATION_SENT: 'EmailVerificationSent',
  EMAIL_VERIFICATION_FAILED: 'EmailVerificationFailed',
  PASSWORD_RESET_SENT: 'PasswordResetSent',
  PASSWORD_RESET_FAILED: 'PasswordResetFailed',
} as const;

export type EventType = (typeof EventTypes)[keyof typeof EventTypes];

/**
 * Schema de base pour l'enveloppe de message
 * Tous les messages doivent suivre ce format
 */
export const EventEnvelopeSchema = <T extends z.ZodTypeAny>(
  eventType: string,
  payloadSchema: T,
) =>
  z.object({
    /** Identifiant unique de l'evenement (UUID v4) */
    eventId: z.string().uuid(),
    /** Type de l'evenement */
    eventType: z.literal(eventType),
    /** Version du schema (pour evolution) */
    eventVersion: z.number().int().min(1).default(1),
    /** Timestamp de creation de l'evenement (ISO 8601) */
    occurredAt: z.string().datetime(),
    /** Service producteur du message */
    producer: z.enum([Producers.OAUTH2_SERVER, Producers.NOTIFICATION_SERVICE]),
    /** Payload specifique a l'evenement */
    payload: payloadSchema,
  });

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
// PAYLOADS
// =============================================================================

/**
 * Payload pour la demande d'envoi d'email de verification
 */
export const SendVerificationEmailPayloadSchema = z.object({
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

export type SendVerificationEmailPayload = z.infer<
  typeof SendVerificationEmailPayloadSchema
>;

/**
 * Payload pour la demande d'envoi d'email de reset de mot de passe
 */
export const SendPasswordResetEmailPayloadSchema = z.object({
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

export type SendPasswordResetEmailPayload = z.infer<
  typeof SendPasswordResetEmailPayloadSchema
>;

/**
 * Payload pour l'evenement email de verification envoye
 */
export const EmailVerificationSentPayloadSchema = z.object({
  /** ID utilisateur */
  userId: z.string().uuid(),
  /** Email destinataire */
  email: z.string().email(),
  /** ID du message SMTP */
  messageId: z.string().optional(),
  /** Timestamp d'envoi */
  sentAt: z.string().datetime(),
});

export type EmailVerificationSentPayload = z.infer<
  typeof EmailVerificationSentPayloadSchema
>;

/**
 * Payload pour l'evenement echec d'envoi email de verification
 */
export const EmailVerificationFailedPayloadSchema = z.object({
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

export type EmailVerificationFailedPayload = z.infer<
  typeof EmailVerificationFailedPayloadSchema
>;

/**
 * Payload pour l'evenement email de reset envoye
 */
export const PasswordResetSentPayloadSchema = z.object({
  /** ID utilisateur */
  userId: z.string().uuid(),
  /** Email destinataire */
  email: z.string().email(),
  /** ID du message SMTP */
  messageId: z.string().optional(),
  /** Timestamp d'envoi */
  sentAt: z.string().datetime(),
});

export type PasswordResetSentPayload = z.infer<
  typeof PasswordResetSentPayloadSchema
>;

/**
 * Payload pour l'evenement echec d'envoi email de reset
 */
export const PasswordResetFailedPayloadSchema = z.object({
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

export type PasswordResetFailedPayload = z.infer<
  typeof PasswordResetFailedPayloadSchema
>;

// =============================================================================
// MESSAGES COMPLETS (ENVELOPPE + PAYLOAD)
// =============================================================================

/**
 * Message complet: Demande d'envoi d'email de verification
 * Producer: oauth2-server
 * Consumer: notification-service
 * Queue: email-verification
 */
export const EmailVerificationRequestedSchema = EventEnvelopeSchema(
  EventTypes.EMAIL_VERIFICATION_REQUESTED,
  SendVerificationEmailPayloadSchema,
);

export type EmailVerificationRequested = z.infer<
  typeof EmailVerificationRequestedSchema
>;

/**
 * Message complet: Demande d'envoi d'email de reset de mot de passe
 * Producer: oauth2-server
 * Consumer: notification-service
 * Queue: password-reset
 */
export const PasswordResetRequestedSchema = EventEnvelopeSchema(
  EventTypes.PASSWORD_RESET_REQUESTED,
  SendPasswordResetEmailPayloadSchema,
);

export type PasswordResetRequested = z.infer<
  typeof PasswordResetRequestedSchema
>;

/**
 * Message complet: Email de verification envoye avec succes
 * Producer: notification-service
 * Consumer: oauth2-server (optionnel)
 */
export const EmailVerificationSentSchema = EventEnvelopeSchema(
  EventTypes.EMAIL_VERIFICATION_SENT,
  EmailVerificationSentPayloadSchema,
);

export type EmailVerificationSent = z.infer<typeof EmailVerificationSentSchema>;

/**
 * Message complet: Echec d'envoi de l'email de verification
 * Producer: notification-service
 * Consumer: oauth2-server (optionnel)
 */
export const EmailVerificationFailedSchema = EventEnvelopeSchema(
  EventTypes.EMAIL_VERIFICATION_FAILED,
  EmailVerificationFailedPayloadSchema,
);

export type EmailVerificationFailed = z.infer<
  typeof EmailVerificationFailedSchema
>;

/**
 * Message complet: Email de reset envoye avec succes
 * Producer: notification-service
 * Consumer: oauth2-server (optionnel)
 */
export const PasswordResetSentSchema = EventEnvelopeSchema(
  EventTypes.PASSWORD_RESET_SENT,
  PasswordResetSentPayloadSchema,
);

export type PasswordResetSent = z.infer<typeof PasswordResetSentSchema>;

/**
 * Message complet: Echec d'envoi de l'email de reset
 * Producer: notification-service
 * Consumer: oauth2-server (optionnel)
 */
export const PasswordResetFailedSchema = EventEnvelopeSchema(
  EventTypes.PASSWORD_RESET_FAILED,
  PasswordResetFailedPayloadSchema,
);

export type PasswordResetFailed = z.infer<typeof PasswordResetFailedSchema>;

// =============================================================================
// BULLMQ JOB OPTIONS
// =============================================================================

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

// =============================================================================
// VALIDATION HELPERS
// =============================================================================

/**
 * Valide un message EmailVerificationRequested
 */
export function validateEmailVerificationRequested(
  data: unknown,
): EmailVerificationRequested {
  return EmailVerificationRequestedSchema.parse(data);
}

/**
 * Safe parse pour EmailVerificationRequested
 */
export function safeParseEmailVerificationRequested(data: unknown) {
  return EmailVerificationRequestedSchema.safeParse(data);
}

/**
 * Valide un message PasswordResetRequested
 */
export function validatePasswordResetRequested(
  data: unknown,
): PasswordResetRequested {
  return PasswordResetRequestedSchema.parse(data);
}

/**
 * Safe parse pour PasswordResetRequested
 */
export function safeParsePasswordResetRequested(data: unknown) {
  return PasswordResetRequestedSchema.safeParse(data);
}

/**
 * Valide un message EmailVerificationSent
 */
export function validateEmailVerificationSent(
  data: unknown,
): EmailVerificationSent {
  return EmailVerificationSentSchema.parse(data);
}

/**
 * Safe parse pour EmailVerificationSent
 */
export function safeParseEmailVerificationSent(data: unknown) {
  return EmailVerificationSentSchema.safeParse(data);
}

/**
 * Valide un message EmailVerificationFailed
 */
export function validateEmailVerificationFailed(
  data: unknown,
): EmailVerificationFailed {
  return EmailVerificationFailedSchema.parse(data);
}

/**
 * Safe parse pour EmailVerificationFailed
 */
export function safeParseEmailVerificationFailed(data: unknown) {
  return EmailVerificationFailedSchema.safeParse(data);
}

/**
 * Valide un message PasswordResetSent
 */
export function validatePasswordResetSent(data: unknown): PasswordResetSent {
  return PasswordResetSentSchema.parse(data);
}

/**
 * Safe parse pour PasswordResetSent
 */
export function safeParsePasswordResetSent(data: unknown) {
  return PasswordResetSentSchema.safeParse(data);
}

/**
 * Valide un message PasswordResetFailed
 */
export function validatePasswordResetFailed(
  data: unknown,
): PasswordResetFailed {
  return PasswordResetFailedSchema.parse(data);
}

/**
 * Safe parse pour PasswordResetFailed
 */
export function safeParsePasswordResetFailed(data: unknown) {
  return PasswordResetFailedSchema.safeParse(data);
}

// =============================================================================
// FACTORY HELPERS
// =============================================================================

/**
 * Cree une enveloppe de message avec les valeurs par defaut
 */
export function createEventEnvelope<T>(
  eventType: EventType,
  producer: Producer,
  payload: T,
): {
  eventId: string;
  eventType: EventType;
  eventVersion: number;
  occurredAt: string;
  producer: Producer;
  payload: T;
} {
  return {
    eventId: crypto.randomUUID(),
    eventType,
    eventVersion: 1,
    occurredAt: new Date().toISOString(),
    producer,
    payload,
  };
}

// =============================================================================
// EXPORTS GROUPES
// =============================================================================

/**
 * Tous les schemas de messages (enveloppe + payload)
 */
export const MessageSchemas = {
  EmailVerificationRequested: EmailVerificationRequestedSchema,
  PasswordResetRequested: PasswordResetRequestedSchema,
  EmailVerificationSent: EmailVerificationSentSchema,
  EmailVerificationFailed: EmailVerificationFailedSchema,
  PasswordResetSent: PasswordResetSentSchema,
  PasswordResetFailed: PasswordResetFailedSchema,
} as const;

/**
 * Tous les schemas de payload uniquement
 */
export const PayloadSchemas = {
  SendVerificationEmail: SendVerificationEmailPayloadSchema,
  SendPasswordResetEmail: SendPasswordResetEmailPayloadSchema,
  EmailVerificationSent: EmailVerificationSentPayloadSchema,
  EmailVerificationFailed: EmailVerificationFailedPayloadSchema,
  PasswordResetSent: PasswordResetSentPayloadSchema,
  PasswordResetFailed: PasswordResetFailedPayloadSchema,
} as const;

// =============================================================================
// BACKWARD COMPATIBILITY (deprecated - use new names)
// =============================================================================

/** @deprecated Use EmailVerificationRequestedSchema instead */
export const SendVerificationEmailJobSchema = EmailVerificationRequestedSchema;
/** @deprecated Use EmailVerificationRequested instead */
export type SendVerificationEmailJob = EmailVerificationRequested;
/** @deprecated Use validateEmailVerificationRequested instead */
export const validateSendVerificationEmailJob =
  validateEmailVerificationRequested;
/** @deprecated Use safeParseEmailVerificationRequested instead */
export const safeParseSendVerificationEmailJob =
  safeParseEmailVerificationRequested;

/** @deprecated Use PasswordResetRequestedSchema instead */
export const SendPasswordResetEmailJobSchema = PasswordResetRequestedSchema;
/** @deprecated Use PasswordResetRequested instead */
export type SendPasswordResetEmailJob = PasswordResetRequested;
/** @deprecated Use validatePasswordResetRequested instead */
export const validateSendPasswordResetEmailJob = validatePasswordResetRequested;
/** @deprecated Use safeParsePasswordResetRequested instead */
export const safeParseSendPasswordResetEmailJob =
  safeParsePasswordResetRequested;

/** @deprecated Use EmailVerificationSentSchema instead */
export const EmailVerificationSentEventSchema = EmailVerificationSentSchema;
/** @deprecated Use EmailVerificationSent instead */
export type EmailVerificationSentEvent = EmailVerificationSent;

/** @deprecated Use EmailVerificationFailedSchema instead */
export const EmailVerificationFailedEventSchema = EmailVerificationFailedSchema;
/** @deprecated Use EmailVerificationFailed instead */
export type EmailVerificationFailedEvent = EmailVerificationFailed;

/** @deprecated Use PasswordResetSentSchema instead */
export const PasswordResetSentEventSchema = PasswordResetSentSchema;
/** @deprecated Use PasswordResetSent instead */
export type PasswordResetSentEvent = PasswordResetSent;

/** @deprecated Use PasswordResetFailedSchema instead */
export const PasswordResetFailedEventSchema = PasswordResetFailedSchema;
/** @deprecated Use PasswordResetFailed instead */
export type PasswordResetFailedEvent = PasswordResetFailed;

/** @deprecated Use MessageSchemas instead */
export const JobSchemas = MessageSchemas;
/** @deprecated Use MessageSchemas instead */
export const EventSchemas = MessageSchemas;

/** @deprecated Use JobMetadata from envelope instead */
export const JobMetadataSchema = z.object({
  jobId: z.string().min(1),
  createdAt: z.string().datetime(),
  attemptNumber: z.number().int().min(0).optional(),
});
/** @deprecated Use envelope fields instead */
export type JobMetadata = z.infer<typeof JobMetadataSchema>;
