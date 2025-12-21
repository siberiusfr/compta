import { Controller, Get, Post, Query, Logger, Optional } from '@nestjs/common';
import { AppService } from './app.service';
import { InjectQueue } from '@nestjs/bullmq';
import { Queue } from 'bullmq';
import { NotificationsService } from './services/notifications.service';
import { UsersService } from './services/users.service';
import { NotificationService } from './notification/notification.service';
import { RedisHealthService } from './health/redis-health.service';
import {
  NotificationChannel,
  NotificationType,
  NotificationPriority,
  NotificationStatus,
} from '@prisma/client';

@Controller()
export class AppController {
  private readonly logger = new Logger(AppController.name);

  constructor(
    private readonly appService: AppService,
    @Optional() @InjectQueue('mail_queue') private mailQueue: Queue | null,
    private readonly notificationsService: NotificationsService,
    private readonly usersService: UsersService,
    private readonly notificationService: NotificationService,
    private readonly redisHealth: RedisHealthService,
  ) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }

  @Post('send-test-email')
  async sendTestEmail(
    @Query('email') email: string,
    @Query('name') name: string,
  ) {
    const recipientEmail = email || 'test@example.com';
    const recipientName = name || 'Test User';

    // Trouver ou créer l'utilisateur
    const user = await this.usersService.findOrCreate(recipientEmail);

    // Créer la notification dans la DB
    const notification = await this.notificationsService.create({
      userId: user.id,
      type: NotificationType.WELCOME_EMAIL,
      channel: NotificationChannel.EMAIL,
      priority: NotificationPriority.NORMAL,
      recipient: recipientEmail,
      subject: 'Bienvenue chez nous !',
      templateId: 'welcome',
      payload: { name: recipientName },
    });

    const redisAvailable = this.redisHealth.isRedisAvailable();

    if (redisAvailable && this.mailQueue) {
      // Mode queue: Ajouter le job à BullMQ
      try {
        const job = await this.mailQueue.add('send_welcome', {
          notificationId: notification.id,
          to: recipientEmail,
          name: recipientName,
        });

        await this.notificationsService.updateStatus(notification.id, {
          status: NotificationStatus.QUEUED,
          jobId: job.id,
        });

        this.logger.log(`✅ Email job queued: ${job.id}`);

        return {
          mode: 'queued',
          message: 'Email job added to queue (async processing)',
          notificationId: notification.id,
          jobId: job.id,
          email: recipientEmail,
          name: recipientName,
        };
      } catch (error) {
        this.logger.warn(`⚠️  Failed to queue email, falling back to direct send: ${error.message}`);
        // Fall through to direct send
      }
    }

    // Mode fallback: Envoi direct sans queue
    this.logger.log(`📧 Sending email directly (Redis unavailable)`);

    try {
      await this.notificationsService.updateStatus(notification.id, {
        status: NotificationStatus.PROCESSING,
      });

      const result = await this.notificationService.sendWelcomeEmail(
        recipientEmail,
        recipientName,
      );

      await this.notificationsService.updateStatus(notification.id, {
        status: NotificationStatus.SENT,
        externalId: result?.messageId,
      });

      this.logger.log(`✅ Email sent directly: ${result?.messageId}`);

      return {
        mode: 'direct',
        message: 'Email sent directly (Redis unavailable - synchronous processing)',
        notificationId: notification.id,
        messageId: result?.messageId,
        email: recipientEmail,
        name: recipientName,
        warning: 'Redis/BullMQ not available. Email was sent synchronously instead of being queued.',
      };
    } catch (error) {
      await this.notificationsService.updateStatus(notification.id, {
        status: NotificationStatus.FAILED,
        errorMessage: error.message,
        errorStack: error.stack,
      });

      this.logger.error(`❌ Failed to send email: ${error.message}`);

      throw error;
    }
  }
}
