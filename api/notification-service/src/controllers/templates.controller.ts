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
  NotificationTemplatesService,
  type CreateTemplateDto,
  type UpdateTemplateDto,
} from '../services/notification-templates.service';
import { NotificationChannel, NotificationType } from '@prisma/client';

@ApiTags('templates')
@ApiBearerAuth('JWT')
@Controller('templates')
export class TemplatesController {
  constructor(
    private readonly templatesService: NotificationTemplatesService,
  ) {}

  // Créer un nouveau template
  @Post()
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create a new template' })
  @ApiResponse({ status: 201, description: 'Template created successfully' })
  @ApiResponse({ status: 400, description: 'Bad request' })
  async create(@Body() createDto: CreateTemplateDto) {
    return this.templatesService.create(createDto);
  }

  // Lister tous les templates
  @Get()
  @ApiOperation({ summary: 'List all templates' })
  @ApiQuery({ name: 'channel', required: false, enum: NotificationChannel })
  @ApiQuery({ name: 'type', required: false, enum: NotificationType })
  @ApiQuery({ name: 'isActive', required: false, type: Boolean })
  @ApiResponse({ status: 200, description: 'List of templates' })
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
  @ApiOperation({ summary: 'Get template by ID' })
  @ApiParam({ name: 'id', description: 'Template ID' })
  @ApiResponse({ status: 200, description: 'Template found' })
  @ApiResponse({ status: 404, description: 'Template not found' })
  async findOne(@Param('id') id: string) {
    return this.templatesService.findById(id);
  }

  // Récupérer un template actif par code
  @Get('code/:code')
  @ApiOperation({ summary: 'Get active template by code' })
  @ApiParam({ name: 'code', description: 'Template code' })
  @ApiResponse({ status: 200, description: 'Template found' })
  @ApiResponse({ status: 404, description: 'Template not found' })
  async findByCode(@Param('code') code: string) {
    return this.templatesService.findByCode(code);
  }

  // Récupérer toutes les versions d'un template
  @Get('code/:code/versions')
  @ApiOperation({ summary: 'Get all versions of a template' })
  @ApiParam({ name: 'code', description: 'Template code' })
  @ApiResponse({ status: 200, description: 'List of template versions' })
  async findVersions(@Param('code') code: string) {
    return this.templatesService.findVersions(code);
  }

  // Mettre à jour un template
  @Patch(':id')
  @ApiOperation({ summary: 'Update template' })
  @ApiParam({ name: 'id', description: 'Template ID' })
  @ApiResponse({ status: 200, description: 'Template updated successfully' })
  @ApiResponse({ status: 404, description: 'Template not found' })
  async update(@Param('id') id: string, @Body() updateDto: UpdateTemplateDto) {
    return this.templatesService.update(id, updateDto);
  }

  // Activer/désactiver un template
  @Patch(':id/active')
  @ApiOperation({ summary: 'Activate/deactivate template' })
  @ApiParam({ name: 'id', description: 'Template ID' })
  @ApiResponse({ status: 200, description: 'Template status updated' })
  @ApiResponse({ status: 404, description: 'Template not found' })
  async setActive(
    @Param('id') id: string,
    @Body() body: { isActive: boolean },
  ) {
    return this.templatesService.setActive(id, body.isActive);
  }

  // Supprimer un template
  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiOperation({ summary: 'Delete template' })
  @ApiParam({ name: 'id', description: 'Template ID' })
  @ApiResponse({ status: 204, description: 'Template deleted successfully' })
  @ApiResponse({ status: 404, description: 'Template not found' })
  async delete(@Param('id') id: string) {
    return this.templatesService.delete(id);
  }
}
