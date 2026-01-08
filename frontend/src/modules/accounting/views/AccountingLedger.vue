<script setup lang="ts">
import { computed } from 'vue'
import { useAccounting } from '../composables/useAccounting'
import { formatCurrency } from '@/shared/utils/format'

const { ledgerAccounts } = useAccounting()

const safeLedgerAccounts = computed(() => {
  if (!ledgerAccounts.value || !Array.isArray(ledgerAccounts.value)) {
    return []
  }
  return ledgerAccounts.value
})

function getTypeColor(type: string) {
  switch (type) {
    case 'asset': return 'bg-blue-100 text-blue-600'
    case 'liability': return 'bg-red-100 text-red-600'
    case 'equity': return 'bg-purple-100 text-purple-600'
    case 'revenue': return 'bg-green-100 text-green-600'
    case 'expense': return 'bg-orange-100 text-orange-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

function getTypeLabel(type: string) {
  switch (type) {
    case 'asset': return 'Actif'
    case 'liability': return 'Passif'
    case 'equity': return 'Capitaux propres'
    case 'revenue': return 'Produits'
    case 'expense': return 'Charges'
    default: return type
  }
}
</script>

<template>
  <div v-if="!ledgerAccounts" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des comptes...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Grand livre</h1>
      <p class="text-gray-600 mt-1">Liste des comptes comptables</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Code</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nom</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Solde</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="account in safeLedgerAccounts" :key="account.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-900 font-medium">{{ account.code }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ account.name }}</td>
            <td>
              <span :class="['px-2 py-1 rounded text-xs font-medium', getTypeColor(account.type)]">
                {{ getTypeLabel(account.type) }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-right font-medium" :class="account.balance >= 0 ? 'text-green-600' : 'text-red-600'">
              {{ formatCurrency(Math.abs(account.balance)) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
