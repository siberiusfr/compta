import { NestFactory } from '@nestjs/core';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';
import { AppModule } from './app.module';
import { ValidationPipe } from '@nestjs/common';
import { AllExceptionsFilter } from './filters/all-exceptions.filter';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Set global prefix for all routes
  app.setGlobalPrefix('notif');

  // Enable validation pipes
  app.useGlobalPipes(new ValidationPipe({
    whitelist: true,
    forbidNonWhitelisted: true,
    transform: true,
  }));

  // Enable global exception filter
  app.useGlobalFilters(new AllExceptionsFilter());

  // Enable CORS (for gateway communication)
  app.enableCors({
    origin: true, // Allow all origins (gateway will handle auth)
    credentials: true,
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-User-Id', 'X-User-Roles', 'X-Tenant-Id'],
  });

  // Configure Swagger/OpenAPI
  const config = new DocumentBuilder()
    .setTitle('Notification Service API')
    .setDescription(`
      Notification Service for COMPTA ERP system.

      This service handles email, SMS, push notifications, and in-app notifications
      using queue-based architecture with BullMQ and Redis.

      ## Authentication
      This service is behind by API Gateway.
      Authentication is handled by gateway.

      The gateway forwards to following headers:
      - \`X-User-Id\`: The authenticated user ID
      - \`X-User-Username\`: The username
      - \`X-User-Email\`: The user email (masked)
      - \`X-User-Roles\`: Comma-separated list of user roles (e.g., "ADMIN,COMPTABLE")
      - \`X-Tenant-Id\`: The tenant ID
      - \`Authorization\`: Optional internal service token for service-to-service communication

      ## Features
      - Multi-channel notifications (Email, SMS, Push, In-App)
      - Queue-based processing with BullMQ
      - Template management with MJML
      - User preferences management
      - Delivery tracking and analytics
      - SendPulse integration
    `)
    .setVersion('1.0.0')
    .addTag('notifications', 'Notification management endpoints')
    .addTag('users', 'User management endpoints')
    .addTag('templates', 'Template management endpoints')
    .addTag('stats', 'Statistics and analytics endpoints')
    .addTag('health', 'Health check endpoints')
    .addTag('sendpulse', 'SendPulse API endpoints')
    .addServer('http://localhost:3000', 'Local development server')
    .addServer('https://api.compta.tn', 'Production server')
    .build();

  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('notif/api/docs', app, document);

  const port = process.env.PORT ?? 3000;
  await app.listen(port);

  console.log(`
    ╔════════════════════════════════════════╗
    ║                                                               ║
    ║   Notification Service API Documentation                      ║
    ║                                                               ║
    ║   📚 Swagger UI: http://localhost:${port}/notif/api/docs       ║
    ║   📄 API JSON:   http://localhost:${port}/notif/api/docs-json  ║
    ║                                                               ║
    ║   ℹ️  Authentication: Handled by Gateway               ║
    ║   ℹ️  Headers: X-User-Id, X-User-Roles, X-Tenant-Id   ║
    ║   ℹ️  Context: /notif/                             ║
    ║                                                               ║
    ╚══════════════════════════════════════════════╝
  `);
}
bootstrap();
