# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a NestJS-based notification service with comprehensive tracking and monitoring capabilities. It handles asynchronous email/SMS/push notifications using BullMQ (Redis-backed queues), Prisma ORM (PostgreSQL), and MJML for email templating.

## Development Commands

**Package Manager**: This project uses `pnpm` (not npm or yarn)

```bash
# Install dependencies
pnpm install

# Development
pnpm run start:dev          # Watch mode with hot reload
pnpm run start:debug        # Debug mode with --inspect

# Build
pnpm run build              # Compile TypeScript to dist/

# Production
pnpm run start:prod         # Run compiled code from dist/

# Linting & Formatting
pnpm run lint               # ESLint with auto-fix
pnpm run format             # Prettier formatting

# Testing
pnpm run test               # Run all unit tests
pnpm run test:watch         # Watch mode for tests
pnpm run test:cov           # Generate coverage report
pnpm run test:e2e           # End-to-end tests
pnpm run test:debug         # Debug tests with Node inspector
pnpm run test -- --testPathPattern="app.controller"  # Run specific test file

# Prisma
pnpm prisma generate        # Generate Prisma Client (after schema changes)
pnpm prisma migrate dev     # Create and apply migrations
pnpm prisma studio          # Open Prisma Studio (DB GUI)
```

## Architecture

### Database Layer (Prisma + PostgreSQL)

**Database Models** (prisma/schema.prisma):

1. **User** - Users with notification preferences per channel
   - Email, phone, pushToken
   - Granular preferences: emailEnabled, smsEnabled, pushEnabled
   - Marketing vs transactional opt-in

2. **Notification** - Complete notification tracking with lifecycle management
   - Status tracking: PENDING → QUEUED → PROCESSING → SENT → DELIVERED
   - Multi-channel support: EMAIL, SMS, PUSH, IN_APP
   - Priority levels: LOW, NORMAL, HIGH, URGENT
   - Retry management with attemptCount, maxAttempts, nextRetryAt
   - Error tracking: errorCode, errorMessage, errorStack
   - Full timestamp trail: queuedAt, processingAt, sentAt, deliveredAt, failedAt
   - Links to BullMQ job via jobId and external provider via externalId

3. **NotificationTemplate** - Template versioning and management
   - Code-based lookup with version history
   - Channel and type classification
   - MJML template storage with variable schema
   - Active/inactive toggle for rollback

4. **NotificationStats** - Aggregated daily statistics
   - Per-channel and per-type metrics
   - Success/failure/bounce tracking
   - Average processing time monitoring

5. **NotificationWebhook** - Provider webhook logs (for delivery confirmations)

**Prisma Client Location**: `generated/prisma/`
**Prisma Configuration**: `prisma.config.ts` (handles DATABASE_URL from .env)

### Queue System (BullMQ + Redis)

**Queue Flow with DB Tracking**:
1. API request creates Notification record in DB (status: PENDING)
2. Job added to BullMQ queue with notificationId
3. Notification updated to QUEUED with jobId
4. MailProcessor picks up job → status: PROCESSING
5. Email sent → status: SENT (with externalId from SMTP)
6. Webhook received → status: DELIVERED
7. On failure → status: FAILED (with error details)

**BullMQ Configuration** (app.module.ts):
- Queue name: `mail_queue`
- Redis: localhost:6379
- MailProcessor: src/notification/mail.processor.ts

**BullBoard Monitoring UI**:
- URL: `http://localhost:3000/queues`
- Features: View jobs, retry failed, inspect payloads

**Graceful Degradation**: If Redis is unavailable, the service falls back to synchronous email sending. Notifications are still tracked in the database but bypass the queue.

### Service Layer

**Core Services** (src/services/):

1. **NotificationsService** - Main notification CRUD and lifecycle management
   - `create()` - Create notification record
   - `updateStatus()` - Update status with automatic timestamp management
   - `findAll()` - List with filters (status, channel, type, date range)
   - `findRetryable()` - Get failed notifications eligible for retry
   - `findScheduledReady()` - Get scheduled notifications ready to send
   - `getStats()` - Get statistics by user or globally

