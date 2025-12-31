import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { Logger } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import * as mjml from 'mjml';
import * as fs from 'fs';
import * as path from 'path';

/**
 * Payload du message de demande de reset de mot de passe.
 * Correspond au format envoye par oauth2-server.
 */
export interface PasswordResetPayload {
  /** Identifiant unique de l'utilisateur (UUID) */
  userId: string;

  /** Adresse email de l'utilisateur */
  email: string;

  /** Nom d'utilisateur pour personnaliser l'email */
  username: string;

  /** Token de reset unique */
  token: string;

  /** Lien complet de reset a inclure dans l'email */
  resetLink: string;

  /** Date et heure d'expiration du token (ISO 8601) */
  expiresAt: string;
}

/**
 * Processor BullMQ pour les demandes de reset de mot de passe.
 *
 * Consomme les messages de la queue 'password-reset' publies par oauth2-server
 * et envoie les emails de reset via SMTP.
 */
@Processor('password-reset')
export class PasswordResetProcessor extends WorkerHost {
  private readonly logger = new Logger(PasswordResetProcessor.name);
  private templateCache: string | null = null;

  constructor(private readonly mailerService: MailerService) {
    super();
  }

  /**
   * Traite un job de demande de reset de mot de passe.
   *
   * @param job Le job BullMQ contenant les donnees de reset
   * @returns Le resultat de l'envoi d'email
   */
  async process(job: Job<PasswordResetPayload, any, string>): Promise<any> {
    const { email, username, resetLink, expiresAt, userId } = job.data;

    this.logger.log(
      `Processing password reset job ${job.id} for user ${username} (${email})`,
    );

    try {
      // Charger et compiler le template MJML
      const html = this.compileTemplate({
        username,
        resetLink,
        expiresAt: this.formatExpirationDate(expiresAt),
      });

      // Envoyer l'email
      const result = await this.mailerService.sendMail({
        to: email,
        subject: 'Reinitialisation de votre mot de passe - COMPTA',
        html,
      });

      this.logger.log(
        `Successfully sent password reset email to ${email} (job ${job.id}, messageId: ${result?.messageId})`,
      );

      return {
        success: true,
        messageId: result?.messageId,
        userId,
        email,
      };
    } catch (error) {
      this.logger.error(
        `Failed to send password reset email to ${email}: ${error.message}`,
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
      this.logger.log(`Loaded password reset template from ${templatePath}`);
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
