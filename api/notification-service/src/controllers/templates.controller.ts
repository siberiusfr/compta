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
  NotificationTemplatesService,
  type CreateTemplateDto,
  type UpdateTemplateDto,
} from '../services/notification-templates.service';
import { NotificationChannel, NotificationType } from '@prisma/client';

@Controller('templates')
export class TemplatesController {
  constructor(
    private readonly templatesService: NotificationTemplatesService,
  ) {}

  // Créer un nouveau template
  @Post()
  @HttpCode(HttpStatus.CREATED)
  async create(@Body() createDto: CreateTemplateDto) {
    return this.templatesService.create(createDto);
  }

  // Lister tous les templates
  @Get()
  async findAll(
    @Query('channel') channel?: NotificationChannel,
    @Query('type') type?: NotificationType,
    @Query('isActive') isActive?: string,
  ) {
    const filters: any = {};
    if (channel) filters.channel = channel;
    if (type) filters.type = type;
    if (isActive !== undefined) filters.isActive = isActive === 'true';

    return this.templatesService.findAll(filters);
  }

  // Récupérer un template par ID
  @Get(':id')
  async findOne(@Param('id') id: string) {
    return this.templatesService.findById(id);
  }

  // Récupérer un template actif par code
  @Get('code/:code')
  async findByCode(@Param('code') code: string) {
    return this.templatesService.findByCode(code);
  }

  // Récupérer toutes les versions d'un template
  @Get('code/:code/versions')
  async findVersions(@Param('code') code: string) {
    return this.templatesService.findVersions(code);
  }

  // Mettre à jour un template
  @Patch(':id')
  async update(@Param('id') id: string, @Body() updateDto: UpdateTemplateDto) {
    return this.templatesService.update(id, updateDto);
  }

  // Activer/désactiver un template
  @Patch(':id/active')
  async setActive(
    @Param('id') id: string,
    @Body() body: { isActive: boolean },
  ) {
    return this.templatesService.setActive(id, body.isActive);
  }

  // Supprimer un template
  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  async delete(@Param('id') id: string) {
    return this.templatesService.delete(id);
  }
}
