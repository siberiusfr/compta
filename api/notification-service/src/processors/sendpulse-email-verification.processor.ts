import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { Logger } from '@nestjs/common';
import { SendPulseService } from '../sendpulse/sendpulse.service';
import mjml from 'mjml';
import * as fs from 'fs';
import * as path from 'path';
import {
  QueueNames,
  EmailVerificationRequested,
  safeParseEmailVerificationRequested,
} from '@compta/notification-contracts';

/**
 * Processor BullMQ pour les demandes de verification d'email utilisant SendPulse.
 *
 * Ce processor utilise SendPulse API au lieu de SMTP direct pour envoyer les emails.
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
export class SendPulseEmailVerificationProcessor extends WorkerHost {
  private readonly logger = new Logger(SendPulseEmailVerificationProcessor.name);
  private templateCache: string | null = null;

  constructor(private readonly sendPulseService: SendPulseService) {
    super();
  }

  /**
   * Traite un job de demande de verification d'email avec SendPulse.
   *
   * @param job Le job BullMQ contenant le message avec enveloppe
   * @returns Le resultat de l'envoi d'email
   */
  async process(
    job: Job<EmailVerificationRequested, any, string>,
  ): Promise<any> {
    // Valider le message complet (enveloppe + payload) avec Zod
    const parseResult = safeParseEmailVerificationRequested(job.data);
    if (!parseResult.success) {
      this.logger.error(
        `Invalid job payload for job ${job.id}: ${parseResult.error.message}`,
      );
      throw new Error(`Invalid job payload: ${parseResult.error.message}`);
    }

    const { eventId, eventType, producer, payload } = parseResult.data;
    const { email, username, verificationLink, expiresAt, userId, locale } = payload;

    this.logger.debug(
      `Processing ${eventType} (eventId: ${eventId}) from ${producer}`,
    );

    this.logger.log(
      `Processing email verification job ${job.id} for user ${username} (${email}) via SendPulse`,
    );

    try {
      // Charger et compiler le template MJML
      const html = this.compileTemplate({
        username,
        verificationLink,
        expiresAt: this.formatExpirationDate(expiresAt),
      });

      // Envoyer l'email via SendPulse
      const result = await this.sendPulseService.sendHtmlEmail(
        html,
        `Bonjour ${username},\n\nVeuillez cliquer sur le lien suivant pour vérifier votre adresse email:\n\n${verificationLink}\n\nCe lien expire le ${this.formatExpirationDate(expiresAt)}.`,
        [{ email, name: username }],
        {
          email: process.env.SENDPULSE_SENDER_EMAIL || 'noreply@compta.tn',
          name: process.env.SENDPULSE_SENDER_NAME || 'COMPTA',
        },
        'Verification de votre adresse email - COMPTA',
        {
          autoPlainText: true,
        },
      );

      this.logger.log(
        `Successfully sent verification email to ${email} via SendPulse (job ${job.id}, eventId: ${eventId}, messageId: ${result?.id})`,
      );

      return {
        success: true,
        eventId,
        messageId: result?.id,
        userId,
        email,
        provider: 'sendpulse',
      };
    } catch (error) {
      this.logger.error(
        `Failed to send verification email to ${email} via SendPulse: ${error.message}`,
        error.stack,
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
    verificationLink: string;
    expiresAt: string;
  }): string {
    // Charger le template depuis le cache ou le fichier
    let mjmlTemplate = this.loadTemplate();

    // Remplacer les variables
    mjmlTemplate = mjmlTemplate
      .replace(/\{\{username\}\}/g, variables.username)
      .replace(/\{\{verificationLink\}\}/g, variables.verificationLink)
      .replace(/\{\{expiresAt\}\}/g, variables.expiresAt);

    // Compiler MJML en HTML
    const { html, errors } = mjml(mjmlTemplate, {
      validationLevel: 'soft',
    });

    if (errors && errors.length > 0) {
      this.logger.warn(
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
      'email-verification.mjml',
    );

    try {
      this.templateCache = fs.readFileSync(templatePath, 'utf8');
      this.logger.log(`Loaded email verification template from ${templatePath}`);
      return this.templateCache;
    } catch (error) {
      this.logger.error(
        `Failed to load template from ${templatePath}: ${error.message}`,
      );
      throw new Error(`Email template not found: ${templatePath}`);
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
