import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { BullModule } from '@nestjs/bullmq';
import { MailerModule } from '@nestjs-modules/mailer';
import { NotificationService } from './notification/notification.service';
import { MailProcessor } from './notification/mail.processor';
import { BullBoardModule } from '@bull-board/nestjs';
import { ExpressAdapter } from '@bull-board/express';
import { BullMQAdapter } from '@bull-board/api/bullMQAdapter';

// Prisma
import { PrismaModule } from './database/prisma.module';

// Services
import { NotificationsService } from './services/notifications.service';
import { NotificationTemplatesService } from './services/notification-templates.service';
import { NotificationStatsService } from './services/notification-stats.service';
import { UsersService } from './services/users.service';

// Controllers
import { NotificationsController } from './controllers/notifications.controller';
import { StatsController } from './controllers/stats.controller';
import { TemplatesController } from './controllers/templates.controller';
import { UsersController } from './controllers/users.controller';

@Module({
  imports: [
    // Prisma Database
    PrismaModule,

    // Connexion globale à Redis
    BullModule.forRoot({
      connection: {
        host: 'localhost',
        port: 6379,
      },
    }),
    // Enregistrement de la file d'attente spécifique aux emails
    BullModule.registerQueue({
      name: 'mail_queue',
    }),
    // Configuration de BullBoard pour surveiller les queues
    BullBoardModule.forRoot({
      route: '/queues',
      adapter: ExpressAdapter,
    }),
    BullBoardModule.forFeature({
      name: 'mail_queue',
      adapter: BullMQAdapter,
    }),
    MailerModule.forRoot({
      transport: {
        host: 'smtp.example.com', // Remplace par ton host
        port: 587,
        auth: {
          user: 'user@example.com',
          pass: 'password',
        },
      },
      defaults: {
        from: '"No Reply" <noreply@example.com>',
      },
    }),
  ],
  controllers: [
    AppController,
    NotificationsController,
    StatsController,
    TemplatesController,
    UsersController,
  ],
  providers: [
    AppService,
    NotificationService,
    MailProcessor,
    NotificationsService,
    NotificationTemplatesService,
    NotificationStatsService,
    UsersService,
  ],
})
export class AppModule {}
