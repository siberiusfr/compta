<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAccounting } from '../composables/useAccounting'
import { Button } from '@/components/ui/button'
import {
  BookText,
  Download,
  ChevronDown
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  accounts,
  journalEntries,
  isLoading,
  formatCurrency,
  formatDate,
  getAccountTypeColor,
  getAccountTypeLabel
} = useAccounting()

const selectedAccountId = ref<string | null>(null)

const selectedAccount = computed(() =>
  accounts.value.find(a => a.id === selectedAccountId.value)
)

const accountEntries = computed(() => {
  if (!selectedAccountId.value) return []

  const entries: any[] = []
  let runningBalance = 0

  journalEntries.value
    .filter(je => je.status === 'posted')
    .forEach(je => {
      je.lines.forEach(line => {
        if (line.accountId === selectedAccountId.value) {
          runningBalance += line.debit - line.credit
          entries.push({
            date: je.date,
            reference: je.reference,
            description: je.description,
            debit: line.debit,
            credit: line.credit,
            balance: runningBalance
          })
        }
      })
    })

  return entries
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <BookText class="h-6 w-6" />
          Grand livre
        </h1>
        <p class="text-muted-foreground">
          Historique des mouvements par compte
        </p>
      </div>
      <Button variant="outline">
        <Download class="h-4 w-4 mr-2" />
        Exporter
      </Button>
    </div>

    <!-- Account Selector -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-md">
        <select
          v-model="selectedAccountId"
          class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring appearance-none"
        >
          <option :value="null">Selectionnez un compte...</option>
          <option v-for="account in accounts" :key="account.id" :value="account.id">
            {{ account.code }} - {{ account.name }}
          </option>
        </select>
        <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <!-- No Account Selected -->
    <div v-else-if="!selectedAccountId" class="text-center py-12">
      <BookText class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Selectionnez un compte</p>
      <p class="text-muted-foreground">Choisissez un compte pour voir son historique</p>
    </div>

    <!-- Account Ledger -->
    <div v-else class="space-y-4">
      <!-- Account Info -->
      <div class="rounded-xl border bg-card p-4">
        <div class="flex items-center justify-between">
          <div>
            <div class="flex items-center gap-3">
              <span class="font-mono text-lg font-bold">{{ selectedAccount?.code }}</span>
              <h2 class="text-lg font-semibold">{{ selectedAccount?.name }}</h2>
              <span :class="cn('text-xs px-2 py-1 rounded-full', getAccountTypeColor(selectedAccount?.type || ''))">
                {{ getAccountTypeLabel(selectedAccount?.type || '') }}
              </span>
            </div>
          </div>
          <div class="text-right">
            <p class="text-sm text-muted-foreground">Solde actuel</p>
            <p :class="cn('text-2xl font-bold', (selectedAccount?.balance || 0) < 0 ? 'text-red-600' : 'text-green-600')">
              {{ formatCurrency(selectedAccount?.balance || 0) }}
            </p>
          </div>
        </div>
      </div>

      <!-- Entries Table -->
      <div v-if="accountEntries.length > 0" class="rounded-xl border bg-card overflow-hidden">
        <table class="w-full">
          <thead class="bg-muted/50">
            <tr>
              <th class="text-left p-4 font-medium">Date</th>
              <th class="text-left p-4 font-medium">Reference</th>
              <th class="text-left p-4 font-medium">Libelle</th>
              <th class="text-right p-4 font-medium">Debit</th>
              <th class="text-right p-4 font-medium">Credit</th>
              <th class="text-right p-4 font-medium">Solde</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-for="(entry, index) in accountEntries" :key="index" class="hover:bg-muted/30">
              <td class="p-4">{{ formatDate(entry.date) }}</td>
              <td class="p-4 font-mono text-sm">{{ entry.reference }}</td>
              <td class="p-4">{{ entry.description }}</td>
              <td class="p-4 text-right">{{ entry.debit > 0 ? formatCurrency(entry.debit) : '' }}</td>
              <td class="p-4 text-right">{{ entry.credit > 0 ? formatCurrency(entry.credit) : '' }}</td>
              <td :class="cn('p-4 text-right font-medium', entry.balance < 0 ? 'text-red-600' : 'text-green-600')">
                {{ formatCurrency(entry.balance) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="text-center py-8 text-muted-foreground">
        Aucun mouvement sur ce compte
      </div>
    </div>
  </div>
</template>
