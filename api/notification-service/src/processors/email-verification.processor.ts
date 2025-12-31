import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { Logger } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import * as mjml from 'mjml';
import * as fs from 'fs';
import * as path from 'path';
import {
  QueueNames,
  SendVerificationEmailJob,
  safeParseSendVerificationEmailJob,
} from '@compta/notification-contracts';

/**
 * Processor BullMQ pour les demandes de verification d'email.
 *
 * Consomme les messages de la queue 'email-verification' publies par oauth2-server
 * et envoie les emails de verification via SMTP.
 *
 * @see {@link SendVerificationEmailJob} pour le schema du payload (defini dans @compta/notification-contracts)
 */
@Processor(QueueNames.EMAIL_VERIFICATION)
export class EmailVerificationProcessor extends WorkerHost {
  private readonly logger = new Logger(EmailVerificationProcessor.name);
  private templateCache: string | null = null;

  constructor(private readonly mailerService: MailerService) {
    super();
  }

  /**
   * Traite un job de demande de verification d'email.
   *
   * @param job Le job BullMQ contenant les donnees de verification
   * @returns Le resultat de l'envoi d'email
   */
  async process(
    job: Job<SendVerificationEmailJob, any, string>,
  ): Promise<any> {
    // Valider le payload avec Zod
    const parseResult = safeParseSendVerificationEmailJob(job.data);
    if (!parseResult.success) {
      this.logger.error(
        `Invalid job payload for job ${job.id}: ${parseResult.error.message}`,
      );
      throw new Error(`Invalid job payload: ${parseResult.error.message}`);
    }

    const { email, username, verificationLink, expiresAt, userId } = parseResult.data;

    this.logger.log(
      `Processing email verification job ${job.id} for user ${username} (${email})`,
    );

    try {
      // Charger et compiler le template MJML
      const html = this.compileTemplate({
        username,
        verificationLink,
        expiresAt: this.formatExpirationDate(expiresAt),
      });

      // Envoyer l'email
      const result = await this.mailerService.sendMail({
        to: email,
        subject: 'Verification de votre adresse email - COMPTA',
        html,
      });

      this.logger.log(
        `Successfully sent verification email to ${email} (job ${job.id}, messageId: ${result?.messageId})`,
      );

      return {
        success: true,
        messageId: result?.messageId,
        userId,
        email,
      };
    } catch (error) {
      this.logger.error(
        `Failed to send verification email to ${email}: ${error.message}`,
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
