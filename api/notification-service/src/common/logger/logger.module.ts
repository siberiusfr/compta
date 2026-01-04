import { Module } from '@nestjs/common';
import { LoggerModule as PinoLoggerModule, Params } from 'nestjs-pino';
import { Request, Response } from 'express';
import { IncomingMessage } from 'http';
import type { LevelWithSilent } from 'pino';

/**
 * Logger Module Configuration
 *
 * Provides structured logging with Pino integrated with NestJS.
 *
 * Features:
 * - Structured JSON logs for production
 * - Pretty-printed logs for development (pino-pretty)
 * - Configurable log levels via LOG_LEVEL env var
 * - Request/Response serializers (optimized, no body)
 * - Sensitive data redaction
 * - Auto-logging disabled for health/metrics endpoints
 * - Custom log levels based on HTTP status codes
 * - Request ID and Correlation ID tracking
 */

const isDevelopment = process.env.NODE_ENV !== 'production';

/**
 * Paths to exclude from auto-logging
 */
const EXCLUDED_PATHS = ['/health', '/metrics', '/health/redis'];

/**
 * Sensitive fields to redact from logs
 */
const REDACT_PATHS = [
  'req.headers.authorization',
  'req.headers.cookie',
  'req.body.password',
  'req.body.token',
  'req.body.accessToken',
  'req.body.email',
  'res.headers["set-cookie"]',
];

/**
 * Custom request serializer - optimized without body
 */
const reqSerializer = (req: IncomingMessage) => {
  const httpReq = req as Request;
  return {
    id: (httpReq as any).id,
    method: httpReq.method,
    url: httpReq.url,
    query: httpReq.query,
    params: httpReq.params,
    headers: {
      host: httpReq.headers.host,
      'user-agent': httpReq.headers['user-agent'],
      'content-type': httpReq.headers['content-type'],
      'content-length': httpReq.headers['content-length'],
    },
    remoteAddress: httpReq.socket?.remoteAddress,
  };
};

/**
 * Custom response serializer - optimized without body
 */
const resSerializer = (res: Response) => {
  return {
    statusCode: res.statusCode,
    headers: {
      'content-type': res.getHeader?.('content-type'),
      'content-length': res.getHeader?.('content-length'),
    },
  };
};

/**
 * Determine log level based on HTTP status code
 */
const customLogLevel = (
  _req: IncomingMessage,
  res: Response,
  err?: Error,
): LevelWithSilent => {
  if (err || res.statusCode >= 500) {
    return 'error';
  }
  if (res.statusCode >= 400) {
    return 'warn';
  }
  return 'info';
};

/**
 * Generate request ID if not present
 */
const genReqId = (req: IncomingMessage, res: Response): string => {
  const httpReq = req as Request;
  const existingId =
    httpReq.headers['x-request-id'] || httpReq.headers['x-correlation-id'];

  if (existingId && typeof existingId === 'string') {
    return existingId;
  }

  // Generate a simple unique ID
  return `${Date.now().toString(36)}-${Math.random().toString(36).substring(2, 9)}`;
};

/**
 * Pino configuration for nestjs-pino
 */
const pinoHttpConfig: Params = {
  pinoHttp: {
    level: process.env.LOG_LEVEL || 'info',
    name: 'notification-service',
    genReqId,
    customLogLevel,
    serializers: {
      req: reqSerializer,
      res: resSerializer,
    },
    redact: {
      paths: REDACT_PATHS,
      remove: true,
    },
    // Auto-logging configuration
    autoLogging: {
      ignore: (req: IncomingMessage) => {
        const httpReq = req as Request;
        const url = httpReq.url || '';
        return EXCLUDED_PATHS.some((path) => url.startsWith(path));
      },
    },
    // Custom props added to each log
    customProps: (req: IncomingMessage, _res: Response) => {
      const httpReq = req as Request;
      return {
        requestId:
          httpReq.headers['x-request-id'] || (httpReq as any).id || undefined,
        correlationId: httpReq.headers['x-correlation-id'] || undefined,
      };
    },
    // Transport configuration
    transport: isDevelopment
      ? {
          target: 'pino-pretty',
          options: {
            colorize: true,
            translateTime: 'SYS:standard',
            ignore: 'pid,hostname',
            singleLine: false,
          },
        }
      : undefined,
    // Add timestamp to all logs
    timestamp: () => `,"time":"${new Date().toISOString()}"`,
  },
};

@Module({
  imports: [PinoLoggerModule.forRoot(pinoHttpConfig)],
  exports: [PinoLoggerModule],
})
export class LoggerModule {}
