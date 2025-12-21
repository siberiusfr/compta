import { Injectable } from '@nestjs/common';
import { PrismaService } from '../database/prisma.service';
import {
  NotificationChannel,
  NotificationType,
  Prisma,
} from '@prisma/client';

export interface DailyStatsFilters {
  startDate?: Date;
  endDate?: Date;
  channel?: NotificationChannel;
  type?: NotificationType;
}

@Injectable()
export class NotificationStatsService {
  constructor(private prisma: PrismaService) {}

  // Créer ou mettre à jour les statistiques d'une journée
  async upsertDailyStats(
    date: Date,
    channel: NotificationChannel,
    type: NotificationType | null,
    data: {
      totalSent?: number;
      totalDelivered?: number;
      totalFailed?: number;
      totalBounced?: number;
      avgProcessingTime?: number;
    },
  ) {
    const dateOnly = new Date(date.toISOString().split('T')[0]);

    return this.prisma.notificationStats.upsert({
      where: {
        date_channel_type: {
          date: dateOnly,
          channel,
          type: type as NotificationType,
        },
      },
      create: {
        date: dateOnly,
        channel,
        type,
        totalSent: data.totalSent || 0,
        totalDelivered: data.totalDelivered || 0,
        totalFailed: data.totalFailed || 0,
        totalBounced: data.totalBounced || 0,
        avgProcessingTime: data.avgProcessingTime,
      },
      update: {
        totalSent: data.totalSent ? { increment: data.totalSent } : undefined,
        totalDelivered: data.totalDelivered
          ? { increment: data.totalDelivered }
          : undefined,
        totalFailed: data.totalFailed
          ? { increment: data.totalFailed }
          : undefined,
        totalBounced: data.totalBounced
          ? { increment: data.totalBounced }
          : undefined,
        avgProcessingTime: data.avgProcessingTime,
      },
    });
  }

  // Récupérer les statistiques quotidiennes
  async getDailyStats(filters: DailyStatsFilters) {
    const where: Prisma.NotificationStatsWhereInput = {};

    if (filters.startDate || filters.endDate) {
      where.date = {};
      if (filters.startDate) where.date.gte = filters.startDate;
      if (filters.endDate) where.date.lte = filters.endDate;
    }

    if (filters.channel) where.channel = filters.channel;
    if (filters.type) where.type = filters.type;

    return this.prisma.notificationStats.findMany({
      where,
      orderBy: { date: 'desc' },
    });
  }

  // Agréger les statistiques depuis les notifications réelles
  async aggregateStats(
    startDate: Date,
    endDate: Date,
    channel?: NotificationChannel,
    type?: NotificationType,
  ) {
    const where: Prisma.NotificationWhereInput = {
      createdAt: {
        gte: startDate,
        lte: endDate,
      },
    };

    if (channel) where.channel = channel;
    if (type) where.type = type;

    const [totalSent, totalDelivered, totalFailed, totalBounced] =
      await Promise.all([
        this.prisma.notification.count({
          where: { ...where, status: 'SENT' },
        }),
        this.prisma.notification.count({
          where: { ...where, status: 'DELIVERED' },
        }),
        this.prisma.notification.count({
          where: { ...where, status: 'FAILED' },
        }),
        this.prisma.notification.count({
          where: { ...where, status: 'BOUNCED' },
        }),
      ]);

    // Calculer le temps de traitement moyen
    const processedNotifications = await this.prisma.notification.findMany({
      where: {
        ...where,
        processingAt: { not: null },
        sentAt: { not: null },
      },
      select: {
        processingAt: true,
        sentAt: true,
      },
    });

    let avgProcessingTime: number | undefined;
    if (processedNotifications.length > 0) {
      const totalProcessingTime = processedNotifications.reduce(
        (sum, notification) => {
          const processingTime =
            notification.sentAt!.getTime() -
            notification.processingAt!.getTime();
          return sum + processingTime;
        },
        0,
      );
      avgProcessingTime = Math.round(
        totalProcessingTime / processedNotifications.length,
      );
    }

    return {
      totalSent,
      totalDelivered,
      totalFailed,
      totalBounced,
      avgProcessingTime,
    };
  }

