<script setup lang="ts">
import { useAccounting } from '../composables/useAccounting'
import { Button } from '@/components/ui/button'
import { BookOpen, Plus, Search, ChevronRight } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { accounts, isLoading, formatCurrency, getAccountTypeLabel, getAccountTypeColor } =
  useAccounting()

const accountClasses = [
  { code: '1', name: 'Classe 1 - Comptes de capitaux' },
  { code: '2', name: "Classe 2 - Comptes d'immobilisations" },
  { code: '3', name: 'Classe 3 - Comptes de stocks' },
  { code: '4', name: 'Classe 4 - Comptes de tiers' },
  { code: '5', name: 'Classe 5 - Comptes financiers' },
  { code: '6', name: 'Classe 6 - Comptes de charges' },
  { code: '7', name: 'Classe 7 - Comptes de produits' },
]

const getAccountsByClass = (classCode: string) => {
  return accounts.value.filter((a) => a.code.startsWith(classCode))
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <BookOpen class="h-6 w-6" />
          Plan comptable
        </h1>
        <p class="text-muted-foreground">{{ accounts.length }} comptes</p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouveau compte
      </Button>
    </div>

    <!-- Search -->
    <div class="relative max-w-sm">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <input
        type="text"
        placeholder="Rechercher un compte..."
        class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
      />
    </div>

    <!-- Account Classes -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <div
      v-else
      class="space-y-4"
    >
      <div
        v-for="classItem in accountClasses"
        :key="classItem.code"
        class="rounded-xl border bg-card overflow-hidden"
      >
        <div class="p-4 bg-muted/30 font-semibold">
          {{ classItem.name }}
        </div>
        <div
          v-if="getAccountsByClass(classItem.code).length > 0"
          class="divide-y"
        >
          <div
            v-for="account in getAccountsByClass(classItem.code)"
            :key="account.id"
            class="flex items-center justify-between p-4 hover:bg-muted/30 transition-colors cursor-pointer"
          >
            <div class="flex items-center gap-4">
              <span class="font-mono text-sm font-medium bg-muted px-2 py-1 rounded">
                {{ account.code }}
              </span>
              <div>
                <p class="font-medium">{{ account.name }}</p>
                <span
                  :class="cn('text-xs px-2 py-0.5 rounded-full', getAccountTypeColor(account.type))"
                >
                  {{ getAccountTypeLabel(account.type) }}
                </span>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <span
                :class="cn('font-medium', account.balance < 0 ? 'text-red-600' : 'text-green-600')"
              >
                {{ formatCurrency(account.balance) }}
              </span>
              <ChevronRight class="h-4 w-4 text-muted-foreground" />
            </div>
          </div>
        </div>
        <div
          v-else
          class="p-4 text-sm text-muted-foreground text-center"
        >
          Aucun compte dans cette classe
        </div>
      </div>
    </div>
  </div>
</template>
