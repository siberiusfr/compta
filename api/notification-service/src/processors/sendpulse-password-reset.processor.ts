import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { PinoLogger, InjectPinoLogger } from 'nestjs-pino';
import { SendPulseService } from '../sendpulse/sendpulse.service';
import mjml from 'mjml';
import * as fs from 'fs';
import * as path from 'path';
import {
  QueueNames,
  PasswordResetRequested,
  safeParsePasswordResetRequested,
} from '@compta/notification-contracts';
import { NotificationException } from '../common/exceptions/notification.exception';

/**
 * Processor BullMQ pour les demandes de reset de mot de passe utilisant SendPulse.
 *
 * Ce processor utilise SendPulse API au lieu de SMTP direct pour envoyer les emails.
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
export class SendPulsePasswordResetProcessor extends WorkerHost {
  private templateCache: string | null = null;

  constructor(
    @InjectPinoLogger(SendPulsePasswordResetProcessor.name)
    private readonly logger: PinoLogger,
    private readonly sendPulseService: SendPulseService,
  ) {
    super();
  }

  /**
   * Traite un job de demande de reset de mot de passe avec SendPulse.
   *
   * @param job Le job BullMQ contenant le message avec enveloppe
   * @returns Le resultat de l'envoi d'email
   */
  async process(
    job: Job<PasswordResetRequested, unknown, string>,
  ): Promise<unknown> {
    // Valider le message complet (enveloppe + payload) avec Zod
    const parseResult = safeParsePasswordResetRequested(job.data);
    if (!parseResult.success) {
      const errorMessage = parseResult.error.message || 'Unknown validation error';
      this.logger.error(
        { jobId: job.id, error: errorMessage },
        `Invalid job payload for job ${job.id}: ${errorMessage}`,
      );
      throw NotificationException.invalidPayload(String(job.id), errorMessage);
    }

    const { eventId, eventType, producer, payload } = parseResult.data;
    const { email, username, resetLink, expiresAt, userId } = payload;

    this.logger.debug(
      { eventId, eventType, producer },
      `Processing ${eventType} (eventId: ${eventId}) from ${producer}`,
    );

    this.logger.info(
      { jobId: job.id, username, email, provider: 'sendpulse' },
      `Processing password reset job ${job.id} for user ${username} (${email}) via SendPulse`,
    );

    try {
      // Charger et compiler le template MJML
      const html = this.compileTemplate({
        username,
        resetLink,
        expiresAt: this.formatExpirationDate(expiresAt),
      });

      // Envoyer l'email via SendPulse
      const result = await this.sendPulseService.sendHtmlEmail(
        html,
        `Bonjour ${username},\n\nVous avez demandé la réinitialisation de votre mot de passe. Veuillez cliquer sur le lien suivant:\n\n${resetLink}\n\nCe lien expire le ${this.formatExpirationDate(expiresAt)}.\n\nSi vous n'avez pas demandé cette réinitialisation, ignorez cet email.`,
        [{ email, name: username }],
        {
          email: process.env.SENDPULSE_SENDER_EMAIL || 'noreply@compta.tn',
          name: process.env.SENDPULSE_SENDER_NAME || 'COMPTA',
        },
        'Réinitialisation de votre mot de passe - COMPTA',
        {
          autoPlainText: true,
        },
      );

      this.logger.info(
        { jobId: job.id, eventId, messageId: result?.id, email, provider: 'sendpulse' },
        `Successfully sent password reset email to ${email} via SendPulse (job ${job.id}, eventId: ${eventId}, messageId: ${result?.id})`,
      );

      return {
        success: true,
        eventId,
        messageId: result?.id,
        userId,
        email,
        provider: 'sendpulse',
      };
    } catch (error: unknown) {
      const err = error as Error;
      this.logger.error(
        { jobId: job.id, email, error: err.message, stack: err.stack, provider: 'sendpulse' },
        `Failed to send password reset email to ${email} via SendPulse: ${err.message}`,
      );

      throw error; // Re-throw pour permettre le retry BullMQ
    }
  }

  /**
   * Compile le template MJML avec les variables fournies.
   *
   * @param variables Les variables a injecter dans le template
   * @returns Le HTML compile
   */
  private compileTemplate(variables: {
    username: string;
    resetLink: string;
    expiresAt: string;
  }): string {
    // Charger le template depuis le cache ou le fichier
    let mjmlTemplate = this.loadTemplate();

    // Remplacer les variables
    mjmlTemplate = mjmlTemplate
      .replace(/\{\{username\}\}/g, variables.username)
      .replace(/\{\{resetLink\}\}/g, variables.resetLink)
      .replace(/\{\{expiresAt\}\}/g, variables.expiresAt);

    // Compiler MJML en HTML
    const { html, errors } = mjml(mjmlTemplate, {
      validationLevel: 'soft',
    });

    if (errors && errors.length > 0) {
      this.logger.warn(
        { errors: errors.map((e) => e.message) },
        `MJML compilation warnings: ${errors.map((e) => e.message).join(', ')}`,
      );
    }

    return html;
  }

  /**
   * Charge le template MJML depuis le fichier.
   * Utilise un cache en memoire pour eviter les lectures repetees.
   *
   * @returns Le contenu du template MJML
   */
  private loadTemplate(): string {
    if (this.templateCache) {
      return this.templateCache;
    }

    const templatePath = path.resolve(
      __dirname,
      '..',
      'templates',
      'password-reset.mjml',
    );

    try {
      this.templateCache = fs.readFileSync(templatePath, 'utf8');
      this.logger.info(
        { templatePath },
        `Loaded password reset template from ${templatePath}`,
      );
      return this.templateCache;
    } catch (error: unknown) {
      const err = error as Error;
      this.logger.error(
        { templatePath, error: err.message },
        `Failed to load template from ${templatePath}: ${err.message}`,
      );
      throw NotificationException.templateLoadFailed(templatePath, err.message);
    }
  }

  /**
   * Formate la date d'expiration pour l'affichage dans l'email.
   *
   * @param expiresAt La date d'expiration au format ISO 8601
   * @returns La date formatee en francais
   */
  private formatExpirationDate(expiresAt: string): string {
    try {
      const date = new Date(expiresAt);
      return date.toLocaleString('fr-FR', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        timeZone: 'Africa/Tunis',
      });
    } catch {
      return expiresAt; // Fallback au format original si parsing echoue
    }
  }
}