  // Récupérer un résumé global
  async getGlobalSummary(filters?: {
    startDate?: Date;
    endDate?: Date;
    channel?: NotificationChannel;
  }) {
    const where: Prisma.NotificationStatsWhereInput = {};

    if (filters?.startDate || filters?.endDate) {
      where.date = {};
      if (filters.startDate) where.date.gte = filters.startDate;
      if (filters.endDate) where.date.lte = filters.endDate;
    }

    if (filters?.channel) where.channel = filters.channel;

    const stats = await this.prisma.notificationStats.findMany({
      where,
    });

    const summary = {
      totalSent: 0,
      totalDelivered: 0,
      totalFailed: 0,
      totalBounced: 0,
      avgProcessingTime: 0,
      byChannel: {} as Record<string, any>,
      byType: {} as Record<string, any>,
    };

    let totalProcessingTimeSum = 0;
    let totalProcessingTimeCount = 0;

    stats.forEach((stat) => {
      summary.totalSent += stat.totalSent;
      summary.totalDelivered += stat.totalDelivered;
      summary.totalFailed += stat.totalFailed;
      summary.totalBounced += stat.totalBounced;

      if (stat.avgProcessingTime) {
        totalProcessingTimeSum += stat.avgProcessingTime;
        totalProcessingTimeCount++;
      }

      // Agrégation par canal
      if (!summary.byChannel[stat.channel]) {
        summary.byChannel[stat.channel] = {
          totalSent: 0,
          totalDelivered: 0,
          totalFailed: 0,
          totalBounced: 0,
        };
      }
      summary.byChannel[stat.channel].totalSent += stat.totalSent;
      summary.byChannel[stat.channel].totalDelivered += stat.totalDelivered;
      summary.byChannel[stat.channel].totalFailed += stat.totalFailed;
      summary.byChannel[stat.channel].totalBounced += stat.totalBounced;

      // Agrégation par type
      if (stat.type) {
        if (!summary.byType[stat.type]) {
          summary.byType[stat.type] = {
            totalSent: 0,
            totalDelivered: 0,
            totalFailed: 0,
            totalBounced: 0,
          };
        }
        summary.byType[stat.type].totalSent += stat.totalSent;
        summary.byType[stat.type].totalDelivered += stat.totalDelivered;
        summary.byType[stat.type].totalFailed += stat.totalFailed;
        summary.byType[stat.type].totalBounced += stat.totalBounced;
      }
    });

    if (totalProcessingTimeCount > 0) {
      summary.avgProcessingTime = Math.round(
        totalProcessingTimeSum / totalProcessingTimeCount,
      );
    }

    return summary;
  }

  // Calculer le taux de succès
  async getSuccessRate(
    startDate: Date,
    endDate: Date,
    channel?: NotificationChannel,
  ) {
    const where: Prisma.NotificationStatsWhereInput = {
      date: {
        gte: startDate,
        lte: endDate,
      },
    };

    if (channel) where.channel = channel;

    const stats = await this.prisma.notificationStats.findMany({
      where,
    });

    const totalSent = stats.reduce((sum, stat) => sum + stat.totalSent, 0);
    const totalDelivered = stats.reduce(
      (sum, stat) => sum + stat.totalDelivered,
      0,
    );
    const totalFailed = stats.reduce((sum, stat) => sum + stat.totalFailed, 0);

    const successRate =
      totalSent > 0 ? ((totalDelivered / totalSent) * 100).toFixed(2) : '0';
    const failureRate =
      totalSent > 0 ? ((totalFailed / totalSent) * 100).toFixed(2) : '0';

    return {
      totalSent,
      totalDelivered,
      totalFailed,
      successRate: parseFloat(successRate),
      failureRate: parseFloat(failureRate),
    };
  }
}
