# Architecture Modulaire Compta

## Overview

This project implements a complete modular architecture for the Compta accounting application, designed for scalability, maintainability, and easy team collaboration.

## Stack Technique

- **Vue 3.5** with Composition API and `<script setup>`
- **TypeScript 5.9** - strict mode
- **Vite 7** - bundler
- **Tailwind CSS 4** - styling
- **Pinia 3** - state management
- **TanStack Vue Query 5** - server state / data fetching
- **Vue Router 4** - routing
- **lucide-vue-next** - icons

---

## Structure du Projet

```
src/
├── core/                      # Core infrastructure
│   ├── components/            # Core UI components (Sidebar, Topbar)
│   ├── layouts/               # Page layouts (DashboardLayout)
│   ├── router/                # Router configuration & guards
│   └── views/                # Core pages (Login, NotFound)
│
├── modules/                   # Feature modules (auto-sufficient)
│   ├── accounting/           # Comptabilité module
│   ├── companies/            # Entreprises module
│   ├── dashboard/            # Dashboard module
│   ├── documents/            # Documents module
│   ├── hr/                  # RH module
│   ├── notifications/        # Notifications module
│   ├── oauth/               # OAuth module
│   └── permissions/         # Permissions module
│
└── shared/                   # Shared utilities & components
    ├── components/           # Reusable UI components
    └── utils/               # Utility functions
```

---

## Architecture d'un Module

Chaque module est **auto-suffisant** et contient:

```
modules/[module-name]/
├── routes.ts                # Module routes
├── types/                   # TypeScript types
│   └── [module].types.ts
├── stores/                  # Pinia store
│   └── [module]Store.ts
├── composables/             # Module composables
│   └── use[Module].ts
├── mock-data/              # Mock data for development
│   └── [module].mock.ts
└── views/                  # Module views
    ├── [Module]View1.vue
    ├── [Module]View2.vue
    └── ...
```

---

## Modules Disponibles

### 1. Dashboard
- **Routes**: `/dashboard`
- **Store**: `useDashboardStore`
- **Features**: 
  - Vue d'ensemble des statistiques
  - Activité récente
  - Graphiques de revenus/dépenses

### 2. Notifications
- **Routes**: 
  - `/notifications/inbox` - Boîte de réception
  - `/notifications/sent` - Notifications envoyées
  - `/notifications/settings` - Paramètres
  - `/notifications/templates` - Modèles
- **Store**: `useNotificationsStore`
- **Features**:
  - Gestion des notifications
  - Modèles de notification
  - Paramètres de notification

### 3. OAuth
- **Routes**:
  - `/oauth/applications` - Applications OAuth
  - `/oauth/tokens` - Tokens OAuth
  - `/oauth/consents` - Consentements
- **Store**: `useOAuthStore`
- **Features**:
  - Gestion des applications OAuth
  - Gestion des tokens d'accès
  - Gestion des consentements

### 4. Documents
- **Routes**:
  - `/documents/all` - Tous les documents
  - `/documents/categories` - Catégories
  - `/documents/upload` - Téléverser
- **Store**: `useDocumentsStore`
- **Features**:
  - Gestion des documents
  - Catégorisation
  - Upload de fichiers

### 5. Permissions
- **Routes**:
  - `/permissions/roles` - Rôles
  - `/permissions/users` - Utilisateurs
  - `/permissions/audit` - Audit
- **Store**: `usePermissionsStore`
- **Features**:
  - Gestion des rôles
  - Gestion des utilisateurs
  - Audit des actions

### 6. Companies
- **Routes**:
  - `/companies/all` - Toutes les entreprises
  - `/companies/create` - Créer une entreprise
  - `/companies/settings` - Paramètres
- **Store**: `useCompaniesStore`
- **Features**:
  - CRUD entreprises
  - Paramètres de l'entreprise

### 7. HR
- **Routes**:
  - `/hr/employees` - Employés
  - `/hr/contracts` - Contrats
  - `/hr/leaves` - Congés
  - `/hr/payroll` - Paie
- **Store**: `useHrStore`
- **Features**:
  - Gestion des employés
  - Gestion des contrats
  - Gestion des demandes de congé
  - Gestion de la paie

### 8. Accounting
- **Routes**:
  - `/accounting/journal` - Journal
  - `/accounting/ledger` - Grand livre
  - `/accounting/balance-sheet` - Bilan
  - `/accounting/income-statement` - Compte de résultat
  - `/accounting/invoices` - Factures
  - `/accounting/expenses` - Dépenses
  - `/accounting/reports` - Rapports
- **Store**: `useAccountingStore`
- **Features**:
  - Journal comptable
  - Grand livre
  - Bilan et compte de résultat
  - Gestion des factures
  - Gestion des dépenses
  - Rapports financiers

---

## Conventions

### Création d'un nouveau module

