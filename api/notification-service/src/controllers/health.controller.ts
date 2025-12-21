import { Controller, Get } from '@nestjs/common';
import { RedisHealthService } from '../health/redis-health.service';

@Controller('health')
export class HealthController {
  constructor(private readonly redisHealth: RedisHealthService) {}

  @Get()
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
  async getRedisHealth() {
    return this.redisHealth.getConnectionStatus();
  }
}
