import { Controller, Get, Post, Query } from '@nestjs/common';
import { AppService } from './app.service';
import { InjectQueue } from '@nestjs/bullmq';
import { Queue } from 'bullmq';
import { NotificationsService } from './services/notifications.service';
import { UsersService } from './services/users.service';
import {
  NotificationChannel,
  NotificationType,
  NotificationPriority,
  NotificationStatus,
} from '@prisma/client';

@Controller()
export class AppController {
  constructor(
    private readonly appService: AppService,
    @InjectQueue('mail_queue') private mailQueue: Queue,
    private readonly notificationsService: NotificationsService,
    private readonly usersService: UsersService,
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

    // Ajouter le job à la queue avec l'ID de la notification
    const job = await this.mailQueue.add('send_welcome', {
      notificationId: notification.id,
      to: recipientEmail,
      name: recipientName,
    });

    // Mettre à jour la notification avec le jobId
    await this.notificationsService.updateStatus(notification.id, {
      status: NotificationStatus.QUEUED,
      jobId: job.id,
    });

    return {
      message: 'Email job added to queue',
      notificationId: notification.id,
      jobId: job.id,
      email: recipientEmail,
      name: recipientName,
    };
  }
}
