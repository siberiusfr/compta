<script setup lang="ts">
import { useAccounting } from '../composables/useAccounting'
import { Button } from '@/components/ui/button'
import {
  Scale,
  Download,
  Calendar
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  trialBalance,
  isLoading,
  formatCurrency,
  getAccountTypeColor
} = useAccounting()

const totals = {
  openingDebit: trialBalance.value.reduce((sum, e) => sum + e.openingDebit, 0),
  openingCredit: trialBalance.value.reduce((sum, e) => sum + e.openingCredit, 0),
  periodDebit: trialBalance.value.reduce((sum, e) => sum + e.periodDebit, 0),
  periodCredit: trialBalance.value.reduce((sum, e) => sum + e.periodCredit, 0),
  closingDebit: trialBalance.value.reduce((sum, e) => sum + e.closingDebit, 0),
  closingCredit: trialBalance.value.reduce((sum, e) => sum + e.closingCredit, 0)
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Scale class="h-6 w-6" />
          Balance generale
        </h1>
        <p class="text-muted-foreground flex items-center gap-2">
          <Calendar class="h-4 w-4" />
          Exercice 2024
        </p>
      </div>
      <Button variant="outline">
        <Download class="h-4 w-4 mr-2" />
        Exporter
      </Button>
    </div>

    <!-- Trial Balance Table -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="rounded-xl border bg-card overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-muted/50">
          <tr>
            <th rowspan="2" class="text-left p-3 font-medium border-r">Compte</th>
            <th rowspan="2" class="text-left p-3 font-medium border-r">Libelle</th>
            <th colspan="2" class="text-center p-2 font-medium border-b border-r">Soldes d'ouverture</th>
            <th colspan="2" class="text-center p-2 font-medium border-b border-r">Mouvements periode</th>
            <th colspan="2" class="text-center p-2 font-medium border-b">Soldes de cloture</th>
          </tr>
          <tr>
            <th class="text-right p-2 font-medium">Debit</th>
            <th class="text-right p-2 font-medium border-r">Credit</th>
            <th class="text-right p-2 font-medium">Debit</th>
            <th class="text-right p-2 font-medium border-r">Credit</th>
            <th class="text-right p-2 font-medium">Debit</th>
            <th class="text-right p-2 font-medium">Credit</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr v-for="entry in trialBalance" :key="entry.accountCode" class="hover:bg-muted/30">
            <td class="p-3 font-mono border-r">{{ entry.accountCode }}</td>
            <td class="p-3 border-r">
              {{ entry.accountName }}
              <span :class="cn('ml-2 text-xs px-1 py-0.5 rounded', getAccountTypeColor(entry.accountType))">
                {{ entry.accountType }}
              </span>
            </td>
            <td class="p-3 text-right">{{ entry.openingDebit > 0 ? formatCurrency(entry.openingDebit) : '' }}</td>
            <td class="p-3 text-right border-r">{{ entry.openingCredit > 0 ? formatCurrency(entry.openingCredit) : '' }}</td>
            <td class="p-3 text-right">{{ entry.periodDebit > 0 ? formatCurrency(entry.periodDebit) : '' }}</td>
            <td class="p-3 text-right border-r">{{ entry.periodCredit > 0 ? formatCurrency(entry.periodCredit) : '' }}</td>
            <td class="p-3 text-right">{{ entry.closingDebit > 0 ? formatCurrency(entry.closingDebit) : '' }}</td>
            <td class="p-3 text-right">{{ entry.closingCredit > 0 ? formatCurrency(entry.closingCredit) : '' }}</td>
          </tr>
        </tbody>
        <tfoot class="bg-muted/50 font-semibold">
          <tr>
            <td colspan="2" class="p-3 text-right border-r">TOTAUX</td>
            <td class="p-3 text-right">{{ formatCurrency(totals.openingDebit) }}</td>
            <td class="p-3 text-right border-r">{{ formatCurrency(totals.openingCredit) }}</td>
            <td class="p-3 text-right">{{ formatCurrency(totals.periodDebit) }}</td>
            <td class="p-3 text-right border-r">{{ formatCurrency(totals.periodCredit) }}</td>
            <td class="p-3 text-right">{{ formatCurrency(totals.closingDebit) }}</td>
            <td class="p-3 text-right">{{ formatCurrency(totals.closingCredit) }}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>
</template>
