import { Controller, Get, Post, Query, Body, HttpCode, HttpStatus } from '@nestjs/common';
import {
  NotificationStatsService,
  DailyStatsFilters,
} from '../services/notification-stats.service';
import { NotificationChannel, NotificationType } from '@prisma/client';

@Controller('stats')
export class StatsController {
  constructor(
    private readonly notificationStatsService: NotificationStatsService,
  ) {}

  // Récupérer les statistiques quotidiennes
  @Get('daily')
  async getDailyStats(
    @Query('startDate') startDate?: string,
    @Query('endDate') endDate?: string,
    @Query('channel') channel?: NotificationChannel,
    @Query('type') type?: NotificationType,
  ) {
    const filters: DailyStatsFilters = {};

    if (startDate) filters.startDate = new Date(startDate);
    if (endDate) filters.endDate = new Date(endDate);
    if (channel) filters.channel = channel;
    if (type) filters.type = type;

    return this.notificationStatsService.getDailyStats(filters);
  }

  // Récupérer le résumé global
  @Get('summary')
  async getGlobalSummary(
    @Query('startDate') startDate?: string,
    @Query('endDate') endDate?: string,
    @Query('channel') channel?: NotificationChannel,
  ) {
    const filters: any = {};

    if (startDate) filters.startDate = new Date(startDate);
    if (endDate) filters.endDate = new Date(endDate);
    if (channel) filters.channel = channel;

    return this.notificationStatsService.getGlobalSummary(filters);
  }

  // Récupérer le taux de succès
  @Get('success-rate')
  async getSuccessRate(
    @Query('startDate') startDate: string,
    @Query('endDate') endDate: string,
    @Query('channel') channel?: NotificationChannel,
  ) {
    if (!startDate || !endDate) {
      throw new Error('startDate and endDate are required');
    }

    return this.notificationStatsService.getSuccessRate(
      new Date(startDate),
      new Date(endDate),
      channel,
    );
  }

  // Agréger les statistiques depuis les notifications
  @Post('aggregate')
  @HttpCode(HttpStatus.OK)
  async aggregateStats(
    @Body()
    body: {
      startDate: string;
      endDate: string;
      channel?: NotificationChannel;
      type?: NotificationType;
    },
  ) {
    return this.notificationStatsService.aggregateStats(
      new Date(body.startDate),
      new Date(body.endDate),
      body.channel,
      body.type,
    );
  }
}
