import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { Logger } from '@nestjs/common';
import { NotificationService } from './notification.service';
import { NotificationsService } from '../services/notifications.service';
import { NotificationStatus } from '@prisma/client';

@Processor('mail_queue')
export class MailProcessor extends WorkerHost {
  private readonly logger = new Logger(MailProcessor.name);

  constructor(
    private readonly notificationService: NotificationService,
    private readonly notificationsService: NotificationsService,
  ) {
    super();
  }

  async process(job: Job<any, any, string>): Promise<any> {
    const { notificationId, to, name } = job.data;

    if (!notificationId) {
      this.logger.warn(`Job ${job.id} has no notificationId, skipping DB tracking`);
      // Ancien comportement pour compatibilité
      if (job.name === 'send_welcome') {
        return this.notificationService.sendWelcomeEmail(to, name);
      }
      return;
    }

    try {
      // Mettre à jour le status à PROCESSING
      await this.notificationsService.updateStatus(notificationId, {
        status: NotificationStatus.PROCESSING,
      });

      this.logger.log(`Processing notification ${notificationId} for ${to}`);

      // Envoyer l'email selon le type
      let result;
      if (job.name === 'send_welcome') {
        result = await this.notificationService.sendWelcomeEmail(to, name);
      } else {
        throw new Error(`Unknown job type: ${job.name}`);
      }

      // Mettre à jour le status à SENT
      await this.notificationsService.updateStatus(notificationId, {
        status: NotificationStatus.SENT,
        externalId: result?.messageId || job.id,
      });

      this.logger.log(`Successfully sent notification ${notificationId}`);

      return result;
    } catch (error) {
      this.logger.error(
        `Failed to process notification ${notificationId}: ${error.message}`,
        error.stack,
      );

      // Mettre à jour le status à FAILED
      await this.notificationsService.updateStatus(notificationId, {
        status: NotificationStatus.FAILED,
        errorMessage: error.message,
        errorStack: error.stack,
        errorCode: error.code,
      });

      throw error; // Re-throw pour que BullMQ puisse gérer le retry
    }
  }
}
