/**
 * COMPTA Notification Contracts
 *
 * Module principal exportant tous les contrats de communication asynchrone
 * entre oauth2-server (Spring Boot) et notification-service (NestJS)
 * via Redis/BullMQ.
 *
 * @example
 * ```typescript
 * import {
 *   SendVerificationEmailJobSchema,
 *   validateSendVerificationEmailJob,
 *   QueueNames,
 *   JobNames,
 * } from '@compta/notification-contracts';
 * ```
 */

// Re-export tous les schemas et types depuis email-contracts
export {
  // Constantes
  QueueNames,
  JobNames,
  DefaultJobOptions,
  // Schemas Zod
  JobMetadataSchema,
  BullMQJobOptionsSchema,
  SendVerificationEmailJobSchema,
  SendPasswordResetEmailJobSchema,
  EmailVerificationSentEventSchema,
  EmailVerificationFailedEventSchema,
  PasswordResetSentEventSchema,
  PasswordResetFailedEventSchema,
  // Types inferes
  type JobMetadata,
  type BullMQJobOptions,
  type SendVerificationEmailJob,
  type SendPasswordResetEmailJob,
  type EmailVerificationSentEvent,
  type EmailVerificationFailedEvent,
  type PasswordResetSentEvent,
  type PasswordResetFailedEvent,
  type QueueName,
  type JobName,
  // Fonctions de validation
  validateSendVerificationEmailJob,
  validateSendPasswordResetEmailJob,
  safeParseSendVerificationEmailJob,
  safeParseSendPasswordResetEmailJob,
  // Groupes de schemas
  JobSchemas,
  EventSchemas,
} from './email-contracts';
