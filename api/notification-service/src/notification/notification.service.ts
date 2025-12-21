import { Injectable } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import mjml from 'mjml';
import * as fs from 'fs';
import * as path from 'path';

@Injectable()
export class NotificationService {
  constructor(private readonly mailerService: MailerService) {}

  async sendWelcomeEmail(to: string, name: string) {
    // 1. Charger le fichier MJML
    const templatePath = path.resolve(
      __dirname,
      '..',
      'templates',
      'welcome.mjml',
    );
    let mjmlTemplate = fs.readFileSync(templatePath, 'utf8');

    // 2. Remplacer les variables (Simple String Replace ou Handlebars)
    mjmlTemplate = mjmlTemplate.replace('{{name}}', name);

    // 3. Compiler en HTML
    const { html } = mjml(mjmlTemplate);

    // 4. Envoyer
    return await this.mailerService.sendMail({
      to,
      subject: 'Bienvenue chez nous !',
      html, // Le HTML généré par MJML
    });
  }
}