1. Créer le dossier `modules/[module-name]/`
2. Créer les fichiers suivants:
   - `routes.ts` - Exporter les routes du module
   - `types/[module].types.ts` - Définir les types TypeScript
   - `stores/[module]Store.ts` - Créer le Pinia store
   - `composables/use[Module].ts` - Créer le composable
   - `mock-data/[module].mock.ts` - Ajouter les mock data
   - `views/*.vue` - Créer les vues

3. Importer les routes dans `core/router/index.ts`:

```typescript
import { [module]Routes } from '@/modules/[module]/routes'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/core/layouts/DashboardLayout.vue'),
    children: [
      ...[module]Routes,
      // ... autres modules
    ]
  }
]
```

4. Ajouter le menu dans `core/components/Sidebar.vue`:

```typescript
const menuItems = [
  // ... autres items
  {
    icon: IconComponent,
    label: 'Module Name',
    children: [
      { label: 'View 1', route: '/[module]/view1' },
      { label: 'View 2', route: '/[module]/view2' }
    ]
  }
]
```

### Store Pattern (Pinia)

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { mockData } from '../mock-data/[module].mock'
import type { EntityType } from '../types/[module].types.ts'

export const use[Module]Store = defineStore('[module]', () => {
  const items = ref<EntityType[]>(mockData)

  const activeItems = computed(() => 
    items.value.filter(i => i.active)
  )

  function createItem(item: Omit<EntityType, 'id'>) {
    const newItem: EntityType = {
      ...item,
      id: Date.now().toString()
    }
    items.value.push(newItem)
    return newItem
  }

  function updateItem(id: string, updates: Partial<EntityType>) {
    const index = items.value.findIndex(i => i.id === id)
    if (index !== -1) {
      items.value[index] = { ...items.value[index], ...updates }
    }
  }

  function deleteItem(id: string) {
    const index = items.value.findIndex(i => i.id === id)
    if (index !== -1) {
      items.value.splice(index, 1)
    }
  }

  return {
    items,
    activeItems,
    createItem,
    updateItem,
    deleteItem
  }
})
```

### Vue Component Pattern

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import { use[Module] } from '../composables/use[Module]'
import { IconComponent } from 'lucide-vue-next'

const { items, createItem, updateItem, deleteItem } = use[Module]()
const searchQuery = ref('')

const filteredItems = computed(() => {
  if (!searchQuery.value) return items.value
  return items.value.filter(/* filter logic */)
})

function handleCreate() {
  // Create logic
}
</script>

<template>
  <div class="space-y-6">
    <!-- Template content -->
  </div>
</template>
```

---

## Intégration Backend Future

L'architecture est prête pour l'intégration avec un backend Spring Boot:

### Remplacer les mock data par des appels API

```typescript
// modules/[module]/stores/[module]Store.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'

export const use[Module]Store = defineStore('[module]', () => {
  const queryClient = useQueryClient()

  const { data: items, isLoading } = useQuery({
    queryKey: ['[module]'],
    queryFn: async () => {
      const response = await fetch('/api/[module]')
      return response.json()
    }
  })

  const createMutation = useMutation({
    mutationFn: async (item: CreateDto) => {
      const response = await fetch('/api/[module]', {
        method: 'POST',
        body: JSON.stringify(item)
      })
      return response.json()
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['[module]'] })
    }
  })

  return {
    items,
    isLoading,
    createItem: createMutation.mutate
  }
})
```

### Créer les endpoints API

```typescript
// api/endpoints/[module].ts
import { apiClient } from '../client'
import type { EntityType, CreateDto, UpdateDto } from '@/types'

export async function get[Module]Items(): Promise<EntityType[]> {
  const { data } = await apiClient.get('/[module]')
  return data
}

export async function create[Module]Item(dto: CreateDto): Promise<EntityType> {
  const { data } = await apiClient.post('/[module]', dto)
  return data
}

export async function update[Module]Item(id: string, dto: UpdateDto): Promise<EntityType> {
  const { data } = await apiClient.put(`/[module]/${id}`, dto)
  return data
}

export async function delete[Module]Item(id: string): Promise<void> {
  await apiClient.delete(`/[module]/${id}`)
}
```

---

## Avantages de cette Architecture

✅ **Scalable**: Facile d'ajouter de nouveaux modules
✅ **Maintenable**: Code organisé et prévisible
✅ **Testable**: Modules isolés
✅ **Réutilisable**: Composants partagés dans `/shared`
✅ **Performance**: Lazy loading par module
✅ **Team-friendly**: Plusieurs devs peuvent travailler en parallèle
✅ **Migration facile**: Backend intégration module par module
✅ **Type-safe**: TypeScript strict sur tous les modules
✅ **Self-documenting**: Structure claire et intuitive

---

## Démarrage

```bash
npm install
npm run dev
```

L'application sera accessible sur `http://localhost:5173`

Pour la connexion, utilisez n'importe quel email et mot de passe (mode démo).
