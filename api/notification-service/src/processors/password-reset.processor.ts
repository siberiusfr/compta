import { Processor } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { MailerService } from '@nestjs-modules/mailer';
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
  constructor(private readonly mailerService: MailerService) {
    super();
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
  async process(job: Job<PasswordResetRequested, any, string>): Promise<any> {
    // Valider le message complet (enveloppe + payload) avec Zod
    const parseResult = safeParsePasswordResetRequested(job.data);
    if (!parseResult.success) {
      this.logger.error(
        `Invalid job payload for job ${job.id}: ${parseResult.error.message}`,
      );
      throw new Error(`Invalid job payload: ${parseResult.error.message}`);
    }

    const { eventId, eventType, producer, payload } = parseResult.data;
    const { email, username, resetLink, expiresAt, userId, locale } = payload;

    this.logger.debug(
      `Processing ${eventType} (eventId: ${eventId}) from ${producer}`,
    );

    this.logger.log(
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

      this.logger.log(
        `Successfully sent password reset email to ${email} (job ${job.id}, eventId: ${eventId}, messageId: ${result?.messageId})`,
      );

      return {
        success: true,
        eventId,
        messageId: result?.messageId,
        userId,
        email,
      };
    } catch (error: any) {
      this.logger.error(
        `Failed to send password reset email to ${email}: ${error.message}`,
        error.stack,
      );

      throw error; // Re-throw pour permettre le retry BullMQ
    }
  }
}
