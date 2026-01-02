import { Injectable } from '@nestjs/common';
import { PrismaService } from '../database/prisma.service';
import { NotificationException } from '../common/exceptions/notification.exception';

export interface CreateUserDto {
  email: string;
  phone?: string;
  pushToken?: string;
  emailEnabled?: boolean;
  smsEnabled?: boolean;
  pushEnabled?: boolean;
  marketingEnabled?: boolean;
  transactionalEnabled?: boolean;
}

export interface UpdateUserDto {
  phone?: string;
  pushToken?: string;
  emailEnabled?: boolean;
  smsEnabled?: boolean;
  pushEnabled?: boolean;
  marketingEnabled?: boolean;
  transactionalEnabled?: boolean;
}

export interface UpdateUserPreferencesDto {
  emailEnabled?: boolean;
  smsEnabled?: boolean;
  pushEnabled?: boolean;
  marketingEnabled?: boolean;
  transactionalEnabled?: boolean;
}

@Injectable()
export class UsersService {
  constructor(private prisma: PrismaService) {}

  // Créer un utilisateur
  async create(data: CreateUserDto) {
    return this.prisma.user.create({
      data,
    });
  }

  // Récupérer un utilisateur par ID
  async findById(id: string) {
    const user = await this.prisma.user.findUnique({
      where: { id },
      include: {
        notifications: {
          take: 10,
          orderBy: {
            createdAt: 'desc',
          },
        },
      },
    });

    if (!user) {
      throw NotificationException.userNotFound(id);
    }

    return user;
  }

  // Récupérer un utilisateur par email
  async findByEmail(email: string) {
    const user = await this.prisma.user.findUnique({
      where: { email },
    });

    if (!user) {
      throw NotificationException.userEmailNotFound(email);
    }

    return user;
  }

  // Trouver ou créer un utilisateur (utile pour l'intégration)
  async findOrCreate(email: string, data?: Partial<CreateUserDto>) {
    let user = await this.prisma.user.findUnique({
      where: { email },
    });

    if (!user) {
      user = await this.prisma.user.create({
        data: {
          email,
          ...data,
        },
      });
    }

    return user;
  }

  // Lister tous les utilisateurs
  async findAll(filters?: { page?: number; limit?: number }) {
    const { page = 1, limit = 50 } = filters || {};
    const skip = (page - 1) * limit;

    const [users, total] = await Promise.all([
      this.prisma.user.findMany({
        orderBy: {
          createdAt: 'desc',
        },
        skip,
        take: limit,
        include: {
          _count: {
            select: { notifications: true },
          },
        },
      }),
      this.prisma.user.count(),
    ]);

    return {
      data: users,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit),
      },
    };
  }

  // Mettre à jour un utilisateur
  async update(id: string, data: UpdateUserDto) {
    await this.findById(id); // Vérifier qu'il existe

    return this.prisma.user.update({
      where: { id },
      data,
    });
  }

  // Mettre à jour les préférences de notification
  async updatePreferences(id: string, preferences: UpdateUserPreferencesDto) {
    await this.findById(id); // Vérifier qu'il existe

    return this.prisma.user.update({
      where: { id },
      data: preferences,
    });
  }

  // Supprimer un utilisateur
  async delete(id: string) {
    await this.findById(id); // Vérifier qu'il existe

    return this.prisma.user.delete({
      where: { id },
    });
  }

  // Récupérer les statistiques de notification d'un utilisateur
  async getUserNotificationStats(userId: string) {
    await this.findById(userId); // Vérifier qu'il existe

    const [total, byStatus, byChannel] = await Promise.all([
      this.prisma.notification.count({
        where: { userId },
      }),
      this.prisma.notification.groupBy({
        by: ['status'],
        where: { userId },
        _count: true,
      }),
      this.prisma.notification.groupBy({
        by: ['channel'],
        where: { userId },
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
    };
  }
}
