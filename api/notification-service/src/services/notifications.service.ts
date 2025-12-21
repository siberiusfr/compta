import { Injectable } from '@nestjs/common';
import { PrismaService } from '../database/prisma.service';
import {
  NotificationStatus,
  NotificationChannel,
  NotificationType,
  NotificationPriority,
  Prisma,
} from '@prisma/client';

export interface CreateNotificationDto {
  userId: string;
  type: NotificationType;
  channel: NotificationChannel;
  priority?: NotificationPriority;
  recipient: string;
  subject?: string;
  templateId?: string;
  payload: Record<string, any>;
  metadata?: Record<string, any>;
  scheduledFor?: Date;
  maxAttempts?: number;
}

export interface UpdateNotificationStatusDto {
  status: NotificationStatus;
  jobId?: string;
  externalId?: string;
  errorCode?: string;
  errorMessage?: string;
  errorStack?: string;
}

export interface NotificationFilters {
  userId?: string;
  status?: NotificationStatus;
  channel?: NotificationChannel;
  type?: NotificationType;
  startDate?: Date;
  endDate?: Date;
  page?: number;
  limit?: number;
}

@Injectable()
export class NotificationsService {
  constructor(private prisma: PrismaService) {}

  // Créer une nouvelle notification
  async create(data: CreateNotificationDto) {
    return this.prisma.notification.create({
      data: {
        userId: data.userId,
        type: data.type,
        channel: data.channel,
        priority: data.priority || NotificationPriority.NORMAL,
        recipient: data.recipient,
        subject: data.subject,
        templateId: data.templateId,
        payload: data.payload,
        metadata: data.metadata,
        scheduledFor: data.scheduledFor,
        maxAttempts: data.maxAttempts || 3,
      },
      include: {
        user: true,
      },
    });
  }

  // Mettre à jour le status d'une notification
  async updateStatus(id: string, data: UpdateNotificationStatusDto) {
    const updateData: Prisma.NotificationUpdateInput = {
      status: data.status,
    };

    const now = new Date();

    // Mettre à jour les timestamps selon le status
    switch (data.status) {
      case NotificationStatus.QUEUED:
        updateData.queuedAt = now;
        updateData.jobId = data.jobId;
        break;
      case NotificationStatus.PROCESSING:
        updateData.processingAt = now;
        updateData.attemptCount = { increment: 1 };
        updateData.lastAttemptAt = now;
        break;
      case NotificationStatus.SENT:
        updateData.sentAt = now;
        updateData.externalId = data.externalId;
        break;
      case NotificationStatus.DELIVERED:
        updateData.deliveredAt = now;
        break;
      case NotificationStatus.FAILED:
      case NotificationStatus.BOUNCED:
        updateData.failedAt = now;
        updateData.errorCode = data.errorCode;
        updateData.errorMessage = data.errorMessage;
        updateData.errorStack = data.errorStack;
        break;
    }

    return this.prisma.notification.update({
      where: { id },
      data: updateData,
      include: {
        user: true,
      },
    });
  }

  // Récupérer une notification par ID
  async findById(id: string) {
    return this.prisma.notification.findUnique({
      where: { id },
      include: {
        user: true,
      },
    });
  }

  // Récupérer une notification par jobId (BullMQ)
  async findByJobId(jobId: string) {
    return this.prisma.notification.findFirst({
      where: { jobId },
      include: {
        user: true,
      },
    });
  }

  // Lister les notifications avec filtres et pagination
  async findAll(filters: NotificationFilters) {
    const {
      userId,
      status,
      channel,
      type,
      startDate,
      endDate,
      page = 1,
      limit = 50,
    } = filters;

    const where: Prisma.NotificationWhereInput = {};

    if (userId) where.userId = userId;
    if (status) where.status = status;
    if (channel) where.channel = channel;
    if (type) where.type = type;

    if (startDate || endDate) {
      where.createdAt = {};
      if (startDate) where.createdAt.gte = startDate;
      if (endDate) where.createdAt.lte = endDate;
    }

    const skip = (page - 1) * limit;

    const [notifications, total] = await Promise.all([
      this.prisma.notification.findMany({
        where,
        include: {
          user: {
            select: {
              id: true,
              email: true,
            },
          },
        },
        orderBy: {
          createdAt: 'desc',
        },
        skip,
        take: limit,
      }),
      this.prisma.notification.count({ where }),
    ]);

    return {
      data: notifications,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit),
      },
    };
  }

  // Récupérer les notifications échouées qui peuvent être réessayées
  async findRetryable() {
    return this.prisma.notification.findMany({
      where: {
        status: NotificationStatus.FAILED,
        attemptCount: {
          lt: this.prisma.notification.fields.maxAttempts,
        },
        OR: [
          { nextRetryAt: null },
          { nextRetryAt: { lte: new Date() } },
        ],
      },
      include: {
        user: true,
      },
      take: 100, // Limiter pour ne pas surcharger
    });
  }

  // Récupérer les notifications programmées prêtes à être envoyées
  async findScheduledReady() {
    return this.prisma.notification.findMany({
      where: {
        status: NotificationStatus.PENDING,
        scheduledFor: {
          lte: new Date(),
        },
      },
      include: {
        user: true,
      },
      take: 100,
    });
  }

  // Statistiques générales
  async getStats(userId?: string) {
    const where: Prisma.NotificationWhereInput = userId ? { userId } : {};

    const [total, byStatus, byChannel, byType] = await Promise.all([
      this.prisma.notification.count({ where }),
      this.prisma.notification.groupBy({
        by: ['status'],
        where,
        _count: true,
      }),
      this.prisma.notification.groupBy({
        by: ['channel'],
        where,
        _count: true,
      }),
      this.prisma.notification.groupBy({
        by: ['type'],
        where,
        _count: true,
      }),
    ]);

    return {
      total,
      byStatus: byStatus.reduce((acc, item) => {
        acc[item.status] = item._count;
        return acc;
      }, {} as Record<string, number>),
      byChannel: byChannel.reduce((acc, item) => {
        acc[item.channel] = item._count;
        return acc;
      }, {} as Record<string, number>),
      byType: byType.reduce((acc, item) => {
        acc[item.type] = item._count;
        return acc;
      }, {} as Record<string, number>),
    };
  }

  // Supprimer les anciennes notifications (cleanup)
  async deleteOlderThan(days: number) {
    const date = new Date();
    date.setDate(date.getDate() - days);

    return this.prisma.notification.deleteMany({
      where: {
        createdAt: {
          lt: date,
        },
        status: {
          in: [
            NotificationStatus.SENT,
            NotificationStatus.DELIVERED,
            NotificationStatus.CANCELLED,
          ],
        },
      },
    });
  }
}
