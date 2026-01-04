/**
 * Pino Logger Configuration (Legacy)
 *
 * @deprecated This file is kept for reference only.
 * Use nestjs-pino with LoggerModule instead.
 *
 * Migration:
 * - Import { PinoLogger, InjectPinoLogger } from 'nestjs-pino'
 * - Use @InjectPinoLogger(ServiceName) decorator in constructor
 * - LoggerModule is automatically configured in app.module.ts
 *
 * Features provided by LoggerModule:
 * - Structured JSON logs for production
 * - Pretty-printed logs for development (pino-pretty)
 * - Configurable log levels via LOG_LEVEL env var
 * - Request/Response serializers (optimized, no body)
 * - Sensitive data redaction
 * - Auto-logging disabled for health/metrics endpoints
 * - Custom log levels based on HTTP status codes
 * - Request ID and Correlation ID tracking
 */

// This file is intentionally empty as all logging is now handled by nestjs-pino.
// See src/common/logger/logger.module.ts for the new configuration.
