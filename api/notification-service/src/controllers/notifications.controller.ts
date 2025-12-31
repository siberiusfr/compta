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
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiQuery,
  ApiParam,
  ApiBody,
} from '@nestjs/swagger';
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

@ApiTags('notifications')
@ApiBearerAuth('JWT')
@Controller('notifications')
export class NotificationsController {
  constructor(private readonly notificationsService: NotificationsService) {}

  // Créer une nouvelle notification
  @Post()
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create a new notification' })
  @ApiResponse({ status: 201, description: 'Notification created successfully' })
  @ApiResponse({ status: 400, description: 'Bad request' })
  async create(@Body() createDto: CreateNotificationDto) {
    return this.notificationsService.create(createDto);
  }

  // Lister les notifications avec filtres
  @Get()
  @ApiOperation({ summary: 'List notifications with filters' })
  @ApiQuery({ name: 'userId', required: false, type: String })
  @ApiQuery({ name: 'status', required: false, enum: NotificationStatus })
  @ApiQuery({ name: 'channel', required: false, enum: NotificationChannel })
  @ApiQuery({ name: 'type', required: false, enum: NotificationType })
  @ApiQuery({ name: 'startDate', required: false, type: Date })
  @ApiQuery({ name: 'endDate', required: false, type: Date })
  @ApiQuery({ name: 'page', required: false, type: Number })
  @ApiQuery({ name: 'limit', required: false, type: Number })
  @ApiResponse({ status: 200, description: 'List of notifications' })
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
  @ApiOperation({ summary: 'Get notification by ID' })
  @ApiParam({ name: 'id', description: 'Notification ID' })
  @ApiResponse({ status: 200, description: 'Notification found' })
  @ApiResponse({ status: 404, description: 'Notification not found' })
  async findOne(@Param('id') id: string) {
    return this.notificationsService.findById(id);
  }

  // Mettre à jour le statut d'une notification
  @Patch(':id/status')
  @ApiOperation({ summary: 'Update notification status' })
  @ApiParam({ name: 'id', description: 'Notification ID' })
  @ApiResponse({ status: 200, description: 'Status updated' })
  async updateStatus(
    @Param('id') id: string,
    @Body() updateDto: UpdateNotificationStatusDto,
  ) {
    return this.notificationsService.updateStatus(id, updateDto);
  }

  // Récupérer les statistiques globales
  @Get('stats/global')
  @ApiOperation({ summary: 'Get global statistics' })
  @ApiQuery({ name: 'userId', required: false, type: String })
  @ApiResponse({ status: 200, description: 'Statistics data' })
  async getGlobalStats(@Query('userId') userId?: string) {
    return this.notificationsService.getStats(userId);
  }

  // Récupérer les notifications programmées prêtes
  @Get('scheduled/ready')
  @ApiOperation({ summary: 'Get scheduled notifications ready to send' })
  @ApiResponse({ status: 200, description: 'List of ready notifications' })
  async getScheduledReady() {
    return this.notificationsService.findScheduledReady();
  }

  // Récupérer les notifications qui peuvent être réessayées
  @Get('failed/retryable')
  @ApiOperation({ summary: 'Get retryable failed notifications' })
  @ApiResponse({ status: 200, description: 'List of retryable notifications' })
  async getRetryable() {
    return this.notificationsService.findRetryable();
  }

  // Nettoyer les anciennes notifications
  @Delete('cleanup/:days')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiOperation({ summary: 'Clean up old notifications' })
  @ApiParam({ name: 'days', description: 'Number of days to keep' })
  @ApiResponse({ status: 204, description: 'Cleanup completed' })
  async cleanup(@Param('days') days: string) {
    const daysNumber = parseInt(days, 10);
    return this.notificationsService.deleteOlderThan(daysNumber);
  }
}
