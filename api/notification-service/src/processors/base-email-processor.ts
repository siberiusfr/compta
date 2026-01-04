import { WorkerHost } from '@nestjs/bullmq';
import { Logger } from '@nestjs/common';
import mjml from 'mjml';
import * as fs from 'fs';
import * as path from 'path';

/**
 * Base processor class for email processors.
 * Provides shared functionality for template loading, compilation, and date formatting.
 *
 * This class eliminates code duplication between EmailVerificationProcessor and PasswordResetProcessor.
 */
export abstract class BaseEmailProcessor extends WorkerHost {
  protected readonly logger: Logger;
  protected templateCache: Map<string, string> = new Map();

  constructor() {
    super();
    this.logger = new Logger(this.constructor.name);
  }

  /**
   * Get the template filename for this processor.
   * Each processor must implement this method to specify its template.
   */
  protected abstract getTemplateFilename(): string;

  /**
   * Get the email subject for this processor.
   * Each processor must implement this method to specify its subject.
   */
  protected abstract getEmailSubject(): string;

  /**
   * Compile the MJML template with the provided variables.
   *
   * @param variables The variables to inject into the template
   * @returns The compiled HTML
   */
  protected compileTemplate(variables: Record<string, string>): string {
    // Load the template from cache or file
    const mjmlTemplate = this.loadTemplate();

    // Replace variables
    let compiledTemplate = mjmlTemplate;
    for (const [key, value] of Object.entries(variables)) {
      compiledTemplate = compiledTemplate.replace(
        new RegExp(`\\{\\{${key}\\}\\}`, 'g'),
        value,
      );
    }

    // Compile MJML to HTML
    const { html, errors } = mjml(compiledTemplate, {
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
   * Load the MJML template from file.
   * Uses in-memory cache to avoid repeated file reads.
   *
   * @returns The MJML template content
   */
  protected loadTemplate(): string {
    const templateFilename = this.getTemplateFilename();

    // Check cache first
    if (this.templateCache.has(templateFilename)) {
      return this.templateCache.get(templateFilename)!;
    }

    const templatePath = path.resolve(
      __dirname,
      '..',
      'templates',
      templateFilename,
    );

    try {
      const templateContent = fs.readFileSync(templatePath, 'utf8');
      this.templateCache.set(templateFilename, templateContent);
      this.logger.log(`Loaded email template from ${templatePath}`);
      return templateContent;
    } catch (error: any) {
      this.logger.error(
        `Failed to load template from ${templatePath}: ${error.message}`,
      );
      throw new Error(`Email template not found: ${templatePath}`);
    }
  }

  /**
   * Format the expiration date for display in the email.
   * Uses configurable locale and timezone from environment variables.
   *
   * @param expiresAt The expiration date in ISO 8601 format
   * @returns The formatted date string
   */
  protected formatExpirationDate(expiresAt: string): string {
    try {
      const date = new Date(expiresAt);

      // Get locale from environment variable or default to 'fr-FR'
      const locale = process.env.EMAIL_LOCALE || 'fr-FR';

      // Get timezone from environment variable or default to 'Africa/Tunis'
      const timeZone = process.env.EMAIL_TIMEZONE || 'Africa/Tunis';

      return date.toLocaleString(locale, {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        timeZone,
      });
    } catch {
      return expiresAt; // Fallback to original format if parsing fails
    }
  }
}
