import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Param,
  Query,
  Body,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import {
  NotificationsService,
  type CreateNotificationDto,
  type UpdateNotificationStatusDto,
  type NotificationFilters,
} from '../services/notifications.service';
import {
  NotificationStatus,
  NotificationChannel,
  NotificationType,
  NotificationPriority,
} from '@prisma/client';

@Controller('notifications')
export class NotificationsController {
  constructor(private readonly notificationsService: NotificationsService) {}

  // Créer une nouvelle notification
  @Post()
  @HttpCode(HttpStatus.CREATED)
  async create(@Body() createDto: CreateNotificationDto) {
    return this.notificationsService.create(createDto);
  }

  // Lister les notifications avec filtres
  @Get()
  async findAll(
    @Query('userId') userId?: string,
    @Query('status') status?: NotificationStatus,
    @Query('channel') channel?: NotificationChannel,
    @Query('type') type?: NotificationType,
    @Query('startDate') startDate?: string,
    @Query('endDate') endDate?: string,
    @Query('page') page?: string,
    @Query('limit') limit?: string,
  ) {
    const filters: NotificationFilters = {};

    if (userId) filters.userId = userId;
    if (status) filters.status = status;
    if (channel) filters.channel = channel;
    if (type) filters.type = type;
    if (startDate) filters.startDate = new Date(startDate);
    if (endDate) filters.endDate = new Date(endDate);
    if (page) filters.page = parseInt(page, 10);
    if (limit) filters.limit = parseInt(limit, 10);

    return this.notificationsService.findAll(filters);
  }

  // Récupérer une notification par ID
  @Get(':id')
  async findOne(@Param('id') id: string) {
    return this.notificationsService.findById(id);
  }

  // Mettre à jour le statut d'une notification
  @Patch(':id/status')
  async updateStatus(
    @Param('id') id: string,
    @Body() updateDto: UpdateNotificationStatusDto,
  ) {
    return this.notificationsService.updateStatus(id, updateDto);
  }

  // Récupérer les statistiques globales
  @Get('stats/global')
  async getGlobalStats(@Query('userId') userId?: string) {
    return this.notificationsService.getStats(userId);
  }

  // Récupérer les notifications programmées prêtes
  @Get('scheduled/ready')
  async getScheduledReady() {
    return this.notificationsService.findScheduledReady();
  }

  // Récupérer les notifications qui peuvent être réessayées
  @Get('failed/retryable')
  async getRetryable() {
    return this.notificationsService.findRetryable();
  }

  // Nettoyer les anciennes notifications
  @Delete('cleanup/:days')
  @HttpCode(HttpStatus.NO_CONTENT)
  async cleanup(@Param('days') days: string) {
    const daysNumber = parseInt(days, 10);
    return this.notificationsService.deleteOlderThan(daysNumber);
  }
}
