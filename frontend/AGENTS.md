# Project Guidelines for AI Agents

## Overview
This document provides essential guidelines for all AI agents working on this Vue 3 + TypeScript project.

## Tech Stack

### Core Technologies
- **Framework**: Vue 3 (Composition API with `<script setup>`)
- **Language**: TypeScript (strict mode enabled)
- **Build Tool**: Vite
- **State Management**: Pinia
- **Routing**: Vue Router 4
- **HTTP Client**: Axios
- **Data Fetching**: @tanstack/vue-query (Vue Query)

### UI Framework
- **Primary UI Library**: Naive UI (naive-ui)
- **Icons**: @vicons/ionicons5
- **Form Validation**: vee-validate with Zod schemas

### Other Libraries
- **Date Handling**: dayjs
- **Utilities**: lodash-es
- **Auto Imports**: unplugin-auto-import (Vue composables and utilities are auto-imported)

## Design Rules

### ⚠️ CRITICAL: Naive UI Only
**ALWAYS use Naive UI components for design. NEVER use native CSS for styling.**

#### Why Naive UI?
- Consistent design system
- Built-in theming support
- Accessible components
- TypeScript support
- No need for custom CSS

#### Examples:
```vue
<!-- ✅ CORRECT: Use Naive UI components -->
<template>
  <n-card>
    <n-space vertical>
      <n-button type="primary">Click me</n-button>
      <n-input v-model:value="text" />
    </n-space>
  </n-card>
</template>

<!-- ❌ WRONG: Native CSS styling -->
<template>
  <div class="my-card">
    <button class="my-button">Click me</button>
  </div>
</template>

<style>
.my-card {
  padding: 16px;
  border: 1px solid #ccc;
}
.my-button {
  background: blue;
  color: white;
}
</style>
```

#### Allowed CSS Usage:
- Only for global resets and base styles (already in `src/style.css`)
- For layout utilities that Naive UI doesn't provide (rare)
- If absolutely necessary, use Naive UI's built-in style props instead

#### Naive UI Components to Use:
- Layout: `n-layout`, `n-layout-sider`, `n-layout-header`, `n-layout-content`
- Containers: `n-card`, `n-collapse`, `n-collapse-item`
- Forms: `n-form`, `n-form-item`, `n-input`, `n-input-number`, `n-select`, `n-date-picker`
- Buttons: `n-button`, `n-button-group`
- Feedback: `n-message`, `n-dialog`, `n-modal`, `n-notification`
- Data Display: `n-table`, `n-list`, `n-descriptions`, `n-statistic`
- Navigation: `n-menu`, `n-breadcrumb`, `n-tabs`
- Loading: `n-spin`, `n-skeleton`
- Other: `n-space`, `n-grid`, `n-divider`, `n-tag`, `n-badge`

## Code Style Guidelines

### Component Structure
```vue
<script setup lang="ts">
// 1. Imports (no need for Vue composables - auto-imported)
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/modules/auth/stores/authStore'

// 2. Composables
const router = useRouter()
const route = useRoute()
const message = useMessage()
const authStore = useAuthStore()

// 3. Reactive state
const loading = ref(false)
const formValue = ref({ ... })

// 4. Computed
const canSubmit = computed(() => { ... })

// 5. Methods
function handleSubmit() { ... }

// 6. Lifecycle hooks
onMounted(() => { ... })
</script>

<template>
  <!-- Template content using Naive UI components -->
</template>

<!-- NO style tags unless absolutely necessary -->
```

### TypeScript Rules
- Always use TypeScript for new files
- Use interfaces for type definitions
- Leverage type inference where possible
- Avoid `any` type - use `unknown` or proper types instead

### Auto-imported Composables
These are available without imports:
- `ref`, `computed`, `reactive`, `watch`, `watchEffect`
- `onMounted`, `onUnmounted`, `onUpdated`
- `useRouter`, `useRoute`
- `useStore` (Pinia stores)

### Store Pattern
Use Pinia stores for state management:
```typescript
import { defineStore } from 'pinia'

export const useMyStore = defineStore('my', {
  state: () => ({ ... }),
  getters: { ... },
  actions: { ... }
})
```

## Project Structure

