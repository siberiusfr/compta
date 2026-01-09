<script setup lang="ts">
import { computed } from 'vue'
import { useHr } from '../composables/useHr'
import { Button } from '@/components/ui/button'
import {
  FileSignature,
  Plus,
  Search,
  Eye,
  Download,
  Calendar,
  Clock,
  AlertTriangle
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  contracts,
  employees,
  isLoading,
  formatCurrency,
  formatDate,
  getStatusColor,
  getStatusLabel,
  getContractTypeLabel
} = useHr()

const contractsWithEmployee = computed(() =>
  contracts.value.map(contract => ({
    ...contract,
    employee: employees.value.find(e => e.id === contract.employeeId)
  }))
)

const daysUntilEnd = (endDate?: Date) => {
  if (!endDate) return null
  const diff = new Date(endDate).getTime() - new Date().getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <FileSignature class="h-6 w-6" />
          Contrats
        </h1>
        <p class="text-muted-foreground">
          Gestion des contrats de travail
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouveau contrat
      </Button>
    </div>

    <!-- Search -->
    <div class="relative max-w-sm">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <input
        type="text"
        placeholder="Rechercher un contrat..."
        class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
      />
    </div>

    <!-- Contracts List -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="contract in contractsWithEmployee"
        :key="contract.id"
        class="rounded-xl border bg-card p-5 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start gap-4">
          <!-- Icon -->
          <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-purple-100 dark:bg-purple-900/30">
            <FileSignature class="h-6 w-6 text-purple-600 dark:text-purple-400" />
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-semibold">{{ contract.employee?.fullName }}</h3>
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(contract.status))">
                {{ getStatusLabel(contract.status) }}
              </span>
              <span
                v-if="contract.endDate && daysUntilEnd(contract.endDate)! <= 30 && daysUntilEnd(contract.endDate)! > 0"
                class="text-xs px-2 py-1 rounded-full bg-yellow-100 text-yellow-600 dark:bg-yellow-900/30 dark:text-yellow-400"
              >
                <AlertTriangle class="h-3 w-3 inline mr-1" />
                Expire dans {{ daysUntilEnd(contract.endDate) }}j
              </span>
            </div>

            <p class="text-sm text-muted-foreground">
              {{ contract.position }} - {{ contract.department }}
            </p>

            <div class="flex flex-wrap items-center gap-4 mt-2 text-sm text-muted-foreground">
              <span class="px-2 py-0.5 rounded bg-muted text-foreground text-xs">
                {{ getContractTypeLabel(contract.type) }}
              </span>
              <span class="flex items-center gap-1">
                <Calendar class="h-4 w-4" />
                {{ formatDate(contract.startDate) }}
                <template v-if="contract.endDate">
                  - {{ formatDate(contract.endDate) }}
                </template>
              </span>
              <span class="flex items-center gap-1">
                <Clock class="h-4 w-4" />
                {{ contract.hoursPerWeek }}h/semaine
              </span>
            </div>
          </div>

          <!-- Salary -->
          <div class="text-right">
            <p class="text-lg font-bold">{{ formatCurrency(contract.salary) }}</p>
            <p class="text-xs text-muted-foreground">/an</p>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-1">
            <Button variant="ghost" size="icon-sm" title="Voir">
              <Eye class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Telecharger">
              <Download class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
