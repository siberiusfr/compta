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

## Generation API avec Orval

Orval genere automatiquement des clients API types a partir des specs OpenAPI.

### Commandes

```bash
pnpm run api:generate   # Generer les clients API
pnpm run api:watch      # Regenerer automatiquement a chaque changement
```

### Configuration (orval.config.ts)

```ts
import { defineConfig } from 'orval'

export default defineConfig({
  // Mode 'single' - un seul fichier genere
  oauth2: {
    input: {
      target: './openapi/oauth2.json',
    },
    output: {
      client: 'vue-query',
      mode: 'single',                   // Un seul fichier generated.ts
      target: './src/modules/oauth/api/generated.ts',
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },
  // Mode 'tags-split' - fichiers separes par tag OpenAPI
  documents: {
    input: {
      target: './openapi/documents.json',
    },
    output: {
      client: 'vue-query',
      mode: 'tags-split',               // Dossier avec fichiers par tag
      target: './src/modules/documents/api',
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },
})
```

### Modes de generation

| Mode | Description | Structure generee |
|------|-------------|-------------------|
| `single` | Un seul fichier | `generated.ts` |
| `tags-split` | Un fichier par tag OpenAPI | `generated/tag-name.ts` |
| `split` | Fichiers separes (types, hooks) | `generated/*.ts` |

### Structure des fichiers generes

```
src/modules/
├── oauth/
│   └── api/
│       └── generated.ts              # mode: 'single'
└── documents/
    └── api/
        ├── index.ts                  # Barrel file - re-export tout
        ├── generated.schemas.ts      # Types et interfaces
        ├── documents/
        │   └── documents.ts          # hooks pour le tag 'documents'
        ├── categories/
        │   └── categories.ts         # hooks pour le tag 'categories'
        ├── document-sharing/
        │   └── document-sharing.ts   # hooks pour le tag 'document-sharing'
        ├── document-versions/
        │   └── document-versions.ts  # hooks pour le tag 'document-versions'
        └── tags/
            └── tags.ts               # hooks pour le tag 'tags'
```

**Important:** Avec `tags-split`, vous devez creer un fichier `index.ts` barrel pour re-exporter les hooks et types:

```ts
// api/index.ts
export * from './generated.schemas'
export { useGetById, useUpload, ... } from './documents/documents'
export { useGetAll2, useCreate1, ... } from './categories/categories'
// etc.
```

### Imports selon le mode

```ts
// Mode 'single' - import depuis le fichier unique
import { useGetToken, useRefreshToken } from '@/modules/oauth/api/generated'

// Mode 'tags-split' - import depuis l'index.ts (recommande)
import {
  useGetById,
  useUpload,
  type DocumentResponse
} from '@/modules/documents/api'

// Mode 'tags-split' - import direct depuis un fichier tag specifique
import { useGetById } from '@/modules/documents/api/documents/documents'
import { useGetAll2 } from '@/modules/documents/api/categories/categories'
import type { DocumentResponse } from '@/modules/documents/api/generated.schemas'
```

### Ajouter un nouveau module API

1. Placer le fichier OpenAPI JSON dans `./openapi/`
2. Ajouter une nouvelle entree dans `orval.config.ts`:

```ts
export default defineConfig({
  // ... config existante
  newModule: {
    input: {
      target: './openapi/new-module.json',
    },
    output: {
      client: 'vue-query',
      mode: 'tags-split',             // Recommande pour les grosses APIs
      target: './src/modules/new-module/api',
      clean: true,
      prettier: true,
      override: {
        mutator: {
          path: './src/api/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },
})
```

3. Executer `pnpm run api:generate`

### Instance Axios Custom (src/api/axios-instance.ts)

L'instance custom gere automatiquement:
- Injection du token JWT dans les headers
- Refresh automatique du token sur erreur 401
- File d'attente des requetes pendant le refresh
- Redirection vers login si refresh echoue

### Utilisation dans les composants

```ts
// Query - lecture de donnees
import { useGetDocuments } from '@/modules/documents/api'

const { data, isLoading, error } = useGetDocuments()

// Mutation - creation/modification
import { useCreateDocument } from '@/modules/documents/api'

const { mutate, isPending } = useCreateDocument()

function handleSubmit(formData: CreateDocumentDto) {
  mutate(
    { data: formData },
    {
      onSuccess: (newDoc) => {
        console.log('Document cree:', newDoc.id)
      },
      onError: (error) => {
        console.error('Erreur:', error.message)
      },
    }
  )
}
```

### Types generes

Orval genere automatiquement:
- **Hooks TanStack Query** (`useGetDocuments`, `useCreateDocument`, etc.)
- **Types TypeScript** pour les requetes et reponses
- **Fonctions de mutation** avec gestion du cache

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
