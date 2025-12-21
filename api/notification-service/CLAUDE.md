# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a NestJS-based notification service that handles asynchronous email delivery using BullMQ (Redis-backed job queues) and MJML for email templating.

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
```

## Architecture

### Core Components

**Email Queue System (BullMQ + Redis)**
- BullMQ queue named `mail_queue` registered in `app.module.ts`
- Redis connection configured in BullModule.forRoot (localhost:6379)
- `MailProcessor` (src/notification/mail.processor.ts) processes jobs from the queue
- Jobs are processed asynchronously, decoupled from HTTP requests
- **BullBoard UI** available at `/queues` for monitoring and managing jobs (configured in app.module.ts)

**Email Service (NotificationService)**
- Located in src/notification/notification.service.ts
- Uses @nestjs-modules/mailer for SMTP transport
- Compiles MJML templates to HTML at runtime
- Templates stored in src/templates/ directory

**MJML Template System**
- Template files in src/templates/ with .mjml extension
- Uses simple string replacement for variables (e.g., `{{name}}`)
- MJML compiled to responsive HTML using the `mjml` package
- Example: welcome.mjml template for welcome emails

### Data Flow

1. External request triggers email job creation
2. Job added to `mail_queue` (BullMQ/Redis)
3. `MailProcessor` picks up job based on job.name
4. Processor calls appropriate NotificationService method
5. Service loads MJML template, replaces variables, compiles to HTML
6. Email sent via configured SMTP transport

### Configuration Notes

**SMTP Configuration** (src/app.module.ts:21-33)
- Currently hardcoded with placeholder values
- Host: smtp.example.com (needs environment-specific configuration)
- Port: 587
- Auth credentials: user@example.com / password
- Default sender: noreply@example.com

**Redis Configuration** (src/app.module.ts:11-16)
- Host: localhost
- Port: 6379
- No authentication configured

**Important**: Both SMTP and Redis configurations should be externalized to environment variables for production use.

### Adding New Email Types

When adding a new email type:

1. Create MJML template in src/templates/
2. Add method in NotificationService to load and process template
3. Add job handler case in MailProcessor.process() matching job.name
4. Ensure MailProcessor is registered as a provider in AppModule

### File Structure

```
src/
├── main.ts                    # Application entry point (port 3000)
├── app.module.ts              # Root module with BullMQ and Mailer config
├── notification/
│   ├── notification.service.ts  # Email sending logic
│   └── mail.processor.ts        # BullMQ job processor
└── templates/
    └── welcome.mjml             # MJML email templates
```

### Queue Monitoring (BullBoard)

**BullBoard Dashboard** provides a web UI to monitor and manage BullMQ queues:

- **Access URL**: `http://localhost:3000/queues` (when app is running)
- **Features**: View jobs, retry failed jobs, clear queues, see job details
- **Configuration**: Configured in app.module.ts with ExpressAdapter

**Testing the Queue**:
```bash
# Start Redis (required)
redis-server  # or docker run -p 6379:6379 redis

# Start the application
pnpm run start:dev

# Add a test job to the queue
curl -X POST "http://localhost:3000/send-test-email?email=test@example.com&name=John"

# View the job in BullBoard
# Open browser: http://localhost:3000/queues
```

### Test Configuration

- Jest configured with ts-jest transformer
- Unit test pattern: `*.spec.ts` files in src/
- E2E tests in test/ directory with separate jest-e2e.json config
- Test root directory: src/
- Coverage output: coverage/ directory
