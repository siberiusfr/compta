import { Processor } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { MailerService } from '@nestjs-modules/mailer';
import { PinoLogger, InjectPinoLogger } from 'nestjs-pino';
import {
  QueueNames,
  PasswordResetRequested,
  safeParsePasswordResetRequested,
} from '@compta/notification-contracts';
import { BaseEmailProcessor } from './base-email-processor';

/**
 * Processor BullMQ pour les demandes de reset de mot de passe.
 *
 * Consomme les messages de la queue 'password-reset' publies par oauth2-server
 * et envoie les emails de reset via SMTP.
 *
 * Le format du message suit l'enveloppe standard:
 * {
 *   eventId: "uuid",
 *   eventType: "PasswordResetRequested",
 *   eventVersion: 1,
 *   occurredAt: "ISO8601",
 *   producer: "oauth2-server",
 *   payload: { userId, email, username, token, resetLink, expiresAt, locale }
 * }
 *
 * @see {@link PasswordResetRequested} pour le schema du message (defini dans @compta/notification-contracts)
 */
@Processor(QueueNames.PASSWORD_RESET)
export class PasswordResetProcessor extends BaseEmailProcessor {
  constructor(
    @InjectPinoLogger(PasswordResetProcessor.name)
    logger: PinoLogger,
    private readonly mailerService: MailerService,
  ) {
    super(logger);
  }

  /**
   * Get the template filename for password reset.
   */
  protected getTemplateFilename(): string {
    return 'password-reset.mjml';
  }

  /**
   * Get the email subject for password reset.
   */
  protected getEmailSubject(): string {
    return 'Reinitialisation de votre mot de passe - COMPTA';
  }

  /**
   * Traite un job de demande de reset de mot de passe.
   *
   * @param job Le job BullMQ contenant le message avec enveloppe
   * @returns Le resultat de l'envoi d'email
   */
  async process(job: Job<PasswordResetRequested, unknown, string>): Promise<unknown> {
    // Valider le message complet (enveloppe + payload) avec Zod
    const parseResult = safeParsePasswordResetRequested(job.data);
    if (!parseResult.success) {
      this.logger.error(
        { jobId: job.id, error: parseResult.error.message },
        `Invalid job payload for job ${job.id}: ${parseResult.error.message}`,
      );
      throw new Error(`Invalid job payload: ${parseResult.error.message}`);
    }

    const { eventId, eventType, producer, payload } = parseResult.data;
    const { email, username, resetLink, expiresAt, userId } = payload;

    this.logger.debug(
      { eventId, eventType, producer },
      `Processing ${eventType} (eventId: ${eventId}) from ${producer}`,
    );

    this.logger.info(
      { jobId: job.id, username, email },
      `Processing password reset job ${job.id} for user ${username} (${email})`,
    );

    try {
      // Compiler le template MJML
      const html = this.compileTemplate({
        username,
        resetLink,
        expiresAt: this.formatExpirationDate(expiresAt),
      });

      // Envoyer l'email
      const result = await this.mailerService.sendMail({
        to: email,
        subject: this.getEmailSubject(),
        html,
      });

      this.logger.info(
        { jobId: job.id, eventId, messageId: result?.messageId, email },
        `Successfully sent password reset email to ${email} (job ${job.id}, eventId: ${eventId}, messageId: ${result?.messageId})`,
      );

      return {
        success: true,
        eventId,
        messageId: result?.messageId,
        userId,
        email,
      };
    } catch (error: unknown) {
      const err = error as Error;
      this.logger.error(
        { jobId: job.id, email, error: err.message, stack: err.stack },
        `Failed to send password reset email to ${email}: ${err.message}`,
      );

      throw error; // Re-throw pour permettre le retry BullMQ
    }
  }
}