```
src/
├── api/           # API client and configuration
├── assets/        # Static assets
├── components/    # Reusable components (use Naive UI)
├── composables/    # Vue composables
├── config/        # Configuration files
├── constants/     # Constants and enums
├── layouts/       # Layout components (use Naive UI)
├── modules/       # Feature modules (auth, accounting, hr, etc.)
│   ├── [module]/
│   │   ├── api/       # API calls for module
│   │   ├── stores/     # Pinia stores
│   │   ├── types/      # TypeScript types
│   │   ├── views/      # Page components
│   │   └── routes.ts  # Module routes
├── plugins/       # Vue plugins
├── router/        # Vue Router configuration
│   └── middleware/  # Navigation guards
├── stores/        # Global stores
├── types/         # Global TypeScript types
├── utils/         # Utility functions
├── views/         # Global views (404, 500, etc.)
├── App.vue        # Root component
└── main.ts        # Application entry point
```

## API Integration

### Using Generated API
The project uses Orval to generate API client from OpenAPI spec:
```typescript
import { useGetUserById } from '@/modules/auth/api/generated/auth-api'

const { data, isLoading, error } = useGetUserById(userId)
```

### Custom API Calls
Use the configured axios client:
```typescript
import { apiClient } from '@/api/client'

const response = await apiClient.get('/api/endpoint')
```

## Routing

### Route Definitions
```typescript
{
  path: '/example',
  name: 'example',
  component: () => import('./views/ExampleView.vue'),
  meta: {
    layout: 'default', // or 'auth'
    requiresAuth: true,
    title: 'Example Page'
  }
}
```

### Navigation
```typescript
// Programmatic navigation
router.push({ name: 'example' })
router.push({ path: '/example' })
router.push({ name: 'example', params: { id: 1 } })
```

## Testing

### Unit Tests
- Use Vitest for unit tests
- Test files should be named `*.test.ts` or `*.spec.ts`
- Use `@vue/test-utils` for component testing

### Test Commands
```bash
pnpm test              # Run tests in watch mode
pnpm test:run          # Run tests once
pnpm test:coverage      # Run tests with coverage
```

## Common Patterns

### Form Handling with Naive UI
```vue
<template>
  <n-form ref="formRef" :model="formValue" :rules="rules">
    <n-form-item path="email" label="Email">
      <n-input v-model:value="formValue.email" />
    </n-form-item>
    <n-form-item path="password" label="Password">
      <n-input v-model:value="formValue.password" type="password" />
    </n-form-item>
    <n-button @click="handleSubmit">Submit</n-button>
  </n-form>
</template>

<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'

const formRef = ref<FormInst | null>(null)
const formValue = ref({ email: '', password: '' })

const rules: FormRules = {
  email: { required: true, message: 'Email is required' },
  password: { required: true, message: 'Password is required' }
}

async function handleSubmit() {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      // Submit form
    }
  })
}
</script>
```

### Loading States
```vue
<template>
  <n-spin :show="loading">
    <n-card>Content</n-card>
  </n-spin>
</template>

<script setup lang="ts">
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    // Fetch data
  } finally {
    loading.value = false
  }
}
</script>
```

### Error Handling
```vue
<script setup lang="ts">
import { useMessage } from 'naive-ui'

const message = useMessage()

async function fetchData() {
  try {
    // API call
  } catch (error) {
    message.error('Failed to fetch data')
  }
}
</script>
```

## Development Commands

```bash
pnpm dev              # Start development server
pnpm dev:host         # Start with host access
pnpm build            # Build for production
pnpm type-check       # TypeScript type checking
pnpm lint             # Run ESLint with auto-fix
pnpm format           # Format code with Prettier
pnpm test             # Run tests
```

## Important Notes

1. **Always use Naive UI components** - Never write custom CSS for styling
2. **Use TypeScript** - All new code must be typed
3. **Follow the module structure** - Keep related code together
4. **Use auto-imports** - Don't import Vue composables manually
5. **Handle errors gracefully** - Use Naive UI's message/notification components
6. **Test your changes** - Run tests and type-check before committing
7. **Keep components small** - Single responsibility principle
8. **Use composition API** - Always use `<script setup>` syntax

## Environment Variables

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=30000
VITE_APP_NAME=Compta
VITE_APP_VERSION=0.0.0
VITE_ENABLE_DEVTOOLS=true
```

## Resources

- [Vue 3 Documentation](https://vuejs.org/)
- [Naive UI Documentation](https://www.naiveui.com/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Vue Router Documentation](https://router.vuejs.org/)
- [TypeScript Documentation](https://www.typescriptlang.org/)
