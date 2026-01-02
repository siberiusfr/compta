import { HttpException, HttpStatus } from '@nestjs/common';
import { ErrorCode } from './error-codes.enum';

/**
 * Custom exception for notification service with standardized error codes
 * and contextual error messages.
 */
export class NotificationException extends HttpException {
  readonly errorCode: ErrorCode;
  readonly details?: Record<string, any>;

  constructor(
    errorCode: ErrorCode,
    message: string,
    httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    details?: Record<string, any>,
  ) {
    super(message, httpStatus);
    this.errorCode = errorCode;
    this.details = details;
  }

  /**
   * Create a user not found exception
   */
  static userNotFound(userId: string): NotificationException {
    return new NotificationException(
      ErrorCode.USER_NOT_FOUND,
      `User with ID ${userId} not found`,
      HttpStatus.NOT_FOUND,
      { userId },
    );
  }

  /**
   * Create a user email not found exception
   */
  static userEmailNotFound(email: string): NotificationException {
    return new NotificationException(
      ErrorCode.USER_EMAIL_NOT_FOUND,
      `User with email ${email} not found`,
      HttpStatus.NOT_FOUND,
      { email },
    );
  }

  /**
   * Create a notification not found exception
   */
  static notificationNotFound(notificationId: string): NotificationException {
    return new NotificationException(
      ErrorCode.NOTIF_NOT_FOUND,
      `Notification with ID ${notificationId} not found`,
      HttpStatus.NOT_FOUND,
      { notificationId },
    );
  }

  /**
   * Create a template not found exception
   */
  static templateNotFound(templateCode: string): NotificationException {
    return new NotificationException(
      ErrorCode.TEMPLATE_NOT_FOUND,
      `Template with code ${templateCode} not found`,
      HttpStatus.NOT_FOUND,
      { templateCode },
    );
  }

  /**
   * Create an email send failed exception
   */
  static emailSendFailed(email: string, reason: string): NotificationException {
    return new NotificationException(
      ErrorCode.EMAIL_SEND_FAILED,
      `Failed to send email to ${email}: ${reason}`,
      HttpStatus.INTERNAL_SERVER_ERROR,
      { email, reason },
    );
  }

  /**
   * Create a template compilation failed exception
   */
  static templateCompilationFailed(templateCode: string, errors: string[]): NotificationException {
    return new NotificationException(
      ErrorCode.TEMPLATE_COMPILATION_FAILED,
      `Failed to compile template ${templateCode}`,
      HttpStatus.INTERNAL_SERVER_ERROR,
      { templateCode, errors },
    );
  }

  /**
   * Create a validation error exception
   */
  static validationError(field: string, reason: string): NotificationException {
    return new NotificationException(
      ErrorCode.VALIDATION_ERROR,
      `Validation error for field ${field}: ${reason}`,
      HttpStatus.BAD_REQUEST,
      { field, reason },
    );
  }

  /**
   * Create a queue job failed exception
   */
  static queueJobFailed(jobName: string, reason: string): NotificationException {
    return new NotificationException(
      ErrorCode.QUEUE_JOB_FAILED,
      `Queue job ${jobName} failed: ${reason}`,
      HttpStatus.INTERNAL_SERVER_ERROR,
      { jobName, reason },
    );
  }

  /**
   * Create a SendPulse API error exception
   */
  static sendPulseApiError(status: number, statusText: string): NotificationException {
    return new NotificationException(
      ErrorCode.SENDPULSE_API_ERROR,
      `SendPulse API error: ${status} ${statusText}`,
      HttpStatus.INTERNAL_SERVER_ERROR,
      { status, statusText },
    );
  }

  /**
   * Create a template load failed exception
   */
  static templateLoadFailed(templatePath: string, reason: string): NotificationException {
    return new NotificationException(
      ErrorCode.TEMPLATE_LOAD_FAILED,
      `Failed to load template from ${templatePath}: ${reason}`,
      HttpStatus.INTERNAL_SERVER_ERROR,
      { templatePath, reason },
    );
  }

  /**
   * Create an invalid payload exception
   */
  static invalidPayload(jobId: string, reason: string): NotificationException {
    return new NotificationException(
      ErrorCode.VALIDATION_ERROR,
      `Invalid job payload for job ${jobId}: ${reason}`,
      HttpStatus.BAD_REQUEST,
      { jobId, reason },
    );
  }

  /**
   * Create a missing headers exception
   */
  static missingHeaders(headers: string[]): NotificationException {
    return new NotificationException(
      ErrorCode.AUTH_MISSING_HEADERS,
      `Missing required headers: ${headers.join(', ')}`,
      HttpStatus.FORBIDDEN,
      { headers },
    );
  }

  /**
   * Create an invalid roles exception
   */
  static invalidRoles(required: string[], userRoles: string[]): NotificationException {
    return new NotificationException(
      ErrorCode.AUTH_INVALID_ROLES,
      `Required role(s) not found. Required: ${required.join(', ')}, User has: ${userRoles.join(', ')}`,
      HttpStatus.FORBIDDEN,
      { required, userRoles },
    );
  }
}
