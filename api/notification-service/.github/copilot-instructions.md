# Copilot / AI assistant instructions for Notification Service

Purpose: short, actionable guidance for AI coding agents working on this service.

1) Big picture
- This is a NestJS TypeScript microservice implementing notification delivery (EMAIL/SMS/PUSH/IN_APP).
- DB: Prisma + PostgreSQL (schema in `prisma/schema.prisma`; client in `generated/prisma/`).
- Queue: BullMQ (Redis) used for async delivery; worker: `src/notification/mail.processor.ts`.
- Templates: MJML templates live in `src/templates/` and are compiled in `src/notification/notification.service.ts`.

2) Where to start reading (quick path to understand flow)
- `src/controllers/notifications.controller.ts` — API entrypoints for creating notifications.
- `src/services/notifications.service.ts` — creates DB record then enqueues job.
- `src/notification/mail.processor.ts` — job processing, sends email and updates notification status.
- `prisma/schema.prisma` — data model (Notification, User, NotificationTemplate, NotificationStats).

3) Key conventions & patterns (do exactly as code does)
- Use `notificationsService.create()` to create records; the code expects a DB record first, then a BullMQ job added referencing `notificationId`.
- Status transitions are explicit (PENDING → QUEUED → PROCESSING → SENT → DELIVERED → FAILED). Update timestamps when changing status.
- Templates use a `code` lookup and auto-versioning. New template creation should deactivate previous version.
- On failures capture `errorCode`, `errorMessage`, `errorStack`, and increment `attemptCount`.

4) Local dev & common commands (must use `pnpm`)
- `pnpm install`
- `pnpm run start:dev` (watch)
- `pnpm run build` / `pnpm run start:prod`
- `pnpm prisma generate` and `pnpm prisma migrate dev` after schema changes
- Tests: `pnpm run test` (unit), `pnpm run test:e2e` (integration)

5) Environment & infra
- Check `.env.example` for required env vars: `DATABASE_URL`, `REDIS_HOST/PORT`, `SMTP_*`, `PORT`.
- When adding new envs, update `.env.example` and `prisma.config.ts` if DB-related.

6) Monitoring & operations
- BullBoard is mounted at `/queues` (default port 3000) — useful to inspect job payloads and retry failures.
- Useful endpoints: `/health`, `/notifications?status=FAILED`, `/notifications/failed/retryable`, `/stats/summary`.

7) Testing notes
- To test full flow, run Postgres + Redis locally (docker recommended), run migrations, then `pnpm run start:dev` and POST to `/send-test-email` or `/notifications`.
- For unit tests, mock Prisma and BullMQ; integration tests can use ephemeral docker DB/Redis.

8) Files to edit for common tasks
- Add processors: `src/notification/mail.processor.ts` (or add new processors for SMS/PUSH under `src/notification/`).
- DB changes: edit `prisma/schema.prisma` → `pnpm prisma migrate dev` → commit generated migration.
- Template changes: add/modify `.mjml` files in `src/templates/` and ensure `NotificationTemplate` versions updated via service.

9) Style & CI
- Repo uses ESLint/Prettier via `pnpm run lint` / `pnpm run format`. Follow existing code style and async/await patterns.

10) When you need context to make a change
- Link to TODO: show the minimal stack trace, relevant controller/service path, and the Prisma model from `prisma/schema.prisma`.
- Example helpful prompt: "I need to modify retry logic for notifications — show where `attemptCount` and `nextRetryAt` are updated and related tests." 

If this looks correct, I can expand examples (small code snippets) or propagate the same file to sibling services (`auth-service`, `authz-service`, etc.). Please tell me if you want those added.
