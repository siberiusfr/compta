<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import {
  Home,
  Bell,
  Lock,
  FileText,
  Shield,
  Building2,
  Users,
  Calculator,
  LogOut,
  Menu,
  X
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const isOpen = ref(true)

const menuItems = [
  {
    icon: Home,
    label: 'Dashboard',
    route: '/dashboard'
  },
  {
    icon: Bell,
    label: 'Notifications',
    children: [
      { label: 'Boîte de réception', route: '/notifications/inbox' },
      { label: 'Envoyées', route: '/notifications/sent' },
      { label: 'Paramètres', route: '/notifications/settings' },
      { label: 'Modèles', route: '/notifications/templates' }
    ]
  },
  {
    icon: Lock,
    label: 'OAuth',
    children: [
      { label: 'Applications', route: '/oauth/applications' },
      { label: 'Tokens', route: '/oauth/tokens' },
      { label: 'Consentements', route: '/oauth/consents' }
    ]
  },
  {
    icon: FileText,
    label: 'Documents',
    children: [
      { label: 'Tous les documents', route: '/documents/all' },
      { label: 'Catégories', route: '/documents/categories' },
      { label: 'Téléverser', route: '/documents/upload' }
    ]
  },
  {
    icon: Shield,
    label: 'Permissions',
    children: [
      { label: 'Rôles', route: '/permissions/roles' },
      { label: 'Utilisateurs', route: '/permissions/users' },
      { label: 'Audits', route: '/permissions/audit' }
    ]
  },
  {
    icon: Building2,
    label: 'Entreprises',
    children: [
      { label: 'Toutes les entreprises', route: '/companies/all' },
      { label: 'Créer une entreprise', route: '/companies/create' },
      { label: 'Paramètres', route: '/companies/settings' }
    ]
  },
  {
    icon: Users,
    label: 'RH',
    children: [
      { label: 'Employés', route: '/hr/employees' },
      { label: 'Contrats', route: '/hr/contracts' },
      { label: 'Congés', route: '/hr/leaves' },
      { label: 'Paie', route: '/hr/payroll' }
    ]
  },
  {
    icon: Calculator,
    label: 'Comptabilité',
    children: [
      { label: 'Journal', route: '/accounting/journal' },
      { label: 'Grand livre', route: '/accounting/ledger' },
      { label: 'Bilan', route: '/accounting/balance-sheet' },
      { label: 'Compte de résultat', route: '/accounting/income-statement' },
      { label: 'Factures', route: '/accounting/invoices' },
      { label: 'Dépenses', route: '/accounting/expenses' },
      { label: 'Rapports', route: '/accounting/reports' }
    ]
  }
]

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

function isActive(itemRoute: string): boolean {
  return route.path.startsWith(itemRoute)
}

function isItemActive(item: any): boolean {
  if (item.route) {
    return isActive(item.route)
  }
  if (item.children) {
    return item.children.some((child: any) => isActive(child.route))
  }
  return false
}
</script>

<template>
  <aside
    :class="[
      'bg-white border-r border-gray-200 transition-all duration-300 flex flex-col',
      isOpen ? 'w-64' : 'w-16'
    ]"
  >
    <div class="flex items-center justify-between p-4 border-b border-gray-200">
      <h1 v-if="isOpen" class="text-xl font-bold text-gray-900">
        Compta
      </h1>
      <button
        @click="isOpen = !isOpen"
        class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
      >
        <Menu v-if="isOpen" :size="20" />
        <X v-else :size="20" />
      </button>
    </div>

    <nav class="flex-1 overflow-auto p-4">
      <ul class="space-y-1">
        <li v-for="item in menuItems" :key="item.label">
          <div v-if="item.children" class="mb-2">
            <div
              :class="[
                'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors mb-1',
                isItemActive(item)
                  ? 'text-blue-600 bg-blue-50'
                  : 'text-gray-700 hover:bg-gray-100'
              ]"
            >
              <component :is="item.icon" :size="18" />
              <span v-if="isOpen">{{ item.label }}</span>
            </div>
            <ul v-if="isOpen" class="pl-6 space-y-1">
              <li v-for="child in item.children" :key="child.route">
                <RouterLink
                  :to="child.route"
                  class="block px-3 py-2 rounded-lg text-sm transition-colors"
                  :class="[
                    isActive(child.route)
                      ? 'text-blue-600 bg-blue-50 font-medium'
                      : 'text-gray-600 hover:bg-gray-100'
                  ]"
                >
                  {{ child.label }}
                </RouterLink>
              </li>
            </ul>
          </div>
          <RouterLink
            v-else
            :to="item.route"
            class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="[
              isItemActive(item)
                ? 'text-blue-600 bg-blue-50'
                : 'text-gray-700 hover:bg-gray-100'
            ]"
          >
            <component :is="item.icon" :size="18" />
            <span v-if="isOpen">{{ item.label }}</span>
          </RouterLink>
        </li>
      </ul>
    </nav>

    <div class="p-4 border-t border-gray-200">
      <button
        @click="handleLogout"
        class="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 transition-colors"
      >
        <LogOut :size="18" />
        <span v-if="isOpen">Déconnexion</span>
      </button>
    </div>
  </aside>
</template>
