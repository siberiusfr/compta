import { Controller, Get, Post, Query, Body, HttpCode, HttpStatus } from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiQuery,
  ApiBody,
} from '@nestjs/swagger';
import {
  NotificationStatsService,
  DailyStatsFilters,
} from '../services/notification-stats.service';
import { NotificationChannel, NotificationType } from '@prisma/client';

@ApiTags('stats')
@ApiBearerAuth('JWT')
@Controller('stats')
export class StatsController {
  constructor(
    private readonly notificationStatsService: NotificationStatsService,
  ) {}

  // Récupérer les statistiques quotidiennes
  @Get('daily')
  @ApiOperation({ summary: 'Get daily statistics' })
  @ApiQuery({ name: 'startDate', required: false, type: Date })
  @ApiQuery({ name: 'endDate', required: false, type: Date })
  @ApiQuery({ name: 'channel', required: false, enum: NotificationChannel })
  @ApiQuery({ name: 'type', required: false, enum: NotificationType })
  @ApiResponse({ status: 200, description: 'Daily statistics' })
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
  @ApiOperation({ summary: 'Get global summary' })
  @ApiQuery({ name: 'startDate', required: false, type: Date })
  @ApiQuery({ name: 'endDate', required: false, type: Date })
  @ApiQuery({ name: 'channel', required: false, enum: NotificationChannel })
  @ApiResponse({ status: 200, description: 'Global summary' })
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
  @ApiOperation({ summary: 'Get success rate' })
  @ApiQuery({ name: 'startDate', required: true, type: Date, description: 'Start date (required)' })
  @ApiQuery({ name: 'endDate', required: true, type: Date, description: 'End date (required)' })
  @ApiQuery({ name: 'channel', required: false, enum: NotificationChannel })
  @ApiResponse({ status: 200, description: 'Success rate metrics' })
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
  @ApiOperation({ summary: 'Aggregate statistics from notifications' })
  @ApiBody({ description: 'Aggregation parameters' })
  @ApiResponse({ status: 200, description: 'Aggregated statistics' })
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
