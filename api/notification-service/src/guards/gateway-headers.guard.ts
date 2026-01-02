import { Injectable, CanActivate, ExecutionContext, Logger, SetMetadata } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { NotificationException } from '../common/exceptions/notification.exception';

/**
 * Guard to verify Gateway headers for internal service-to-service communication.
 *
 * The API Gateway forwards to following headers:
 * - X-User-Id: The authenticated user ID
 * - X-User-Username: The username
 * - X-User-Email: The user email (masked)
 * - X-User-Roles: Comma-separated list of user roles (e.g., "ADMIN,COMPTABLE")
 * - X-Tenant-Id: The tenant ID
 *
 * This guard checks if the request has required roles from these headers.
 */
@Injectable()
export class GatewayHeadersGuard implements CanActivate {
  private readonly logger = new Logger(GatewayHeadersGuard.name);

  private static readonly HEADER_USER_ID = 'x-user-id';
  private static readonly HEADER_USERNAME = 'x-user-username';
  private static readonly HEADER_EMAIL = 'x-user-email';
  private static readonly HEADER_ROLES = 'x-user-roles';
  private static readonly HEADER_TENANT_ID = 'x-tenant-id';

  constructor(private readonly reflector: Reflector) {}

  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const handler = context.getHandler();

    // Get required roles from decorator (if any)
    const requiredRoles = this.reflector.get<string[]>('roles', handler) || [];

    // If no roles required, allow access
    if (requiredRoles.length === 0) {
      return true;
    }

    // Get user roles from Gateway headers
    const userRolesHeader = request.headers[GatewayHeadersGuard.HEADER_ROLES] as string;

    if (!userRolesHeader) {
      this.logger.warn('X-User-Roles header missing from Gateway');
      throw NotificationException.missingHeaders([GatewayHeadersGuard.HEADER_ROLES]);
    }

    // Parse roles from header (comma-separated)
    const userRoles = userRolesHeader.split(',').map((r: string) => r.trim().toUpperCase());

    // Check if user has at least one of the required roles
    const hasRequiredRole = requiredRoles.some((role: string) => userRoles.includes(role));

    if (!hasRequiredRole) {
      this.logger.warn(
        `Access denied: User roles [${userRoles.join(', ')}] do not include any required role [${requiredRoles.join(', ')}]`,
      );
      throw NotificationException.invalidRoles(requiredRoles, userRoles);
    }

    this.logger.debug(
      `Access granted: User has roles [${userRoles.join(', ')}], required [${requiredRoles.join(', ')}]`,
    );

    // Attach user info to request for later use
    request.user = {
      id: request.headers[GatewayHeadersGuard.HEADER_USER_ID],
      username: request.headers[GatewayHeadersGuard.HEADER_USERNAME],
      email: request.headers[GatewayHeadersGuard.HEADER_EMAIL],
      roles: userRoles,
      tenantId: request.headers[GatewayHeadersGuard.HEADER_TENANT_ID],
    };

    return true;
  }
}

/**
 * Decorator to specify required roles for an endpoint.
 *
 * Usage:
 * @Roles('ADMIN')
 * @Post('/sensitive-endpoint')
 * async sensitiveOperation() { ... }
 *
 * @Roles('ADMIN', 'COMPTABLE')
 * @Post('/management-endpoint')
 * async managementOperation() { ... }
 */
export const Roles = (...roles: string[]) => SetMetadata('roles', roles);
