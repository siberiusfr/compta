import { Controller, Get } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { RedisHealthService } from '../health/redis-health.service';

@ApiTags('health')
@Controller('health')
export class HealthController {
  constructor(private readonly redisHealth: RedisHealthService) {}

  @Get()
  @ApiOperation({ summary: 'Get overall service health' })
  @ApiResponse({ status: 200, description: 'Service is healthy' })
  @ApiResponse({ status: 503, description: 'Service is unhealthy' })
  async getHealth() {
    const redisStatus = await this.redisHealth.getConnectionStatus();

    return {
      status: 'ok',
      timestamp: new Date().toISOString(),
      services: {
        api: {
          status: 'up',
        },
        redis: {
          status: redisStatus.connected ? 'up' : 'down',
          ...redisStatus,
        },
        queue: {
          status: redisStatus.connected ? 'up' : 'degraded',
          mode: redisStatus.connected ? 'async (BullMQ)' : 'sync (fallback)',
        },
      },
    };
  }

  @Get('redis')
  @ApiOperation({ summary: 'Get Redis connection status' })
  @ApiResponse({ status: 200, description: 'Redis status' })
  async getRedisHealth() {
    return this.redisHealth.getConnectionStatus();
  }
}
