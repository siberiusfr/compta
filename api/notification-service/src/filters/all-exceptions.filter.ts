import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Request, Response } from 'express';

@Catch()
export class AllExceptionsFilter implements ExceptionFilter {
  private readonly logger = new Logger(AllExceptionsFilter.name);

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    // Check if it's a Redis/BullMQ connection error
    if (this.isRedisConnectionError(exception)) {
      // Suppress Redis connection errors from being sent to client
      // They're already logged by RedisHealthService
      return;
    }

    const status =
      exception instanceof HttpException
        ? exception.getStatus()
        : HttpStatus.INTERNAL_SERVER_ERROR;

    const message =
      exception instanceof HttpException
        ? exception.message
        : 'Internal server error';

    const errorResponse = {
      statusCode: status,
      timestamp: new Date().toISOString(),
      path: request.url,
      method: request.method,
      message,
    };

    // Log only real errors, not Redis connection attempts
    if (!(exception instanceof HttpException) && !this.isRedisConnectionError(exception)) {
      this.logger.error(
        `${request.method} ${request.url}`,
        exception instanceof Error ? exception.stack : exception,
      );
    }

    response.status(status).json(errorResponse);
  }

  private isRedisConnectionError(exception: unknown): boolean {
    if (exception instanceof Error) {
      const errorCode = (exception as any).code;
      const errorMessage = exception.message;

      // Check for Redis connection errors
      if (errorCode === 'ECONNREFUSED') {
        return true;
      }

      if (errorMessage?.includes('Redis') || errorMessage?.includes('redis')) {
        return true;
      }

      // Check for AggregateError from ioredis
      if (exception.constructor.name === 'AggregateError') {
        return true;
      }
    }

    return false;
  }
}
