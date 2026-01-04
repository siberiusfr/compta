import { Processor } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { MailerService } from '@nestjs-modules/mailer';
import { PinoLogger, InjectPinoLogger } from 'nestjs-pino';
import {
  QueueNames,
  EmailVerificationRequested,
  safeParseEmailVerificationRequested,
} from '@compta/notification-contracts';
import { BaseEmailProcessor } from './base-email-processor';

/**
 * Processor BullMQ pour les demandes de verification d'email.
 *
 * Consomme les messages de la queue 'email-verification' publies par oauth2-server
 * et envoie les emails de verification via SMTP.
 *
 * Le format du message suit l'enveloppe standard:
 * {
 *   eventId: "uuid",
 *   eventType: "EmailVerificationRequested",
 *   eventVersion: 1,
 *   occurredAt: "ISO8601",
 *   producer: "oauth2-server",
 *   payload: { userId, email, username, token, verificationLink, expiresAt, locale }
 * }
 *
 * @see {@link EmailVerificationRequested} pour le schema du message (defini dans @compta/notification-contracts)
 */
@Processor(QueueNames.EMAIL_VERIFICATION)
export class EmailVerificationProcessor extends BaseEmailProcessor {
  constructor(
    @InjectPinoLogger(EmailVerificationProcessor.name)
    logger: PinoLogger,
    private readonly mailerService: MailerService,
  ) {
    super(logger);
  }

  /**
   * Get the template filename for email verification.
   */
  protected getTemplateFilename(): string {
    return 'email-verification.mjml';
  }

  /**
   * Get the email subject for email verification.
   */
  protected getEmailSubject(): string {
    return 'Verification de votre adresse email - COMPTA';
  }

  /**
   * Traite un job de demande de verification d'email.
   *
   * @param job Le job BullMQ contenant le message avec enveloppe
   * @returns Le resultat de l'envoi d'email
   */
  async process(
    job: Job<EmailVerificationRequested, unknown, string>,
  ): Promise<unknown> {
    // Valider le message complet (enveloppe + payload) avec Zod
    const parseResult = safeParseEmailVerificationRequested(job.data);
    if (!parseResult.success) {
      this.logger.error(
        { jobId: job.id, error: parseResult.error.message },
        `Invalid job payload for job ${job.id}: ${parseResult.error.message}`,
      );
      throw new Error(`Invalid job payload: ${parseResult.error.message}`);
    }

    const { eventId, eventType, producer, payload } = parseResult.data;
    const { email, username, verificationLink, expiresAt, userId } = payload;

    this.logger.debug(
      { eventId, eventType, producer },
      `Processing ${eventType} (eventId: ${eventId}) from ${producer}`,
    );

    this.logger.info(
      { jobId: job.id, username, email },
      `Processing email verification job ${job.id} for user ${username} (${email})`,
    );

    try {
      // Compiler le template MJML
      const html = this.compileTemplate({
        username,
        verificationLink,
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
        `Successfully sent verification email to ${email} (job ${job.id}, eventId: ${eventId}, messageId: ${result?.messageId})`,
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
        `Failed to send verification email to ${email}: ${err.message}`,
      );

      throw error; // Re-throw pour permettre le retry BullMQ
    }
  }
}
