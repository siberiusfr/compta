import { Controller, Get, Post, Query } from '@nestjs/common';
import { AppService } from './app.service';
import { InjectQueue } from '@nestjs/bullmq';
import { Queue } from 'bullmq';

@Controller()
export class AppController {
  constructor(
    private readonly appService: AppService,
    @InjectQueue('mail_queue') private mailQueue: Queue,
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
    await this.mailQueue.add('send_welcome', {
      to: email || 'test@example.com',
      name: name || 'Test User',
    });

    return {
      message: 'Email job added to queue',
      email: email || 'test@example.com',
      name: name || 'Test User',
    };
  }
}