2. **UsersService** - User management and preferences
   - `findOrCreate()` - Idempotent user creation (useful for integration)
   - `updatePreferences()` - Update notification preferences per channel
   - `getUserNotificationStats()` - Per-user notification statistics

3. **NotificationTemplatesService** - Template versioning
   - `findByCode()` - Get active template by code
   - `create()` - Auto-versioning (deactivates old version)
   - `findVersions()` - Get all versions of a template

4. **NotificationStatsService** - Statistics aggregation
   - `upsertDailyStats()` - Update daily metrics
   - `aggregateStats()` - Calculate stats from raw notifications
   - `getGlobalSummary()` - Overall statistics with channel/type breakdown
   - `getSuccessRate()` - Calculate success/failure rates

### REST API (Supervision Endpoints)

**Controllers** (src/controllers/):

**NotificationsController** (`/notifications`):
- `GET /notifications` - List with filters (userId, status, channel, type, dates)
- `GET /notifications/:id` - Get notification details
- `POST /notifications` - Create new notification
- `PATCH /notifications/:id/status` - Update status
- `GET /notifications/stats/global` - Global statistics
- `GET /notifications/scheduled/ready` - Get scheduled ready to send
- `GET /notifications/failed/retryable` - Get retryable failures
- `DELETE /notifications/cleanup/:days` - Delete old notifications

**StatsController** (`/stats`):
- `GET /stats/daily` - Daily statistics with filters
- `GET /stats/summary` - Global summary by channel/type
- `GET /stats/success-rate` - Success rate metrics
- `POST /stats/aggregate` - Trigger stats aggregation

**TemplatesController** (`/templates`):
- `GET /templates` - List templates (filter by channel/type/active)
- `GET /templates/:id` - Get template by ID
- `GET /templates/code/:code` - Get active template by code
- `GET /templates/code/:code/versions` - Get version history
- `POST /templates` - Create new template (auto-versioning)
- `PATCH /templates/:id` - Update template
- `PATCH /templates/:id/active` - Activate/deactivate

**UsersController** (`/users`):
- `GET /users` - List users with pagination
- `GET /users/:id` - Get user with recent notifications
- `GET /users/email/:email` - Find by email
- `POST /users` - Create user
- `PATCH /users/:id/preferences` - Update notification preferences
- `GET /users/:id/stats` - User notification statistics
- `DELETE /users/:id` - Delete user (cascade notifications)

**HealthController** (`/health`):
- `GET /health` - Full system status (API, Redis, queue mode)
- `GET /health/redis` - Redis connection status

**Swagger Documentation**: Available at `/api`

### Email Sending (MJML Templates)

**NotificationService** (src/notification/notification.service.ts):
- Loads MJML templates from src/templates/
- Simple variable replacement: `{{variableName}}`
- Compiles MJML to responsive HTML
- Sends via @nestjs-modules/mailer

**SMTP Configuration** (app.module.ts - should be moved to .env):
- Host: smtp.example.com
- Port: 587
- Credentials: user@example.com / password

### File Structure

```
src/
├── main.ts                           # App entry (port 3000)
├── app.module.ts                     # Root module with all imports
├── app.controller.ts                 # Test endpoint: POST /send-test-email
├── database/
│   ├── prisma.module.ts              # Global Prisma module
│   └── prisma.service.ts             # Prisma client wrapper
├── services/                         # Business logic layer
│   ├── notifications.service.ts      # Notification lifecycle
│   ├── users.service.ts              # User management
│   ├── notification-templates.service.ts
│   └── notification-stats.service.ts # Statistics aggregation
├── controllers/                      # REST API layer
│   ├── notifications.controller.ts   # /notifications
│   ├── stats.controller.ts           # /stats
│   ├── templates.controller.ts       # /templates
│   ├── users.controller.ts           # /users
│   └── health.controller.ts          # /health
├── health/
│   └── redis-health.service.ts       # Redis connection monitoring
├── filters/
│   └── all-exceptions.filter.ts      # Global error handling
├── notification/
│   ├── notification.service.ts       # MJML email sending
│   └── mail.processor.ts             # BullMQ worker (with DB tracking)
└── templates/
    └── welcome.mjml                  # Email templates

prisma/
├── schema.prisma                     # Database schema
└── migrations/                       # Migration history

generated/prisma/                     # Generated Prisma Client
```

