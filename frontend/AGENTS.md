# AGENTS.md - Conventions et Regles du Projet

## Stack Technique

- **Vue 3.5** avec Composition API et `<script setup>`
- **TypeScript 5.9** - strict mode
- **Vite 7** - bundler
- **Tailwind CSS 4** - styling (CSS-first config)
- **Pinia 3** - state management
- **TanStack Vue Query 5** - server state / data fetching
- **Axios** - client HTTP
- **Zod 4** - validation de schemas
- **VeeValidate 4** - gestion des formulaires
- **Vue Router 4** - routing
- **shadcn-vue (reka-ui)** - composants UI
- **VueUse** - composables utilitaires

---

## Structure des Dossiers

```
src/
├── api/                  # Clients API et endpoints
│   ├── client.ts         # Instance Axios configuree
│   └── endpoints/        # Fonctions par domaine
├── components/
│   ├── ui/               # Composants shadcn-vue
│   └── [feature]/        # Composants par feature
├── composables/          # Hooks Vue reutilisables
├── layouts/              # Layouts de pages
├── lib/                  # Utilitaires (utils.ts, etc.)
├── pages/                # Pages/vues de l'app
├── router/               # Configuration Vue Router
├── stores/               # Stores Pinia
├── types/                # Types TypeScript globaux
└── validators/           # Schemas Zod
```

---

## Conventions de Code

### Composants Vue

```vue
<script setup lang="ts">
// 1. Imports
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'

// 2. Props & Emits
const props = defineProps<{
  id: string
}>()

const emit = defineEmits<{
  submit: [data: FormData]
}>()

// 3. Composables
const { data, isLoading } = useQuery(...)

// 4. State reactif
const count = ref(0)

// 5. Computed
const doubled = computed(() => count.value * 2)

// 6. Fonctions
function handleClick() {
  // ...
}
</script>

<template>
  <!-- Template -->
</template>
```

### Nommage

| Type | Convention | Exemple |
|------|------------|---------|
| Composants | PascalCase | `UserProfile.vue` |
| Composables | camelCase + use | `useAuth.ts` |
| Stores | camelCase + Store | `userStore.ts` |
| Types/Interfaces | PascalCase | `User`, `ApiResponse` |
| Fichiers utilitaires | camelCase | `formatDate.ts` |
| Schemas Zod | camelCase + Schema | `userSchema.ts` |

### Props et Events

```ts
// Props - toujours typer avec generics
defineProps<{
  user: User
  isLoading?: boolean
}>()

// Events - utiliser la syntaxe tuple
defineEmits<{
  update: [value: string]
  delete: [id: number]
}>()
```

---

## Data Fetching avec TanStack Query

### Queries

```ts
// composables/useUsers.ts
import { useQuery } from '@tanstack/vue-query'
import { getUsers } from '@/api/endpoints/users'

export function useUsers() {
  return useQuery({
    queryKey: ['users'],
    queryFn: getUsers,
  })
}
```

### Mutations

```ts
import { useMutation, useQueryClient } from '@tanstack/vue-query'

export function useCreateUser() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
    },
  })
}
```

### Query Keys Convention

```ts
// Simple
['users']
['user', userId]

// Avec filtres
['users', { status: 'active' }]

// Hierarchique
['users', userId, 'posts']
```

---

## State Management avec Pinia

### Structure d'un Store

```ts
// stores/userStore.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value)

  // Actions
  function setUser(newUser: User) {
    user.value = newUser
  }

  function logout() {
    user.value = null
    token.value = null
  }

  return {
    user,
    token,
    isAuthenticated,
    setUser,
    logout,
  }
})
```

### Quand utiliser Pinia vs TanStack Query

| Pinia | TanStack Query |
|-------|----------------|
| Auth state | Donnees serveur |
| UI state (sidebar, theme) | Listes, details |
| Donnees locales | Cache API |
| Preferences utilisateur | CRUD operations |

---

## Formulaires avec VeeValidate + Zod

### Schema Zod

```ts
// validators/userSchema.ts
import { z } from 'zod'

export const userSchema = z.object({
  email: z.string().email('Email invalide'),
  password: z.string().min(8, 'Minimum 8 caracteres'),
  name: z.string().min(2, 'Minimum 2 caracteres'),
})

export type UserFormData = z.infer<typeof userSchema>
```

### Formulaire

```vue
<script setup lang="ts">
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { userSchema, type UserFormData } from '@/validators/userSchema'

const { handleSubmit, errors, defineField } = useForm<UserFormData>({
  validationSchema: toTypedSchema(userSchema),
})

const [email, emailAttrs] = defineField('email')
const [password, passwordAttrs] = defineField('password')

const onSubmit = handleSubmit((values) => {
  // values est type-safe
})
</script>
```

---

## API Client

### Configuration Axios

```ts
// api/client.ts
import axios from 'axios'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Intercepteur pour le token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Intercepteur pour les erreurs
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
    }
    return Promise.reject(error)
  }
)
```

### Endpoints

```ts
// api/endpoints/users.ts
import { apiClient } from '../client'
import type { User } from '@/types'

export async function getUsers(): Promise<User[]> {
  const { data } = await apiClient.get('/users')
  return data
}

export async function createUser(payload: CreateUserDto): Promise<User> {
  const { data } = await apiClient.post('/users', payload)
  return data
}
```

---

## Styling avec Tailwind

### Classes Utilitaires

```vue
<template>
  <!-- Utiliser cn() pour merger les classes conditionnelles -->
  <div :class="cn('p-4 rounded-lg', props.variant === 'primary' && 'bg-primary')">
    ...
  </div>
</template>
```

### Composants shadcn-vue

- Toujours importer depuis `@/components/ui/`
- Ne pas modifier les composants de base directement
- Creer des wrappers si customisation necessaire

---

## Regles Importantes

1. **Pas de `any`** - Toujours typer explicitement
2. **Pas de props drilling** - Utiliser Pinia ou provide/inject
3. **Pas de logique dans les templates** - Extraire dans computed
4. **Toujours `<script setup>`** - Pas de Options API
5. **Erreurs gerees** - Try/catch ou error boundaries
6. **Lazy loading** - Routes et composants lourds
7. **Accessibilite** - Utiliser les composants reka-ui correctement
8. **Tests** - Vitest pour unit, Playwright pour e2e (a ajouter)

---

## Variables d'Environnement

```env
VITE_API_URL=http://localhost:3000/api
VITE_APP_NAME=Compta
```

Acces dans le code :
```ts
import.meta.env.VITE_API_URL
```
