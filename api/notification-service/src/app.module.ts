import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { BullModule } from '@nestjs/bullmq';
import { MailerModule } from '@nestjs-modules/mailer';
import { NotificationService } from './notification/notification.service';

@Module({
  imports: [
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
  controllers: [AppController],
  providers: [AppService, NotificationService],
})
export class AppModule {}