### Important Implementation Details

**Creating Notifications (Full Flow)**:
```typescript
// 1. Find or create user
const user = await usersService.findOrCreate(email);

// 2. Create notification in DB
const notification = await notificationsService.create({
  userId: user.id,
  type: NotificationType.WELCOME_EMAIL,
  channel: NotificationChannel.EMAIL,
  priority: NotificationPriority.NORMAL,
  recipient: email,
  templateId: 'welcome',
  payload: { name: userName }
});

// 3. Add job to BullMQ queue
const job = await mailQueue.add('send_welcome', {
  notificationId: notification.id,
  to: email,
  name: userName
});

// 4. Update notification with jobId
await notificationsService.updateStatus(notification.id, {
  status: NotificationStatus.QUEUED,
  jobId: job.id
});
```

**MailProcessor Tracking**:
- Automatically updates status: PROCESSING → SENT (or FAILED)
- Stores SMTP messageId as externalId
- Captures full error details on failure
- Compatible with legacy jobs (no notificationId)

**Enums** (from Prisma schema):
- NotificationStatus: PENDING, QUEUED, PROCESSING, SENT, DELIVERED, FAILED, BOUNCED, CANCELLED
- NotificationChannel: EMAIL, SMS, PUSH, IN_APP
- NotificationType: WELCOME_EMAIL, PASSWORD_RESET, ORDER_CONFIRMATION, etc.
- NotificationPriority: LOW, NORMAL, HIGH, URGENT

### Database Indexes

Critical indexes for performance:
- Notification: userId+status, status+createdAt, type+status, channel+status, scheduledFor, jobId, externalId
- User: email
- NotificationStats: date, date+channel+type (unique)
- NotificationWebhook: notificationId, event

### Environment Variables

Required in `.env` (see `.env.example`):
```bash
# Database
DATABASE_URL="postgresql://user:pass@localhost:5432/notification_db?schema=public"

# Redis (for BullMQ queue)
REDIS_HOST=localhost
REDIS_PORT=6379

# Application
PORT=3000
NODE_ENV=development

# SMTP Configuration
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USER=user@example.com
SMTP_PASSWORD=password
SMTP_FROM="No Reply <noreply@example.com>"
```

### Testing the System

```bash
# 1. Start PostgreSQL and Redis
docker run -p 5432:5432 -e POSTGRES_PASSWORD=password postgres
docker run -p 6379:6379 redis

# 2. Run migrations
pnpm prisma migrate dev

# 3. Start application
pnpm run start:dev

# 4. Send test email (creates user + notification + job)
curl -X POST "http://localhost:3000/send-test-email?email=test@example.com&name=John"

# 5. Monitor in BullBoard
open http://localhost:3000/queues

# 6. Check notification status
curl "http://localhost:3000/notifications?email=test@example.com"

# 7. View statistics
curl "http://localhost:3000/stats/summary"
```

### Monitoring & Supervision

**Key Endpoints for Operations**:
- `/queues` - BullBoard UI for queue monitoring
- `/notifications?status=FAILED` - List failed notifications
- `/notifications/failed/retryable` - Get notifications eligible for retry
- `/stats/success-rate?startDate=2025-01-01&endDate=2025-01-31` - Success metrics
- `/stats/summary` - Overall health dashboard data

**Database Cleanup**:
```bash
# Delete successful notifications older than 30 days
curl -X DELETE "http://localhost:3000/notifications/cleanup/30"
```
