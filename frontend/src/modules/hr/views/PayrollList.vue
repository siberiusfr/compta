<script setup lang="ts">
import { useHr } from '../composables/useHr'
import { Button } from '@/components/ui/button'
import {
  Wallet,
  Plus,
  Search,
  Filter,
  Download,
  Check,
  Send
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  payrollEntries,
  totalPayroll,
  isLoading,
  formatCurrency,
  getStatusColor,
  getStatusLabel
} = useHr()

const formatPeriod = (period: string): string => {
  const parts = period.split('-')
  const year = parts[0] ?? '2024'
  const month = parts[1] ?? '01'
  const date = new Date(parseInt(year), parseInt(month) - 1)
  return new Intl.DateTimeFormat('fr-FR', { month: 'long', year: 'numeric' }).format(date)
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Wallet class="h-6 w-6" />
          Paie
        </h1>
        <p class="text-muted-foreground">
          Gestion de la paie
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Generer la paie
      </Button>
    </div>

    <!-- Stats -->
    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-xl border bg-card p-4">
        <p class="text-sm text-muted-foreground">Total paie (mois en cours)</p>
        <p class="text-2xl font-bold">{{ formatCurrency(totalPayroll) }}</p>
      </div>
      <div class="rounded-xl border bg-card p-4">
        <p class="text-sm text-muted-foreground">Bulletins generes</p>
        <p class="text-2xl font-bold">{{ payrollEntries.length }}</p>
      </div>
      <div class="rounded-xl border bg-card p-4">
        <p class="text-sm text-muted-foreground">A valider</p>
        <p class="text-2xl font-bold text-yellow-600">
          {{ payrollEntries.filter(p => p.status === 'draft').length }}
        </p>
      </div>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Rechercher..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <Button variant="outline" size="icon">
        <Filter class="h-4 w-4" />
      </Button>
    </div>

    <!-- Payroll List -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="rounded-xl border bg-card overflow-hidden">
      <table class="w-full">
        <thead class="bg-muted/50">
          <tr>
            <th class="text-left p-4 font-medium">Employe</th>
            <th class="text-left p-4 font-medium">Periode</th>
            <th class="text-right p-4 font-medium">Brut</th>
            <th class="text-right p-4 font-medium">Cotisations</th>
            <th class="text-right p-4 font-medium">Net</th>
            <th class="text-center p-4 font-medium">Statut</th>
            <th class="text-right p-4 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr
            v-for="entry in payrollEntries"
            :key="entry.id"
            class="hover:bg-muted/30 transition-colors"
          >
            <td class="p-4 font-medium">{{ entry.employeeName }}</td>
            <td class="p-4 text-muted-foreground capitalize">{{ formatPeriod(entry.period) }}</td>
            <td class="p-4 text-right">{{ formatCurrency(entry.grossSalary) }}</td>
            <td class="p-4 text-right text-red-600">-{{ formatCurrency(entry.deductions + entry.taxes) }}</td>
            <td class="p-4 text-right font-medium">{{ formatCurrency(entry.netSalary) }}</td>
            <td class="p-4 text-center">
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(entry.status))">
                {{ getStatusLabel(entry.status) }}
              </span>
            </td>
            <td class="p-4">
              <div class="flex items-center justify-end gap-1">
                <Button
                  v-if="entry.status === 'draft'"
                  variant="ghost"
                  size="icon-sm"
                  title="Valider"
                >
                  <Check class="h-4 w-4" />
                </Button>
                <Button
                  v-if="entry.status === 'validated'"
                  variant="ghost"
                  size="icon-sm"
                  title="Envoyer"
                >
                  <Send class="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="icon-sm" title="Telecharger">
                  <Download class="h-4 w-4" />
                </Button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
