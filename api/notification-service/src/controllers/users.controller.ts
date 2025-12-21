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
  UsersService,
  type CreateUserDto,
  type UpdateUserDto,
  type UpdateUserPreferencesDto,
} from '../services/users.service';

@Controller('users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  // Créer un utilisateur
  @Post()
  @HttpCode(HttpStatus.CREATED)
  async create(@Body() createDto: CreateUserDto) {
    return this.usersService.create(createDto);
  }

  // Lister tous les utilisateurs
  @Get()
  async findAll(@Query('page') page?: string, @Query('limit') limit?: string) {
    const filters: any = {};
    if (page) filters.page = parseInt(page, 10);
    if (limit) filters.limit = parseInt(limit, 10);

    return this.usersService.findAll(filters);
  }

  // Récupérer un utilisateur par ID
  @Get(':id')
  async findOne(@Param('id') id: string) {
    return this.usersService.findById(id);
  }

  // Récupérer un utilisateur par email
  @Get('email/:email')
  async findByEmail(@Param('email') email: string) {
    return this.usersService.findByEmail(email);
  }

  // Mettre à jour un utilisateur
  @Patch(':id')
  async update(@Param('id') id: string, @Body() updateDto: UpdateUserDto) {
    return this.usersService.update(id, updateDto);
  }

  // Mettre à jour les préférences de notification
  @Patch(':id/preferences')
  async updatePreferences(
    @Param('id') id: string,
    @Body() preferences: UpdateUserPreferencesDto,
  ) {
    return this.usersService.updatePreferences(id, preferences);
  }

  // Récupérer les statistiques de notification d'un utilisateur
  @Get(':id/stats')
  async getUserStats(@Param('id') id: string) {
    return this.usersService.getUserNotificationStats(id);
  }

  // Supprimer un utilisateur
  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  async delete(@Param('id') id: string) {
    return this.usersService.delete(id);
  }
}
