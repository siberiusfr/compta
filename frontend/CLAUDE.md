# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Compta is a modern enterprise management application (French: "Comptabilité" = Accounting) built with Vue 3, TypeScript, and Vite. The application is structured around four main business modules: Authentication, Accounting, HR, and Documents.

## Development Commands

### Running the Application
```bash
pnpm dev              # Start dev server on localhost:3000
pnpm dev:host         # Start dev server with network access
pnpm preview          # Preview production build
pnpm preview:https    # Preview with HTTPS
```

### Building
```bash
pnpm build            # Type-check and build for production
pnpm build:staging    # Build with staging mode
pnpm build:analyze    # Build with bundle analysis
```

### Testing
```bash
pnpm test             # Run tests in watch mode (Vitest)
pnpm test:run         # Run tests once
pnpm test:ui          # Open Vitest UI
pnpm test:coverage    # Run tests with coverage report
```

### Code Quality
```bash
pnpm type-check       # Run TypeScript compiler without emitting files
pnpm lint             # Lint and auto-fix with ESLint
pnpm lint:check       # Lint without auto-fix
pnpm format           # Format code with Prettier
pnpm format:check     # Check formatting without modifying
pnpm check            # Run all checks (type-check + lint + format + test)
```

### API Code Generation
```bash
pnpm generate:api:auth    # Generate API client for auth module only
pnpm generate:api:all     # Generate all API clients (orval)
```

The project uses Orval to generate type-safe API clients from OpenAPI specs. API endpoints:
- Auth service: `http://localhost:8083/v3/api-docs`
- Accounting service: `http://localhost:8081/v3/api-docs`
- Invoicing service: `http://localhost:8082/v3/api-docs`

Generated code locations:
- Auth: `src/modules/auth/api/generated/auth-api.ts` (vue-query client)
- Other APIs: `src/api/comptabilite.ts` and `src/api/facturation.ts` (axios client)

All generated clients use the custom axios instance from `src/api/axios-mutator.ts`.

## Architecture

### Module-Based Structure

The application follows a strict module-based architecture. Each module is self-contained with its own routes, stores, views, components, and types:

```
src/modules/
├── auth/         # Authentication & session management
├── accounting/   # Invoices, expenses, financial dashboard
├── hr/           # Employees, payroll, HR statistics
└── documents/    # Document upload and management
```

**Module Structure Pattern:**
```
module-name/
├── views/          # Vue pages (lazy-loaded via router)
├── stores/         # Pinia stores for module state
├── routes.ts       # Route definitions exported to main router
├── components/     # Module-specific components
├── composables/    # Module-specific composables
├── api/            # Module-specific API (for generated code)
└── types/          # Module TypeScript types
```

### Router Architecture

- **Routes** are defined per-module in `routes.ts` and aggregated in `src/router/index.ts`
- **Default route** redirects to `/accounting`
- **Authentication guard** uses localStorage token check (`auth_token` key)
- Routes default to `requiresAuth: true` unless explicitly set to false
- **Middleware pipeline** (in order):
  1. Logging (`loggingMiddleware`)
  2. Progress bar (`progressMiddleware`)
  3. Auth check (inline guard checking `auth_token`)
  4. Permissions (`permissionsMiddleware`)
  5. Analytics (`analyticsMiddleware`)

### State Management

- **Pinia** for global and module-specific stores
- **Composition API** pattern (setup function style)
- Auth store location: `src/stores/auth.ts` (central auth store)
- Module stores: `src/modules/{module}/stores/{module}Store.ts`
- Auth tokens stored in localStorage with key `auth_token`

### API Layer

- **Axios** as HTTP client configured in `src/api/client.ts`
- **Base URL**: from `VITE_API_BASE_URL` env var or `/api` as fallback
- **Request interceptor**: Automatically adds `Authorization: Bearer {token}` header
- **Response interceptor**: Handles 401 errors by clearing auth and redirecting to `/login`
- **API proxy**: Dev server proxies `/api` to backend (configured in `vite.config.ts`)
- **Tanstack Query (Vue Query)**: Used for data fetching, caching, and mutations
  - Configuration in `src/plugins/vue-query.ts`
  - 5 minute stale time, 10 minute garbage collection
  - Auto-refetch on window focus and reconnect

### Auto-Imports

The project uses `unplugin-auto-import` and `unplugin-vue-components`:

**Auto-imported APIs:**
- Vue: `ref`, `computed`, `watch`, etc.
- Vue Router: `useRouter`, `useRoute`
- Pinia: `defineStore`, `storeToRefs`
- VueUse: All composables
- Naive UI: `useDialog`, `useMessage`, `useNotification`, `useLoadingBar`

**Auto-imported Components:**
- All Naive UI components (via `NaiveUiResolver`)
- Type definitions generated in `src/components.d.ts` and `src/auto-imports.d.ts`

### Path Aliases

Configured in both `vite.config.ts` and `tsconfig.app.json`:

```
@/             → src/
@modules/      → src/modules/
@components/   → src/components/
@layouts/      → src/layouts/
@utils/        → src/utils/
@api/          → src/api/
@stores/       → src/stores/
@app-types/    → src/types/
```

### Build Configuration

**Manual chunks** for optimal caching (in `vite.config.ts`):
- `naive-ui`: Naive UI library
- `icons`: @vicons/ionicons5
- `vendor`: Core framework (vue, vue-router, pinia, @tanstack/vue-query, axios)
- `utils-vendor`: Utility libraries (dayjs, lodash-es, zod)

## Testing

- **Framework**: Vitest with happy-dom environment
- **Setup file**: `vitest.setup.ts`
- **Vue Testing Library**: `@vue/test-utils` for component testing
- **Coverage provider**: v8
- Test files use `.test.ts` suffix (e.g., `format.test.ts`, `LoadingSpinner.test.ts`)

## Code Conventions

- **Components**: PascalCase (e.g., `UserProfile.vue`)
- **Files**: camelCase (e.g., `formatDate.ts`)
- **Stores**: `use` prefix (e.g., `useAuthStore`)
- **Composables**: `use` prefix (e.g., `useAsync`)
- **Types**: PascalCase (e.g., `User`, `ApiResponse`)

## Authentication Flow

1. User logs in via `/login` route
2. Backend returns JWT token
3. Token stored in localStorage as `auth_token`
4. Axios request interceptor attaches token to all requests
5. On 401 response, axios response interceptor clears auth and redirects to login
6. Router guard checks for token presence before allowing access to protected routes

## UI Framework

- **Naive UI**: Primary component library (auto-imported)
- **Icons**: @vicons/ionicons5
- **Styling**: CSS with global styles in `src/style.css`
- **Meta tag**: Naive UI requires `<meta name="naive-ui-style">` (added in `main.ts`)

## Important Notes

- This is a **frontend-only** repository; backend services run separately
- The application is in French (UI text, comments, variable names may be in French)
- Default port is `3000` for dev server
- Environment variables must be prefixed with `VITE_` to be exposed to client code
