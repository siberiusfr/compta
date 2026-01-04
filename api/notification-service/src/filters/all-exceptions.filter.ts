import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { BaseExceptionFilter } from '@nestjs/core';
import { Request, Response } from 'express';
import { NotificationException } from '../common/exceptions/notification.exception';

/**
 * Global Exception Filter
 *
 * Catches all unhandled exceptions and provides structured error responses.
 * Uses NestJS Logger which is automatically routed to Pino via nestjs-pino.
 *
 * Features:
 * - Extends BaseExceptionFilter for proper exception handling
 * - Logs all exceptions with full context (request, stack trace, etc.)
 * - Redis connection errors are suppressed from client responses
 * - NotificationException details are included in response
 * - Structured error responses with timestamp and path
 */
@Catch()
export class AllExceptionsFilter
  extends BaseExceptionFilter
  implements ExceptionFilter
{
  private readonly logger = new Logger(AllExceptionsFilter.name);

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    // Check if it's a Redis/BullMQ connection error
    if (this.isRedisConnectionError(exception)) {
      // Suppress Redis connection errors from being sent to client
      // They're already logged by RedisHealthService
      this.logger.debug('Redis connection error suppressed');
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

    const errorResponse: Record<string, unknown> = {
      statusCode: status,
      timestamp: new Date().toISOString(),
      path: request.url,
      method: request.method,
      message,
    };

    // Add error code and details if it's a NotificationException
    if (exception instanceof NotificationException) {
      errorResponse.errorCode = exception.errorCode;
      if (exception.details) {
        errorResponse.details = exception.details;
      }
    }

    // Build log context
    const logContext = {
      statusCode: status,
      method: request.method,
      url: request.url,
      requestId: request.headers['x-request-id'],
      correlationId: request.headers['x-correlation-id'],
      userAgent: request.headers['user-agent'],
      ip: request.ip || request.socket?.remoteAddress,
      errorCode:
        exception instanceof NotificationException
          ? exception.errorCode
          : undefined,
    };

    // Log based on status code
    if (status >= 500) {
      this.logger.error(
        `[${status}] ${request.method} ${request.url} - ${message}`,
        exception instanceof Error ? exception.stack : undefined,
        JSON.stringify(logContext),
      );
    } else if (status >= 400) {
      this.logger.warn(
        `[${status}] ${request.method} ${request.url} - ${message}`,
        JSON.stringify(logContext),
      );
    }

    response.status(status).json(errorResponse);
  }

  private isRedisConnectionError(exception: unknown): boolean {
    if (exception instanceof Error) {
      const errorCode = (exception as NodeJS.ErrnoException).code;
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
