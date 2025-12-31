import { Module } from '@nestjs/common';
import { APP_FILTER } from '@nestjs/core';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { BullModule } from '@nestjs/bullmq';
import { MailerModule } from '@nestjs-modules/mailer';
import { NotificationService } from './notification/notification.service';
import { MailProcessor } from './notification/mail.processor';
import { EmailVerificationProcessor } from './processors/email-verification.processor';
import { PasswordResetProcessor } from './processors/password-reset.processor';
import { BullBoardModule } from '@bull-board/nestjs';
import { ExpressAdapter } from '@bull-board/express';
import { BullMQAdapter } from '@bull-board/api/bullMQAdapter';
import { QueueNames } from '@compta/notification-contracts';

// Prisma
import { PrismaModule } from './database/prisma.module';

// Services
import { NotificationsService } from './services/notifications.service';
import { NotificationTemplatesService } from './services/notification-templates.service';
import { NotificationStatsService } from './services/notification-stats.service';
import { UsersService } from './services/users.service';
import { RedisHealthService } from './health/redis-health.service';

// Controllers
import { NotificationsController } from './controllers/notifications.controller';
import { StatsController } from './controllers/stats.controller';
import { TemplatesController } from './controllers/templates.controller';
import { UsersController } from './controllers/users.controller';
import { HealthController } from './controllers/health.controller';

// Filters
import { AllExceptionsFilter } from './filters/all-exceptions.filter';

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
    // Enregistrement de la file d'attente spécifique aux emails (legacy)
    BullModule.registerQueue({
      name: 'mail_queue',
    }),
    // Queue pour la verification d'email (oauth2-server -> notification-service)
    // @see QueueNames.EMAIL_VERIFICATION from @compta/notification-contracts
    BullModule.registerQueue({
      name: QueueNames.EMAIL_VERIFICATION,
    }),
    // Queue pour le reset de mot de passe (oauth2-server -> notification-service)
    // @see QueueNames.PASSWORD_RESET from @compta/notification-contracts
    BullModule.registerQueue({
      name: QueueNames.PASSWORD_RESET,
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
    BullBoardModule.forFeature({
      name: QueueNames.EMAIL_VERIFICATION,
      adapter: BullMQAdapter,
    }),
    BullBoardModule.forFeature({
      name: QueueNames.PASSWORD_RESET,
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
    HealthController,
  ],
  providers: [
    AppService,
    NotificationService,
    MailProcessor,
    EmailVerificationProcessor,
    PasswordResetProcessor,
    NotificationsService,
    NotificationTemplatesService,
    NotificationStatsService,
    UsersService,
    RedisHealthService,
    {
      provide: APP_FILTER,
      useClass: AllExceptionsFilter,
    },
  ],
})
export class AppModule {}
