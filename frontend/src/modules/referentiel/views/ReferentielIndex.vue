<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { Database, Package, Users, Truck, Folder } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const router = useRouter()
const route = useRoute()

const tabs = [
  { id: 'produits', label: 'Produits', icon: Package, route: 'referentiel-produits-list' },
  { id: 'clients', label: 'Clients', icon: Users, route: 'referentiel-clients-list' },
  {
    id: 'fournisseurs',
    label: 'Fournisseurs',
    icon: Truck,
    route: 'referentiel-fournisseurs-list',
  },
  { id: 'familles', label: 'Familles', icon: Folder, route: 'referentiel-familles-list' },
]

const activeTab = computed(() => {
  const path = route.path
  if (path.includes('/produits')) return 'produits'
  if (path.includes('/clients')) return 'clients'
  if (path.includes('/fournisseurs')) return 'fournisseurs'
  if (path.includes('/familles')) return 'familles'
  return 'produits'
})

function navigateToTab(tabRoute: string) {
  router.push({ name: tabRoute })
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold flex items-center gap-2">
        <Database class="h-6 w-6" />
        Référentiel
      </h1>
      <p class="text-muted-foreground">Gérez vos données de référence</p>
    </div>

    <!-- Tabs -->
    <div class="border-b">
      <nav class="flex gap-1 -mb-px">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="navigateToTab(tab.route)"
          :class="
            cn(
              'flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors',
              activeTab === tab.id
                ? 'border-primary text-primary'
                : 'border-transparent text-muted-foreground hover:text-foreground hover:border-border'
            )
          "
        >
          <component
            :is="tab.icon"
            class="h-4 w-4"
          />
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- Content -->
    <router-view />
  </div>
</template>
