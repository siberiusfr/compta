import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import Redis from 'ioredis';

@Injectable()
export class RedisHealthService implements OnModuleInit {
  private readonly logger = new Logger(RedisHealthService.name);
  private redis: Redis | null = null;
  private isConnected = false;
  private connectionAttempts = 0;
  private readonly MAX_RETRY_ATTEMPTS = 3;

  async onModuleInit() {
    await this.checkRedisConnection();
  }

  private async checkRedisConnection(): Promise<void> {
    try {
      this.redis = new Redis({
        host: process.env.REDIS_HOST || 'localhost',
        port: parseInt(process.env.REDIS_PORT || '6379'),
        maxRetriesPerRequest: 1,
        retryStrategy: (times) => {
          if (times > this.MAX_RETRY_ATTEMPTS) {
            return null; // Stop retrying
          }
          return Math.min(times * 100, 2000);
        },
        lazyConnect: true,
      });

      // Try to connect
      await this.redis.connect();

      // Test the connection
      await this.redis.ping();

      this.isConnected = true;
      this.logger.log('✅ Redis connection established successfully');

      // Handle disconnection
      this.redis.on('error', (error) => {
        this.isConnected = false;
        this.logger.warn(`❌ Redis connection error: ${error.message}`);
      });

      this.redis.on('close', () => {
        this.isConnected = false;
        this.logger.warn('❌ Redis connection closed');
      });

      this.redis.on('reconnecting', () => {
        this.logger.log('🔄 Redis reconnecting...');
      });

    } catch (error) {
      this.isConnected = false;
      this.connectionAttempts++;

      if (this.connectionAttempts === 1) {
        // Only log once to avoid spam
        this.logger.warn(
          `⚠️  Redis is not available (${error.message})\n` +
          `   → BullMQ queue features will be disabled\n` +
          `   → Notifications will be processed synchronously\n` +
          `   → To enable queue processing, start Redis:\n` +
          `     docker run -d -p 6379:6379 redis\n` +
          `     or: redis-server`
        );
      }

      // Clean up failed connection
      if (this.redis) {
        this.redis.disconnect();
        this.redis = null;
      }
    }
  }

  isRedisAvailable(): boolean {
    return this.isConnected;
  }

  getRedisClient(): Redis | null {
    return this.redis;
  }

  async getConnectionStatus() {
    return {
      connected: this.isConnected,
      host: process.env.REDIS_HOST || 'localhost',
      port: parseInt(process.env.REDIS_PORT || '6379'),
      attempts: this.connectionAttempts,
    };
  }
}
