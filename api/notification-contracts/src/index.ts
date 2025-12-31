/**
 * COMPTA Notification Contracts
 *
 * Module principal exportant tous les contrats de communication asynchrone
 * entre oauth2-server (Spring Boot) et notification-service (NestJS)
 * via Redis/BullMQ.
 *
 * STRUCTURE DES MESSAGES:
 * Tous les messages suivent le format d'enveloppe standard:
 * {
 *   eventId: "uuid",
 *   eventType: "NomDuType",
 *   eventVersion: 1,
 *   occurredAt: "ISO8601",
 *   producer: "nom-du-service",
 *   payload: { ... }
 * }
 *
 * @example
 * ```typescript
 * import {
 *   EmailVerificationRequestedSchema,
 *   safeParseEmailVerificationRequested,
 *   QueueNames,
 *   EventTypes,
 *   Producers,
 *   createEventEnvelope,
 * } from '@compta/notification-contracts';
 * ```
 */

export {
  // =============================================================================
  // ENVELOPPE STANDARD
  // =============================================================================
  EventEnvelopeSchema,
  Producers,
  EventTypes,
  type Producer,
  type EventType,

  // =============================================================================
  // CONSTANTES
  // =============================================================================
  QueueNames,
  JobNames,
  DefaultJobOptions,
  type QueueName,
  type JobName,

  // =============================================================================
  // SCHEMAS PAYLOAD (sans enveloppe)
  // =============================================================================
  SendVerificationEmailPayloadSchema,
  SendPasswordResetEmailPayloadSchema,
  EmailVerificationSentPayloadSchema,
  EmailVerificationFailedPayloadSchema,
  PasswordResetSentPayloadSchema,
  PasswordResetFailedPayloadSchema,
  type SendVerificationEmailPayload,
  type SendPasswordResetEmailPayload,
  type EmailVerificationSentPayload,
  type EmailVerificationFailedPayload,
  type PasswordResetSentPayload,
  type PasswordResetFailedPayload,

  // =============================================================================
  // SCHEMAS MESSAGE COMPLET (enveloppe + payload)
  // =============================================================================
  EmailVerificationRequestedSchema,
  PasswordResetRequestedSchema,
  EmailVerificationSentSchema,
  EmailVerificationFailedSchema,
  PasswordResetSentSchema,
  PasswordResetFailedSchema,
  type EmailVerificationRequested,
  type PasswordResetRequested,
  type EmailVerificationSent,
  type EmailVerificationFailed,
  type PasswordResetSent,
  type PasswordResetFailed,

  // =============================================================================
  // BULLMQ OPTIONS
  // =============================================================================
  BullMQJobOptionsSchema,
  type BullMQJobOptions,

  // =============================================================================
  // FONCTIONS DE VALIDATION
  // =============================================================================
  validateEmailVerificationRequested,
  safeParseEmailVerificationRequested,
  validatePasswordResetRequested,
  safeParsePasswordResetRequested,
  validateEmailVerificationSent,
  safeParseEmailVerificationSent,
  validateEmailVerificationFailed,
  safeParseEmailVerificationFailed,
  validatePasswordResetSent,
  safeParsePasswordResetSent,
  validatePasswordResetFailed,
  safeParsePasswordResetFailed,

  // =============================================================================
  // FACTORY HELPERS
  // =============================================================================
  createEventEnvelope,

  // =============================================================================
  // GROUPES DE SCHEMAS
  // =============================================================================
  MessageSchemas,
  PayloadSchemas,

  // =============================================================================
  // BACKWARD COMPATIBILITY (deprecated)
  // =============================================================================
  /** @deprecated */ SendVerificationEmailJobSchema,
  /** @deprecated */ type SendVerificationEmailJob,
  /** @deprecated */ validateSendVerificationEmailJob,
  /** @deprecated */ safeParseSendVerificationEmailJob,
  /** @deprecated */ SendPasswordResetEmailJobSchema,
  /** @deprecated */ type SendPasswordResetEmailJob,
  /** @deprecated */ validateSendPasswordResetEmailJob,
  /** @deprecated */ safeParseSendPasswordResetEmailJob,
  /** @deprecated */ EmailVerificationSentEventSchema,
  /** @deprecated */ type EmailVerificationSentEvent,
  /** @deprecated */ EmailVerificationFailedEventSchema,
  /** @deprecated */ type EmailVerificationFailedEvent,
  /** @deprecated */ PasswordResetSentEventSchema,
  /** @deprecated */ type PasswordResetSentEvent,
  /** @deprecated */ PasswordResetFailedEventSchema,
  /** @deprecated */ type PasswordResetFailedEvent,
  /** @deprecated */ JobSchemas,
  /** @deprecated */ EventSchemas,
  /** @deprecated */ JobMetadataSchema,
  /** @deprecated */ type JobMetadata,
} from './email-contracts';
