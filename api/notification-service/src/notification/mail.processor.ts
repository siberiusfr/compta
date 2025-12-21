import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { NotificationService } from './notification.service';

@Processor('mail_queue')
export class MailProcessor extends WorkerHost {
  constructor(private readonly notificationService: NotificationService) {
    super();
  }

  async process(job: Job<any, any, string>): Promise<any> {
    const { to, name } = job.data;

    if (job.name === 'send_welcome') {
      this.notificationService.sendWelcomeEmail(to, name);
    }
  }
}
