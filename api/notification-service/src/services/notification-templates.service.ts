import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../database/prisma.service';
import {
  NotificationChannel,
  NotificationType,
  Prisma,
} from '@prisma/client';

export interface CreateTemplateDto {
  code: string;
  name: string;
  channel: NotificationChannel;
  type: NotificationType;
  subject?: string;
  bodyTemplate: string;
  variables: Record<string, any>;
}

export interface UpdateTemplateDto {
  name?: string;
  subject?: string;
  bodyTemplate?: string;
  variables?: Record<string, any>;
  isActive?: boolean;
}

@Injectable()
export class NotificationTemplatesService {
  constructor(private prisma: PrismaService) {}

  // Créer un nouveau template
  async create(data: CreateTemplateDto) {
    // Vérifier si le code existe déjà
    const existing = await this.prisma.notificationTemplate.findUnique({
      where: { code: data.code },
    });

    let version = 1;
    if (existing) {
      // Désactiver l'ancienne version
      await this.prisma.notificationTemplate.update({
        where: { code: data.code },
        data: { isActive: false },
      });
      version = existing.version + 1;
    }

    return this.prisma.notificationTemplate.create({
      data: {
        code: data.code,
        name: data.name,
        channel: data.channel,
        type: data.type,
        subject: data.subject,
        bodyTemplate: data.bodyTemplate,
        variables: data.variables,
        version,
      },
    });
  }

  // Récupérer un template actif par code
  async findByCode(code: string) {
    const template = await this.prisma.notificationTemplate.findFirst({
      where: {
        code,
        isActive: true,
      },
    });

    if (!template) {
      throw new NotFoundException(`Template with code ${code} not found`);
    }

    return template;
  }

  // Récupérer un template par ID
  async findById(id: string) {
    const template = await this.prisma.notificationTemplate.findUnique({
      where: { id },
    });

    if (!template) {
      throw new NotFoundException(`Template with id ${id} not found`);
    }

    return template;
  }

  // Lister tous les templates
  async findAll(filters?: {
    channel?: NotificationChannel;
    type?: NotificationType;
    isActive?: boolean;
  }) {
    const where: Prisma.NotificationTemplateWhereInput = {};

    if (filters?.channel) where.channel = filters.channel;
    if (filters?.type) where.type = filters.type;
    if (filters?.isActive !== undefined) where.isActive = filters.isActive;

    return this.prisma.notificationTemplate.findMany({
      where,
      orderBy: [{ code: 'asc' }, { version: 'desc' }],
    });
  }

  // Récupérer toutes les versions d'un template
  async findVersions(code: string) {
    return this.prisma.notificationTemplate.findMany({
      where: { code },
      orderBy: { version: 'desc' },
    });
  }

  // Mettre à jour un template
  async update(id: string, data: UpdateTemplateDto) {
    const template = await this.findById(id);

    return this.prisma.notificationTemplate.update({
      where: { id },
      data,
    });
  }

  // Activer/désactiver un template
  async setActive(id: string, isActive: boolean) {
    return this.prisma.notificationTemplate.update({
      where: { id },
      data: { isActive },
    });
  }

  // Supprimer un template
  async delete(id: string) {
    await this.findById(id); // Vérifier qu'il existe
    return this.prisma.notificationTemplate.delete({
      where: { id },
    });
  }
}
