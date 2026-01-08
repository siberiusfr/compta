<script setup lang="ts">
import { computed } from 'vue'
import { useAccounting } from '../composables/useAccounting'
import { formatCurrency } from '@/shared/utils/format'

const { journalEntries } = useAccounting()

const safeJournalEntries = computed(() => {
  if (!journalEntries.value || !Array.isArray(journalEntries.value)) {
    return []
  }
  return journalEntries.value
})
</script>

<template>
  <div v-if="!journalEntries" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des écritures...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Journal</h1>
      <p class="text-gray-600 mt-1">Historique des écritures comptables</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Référence</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Compte</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Débit</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Crédit</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="entry in safeJournalEntries" :key="entry.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-600">{{ entry.date.toLocaleDateString('fr-FR') }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ entry.reference }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ entry.description }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ entry.accountId }} - {{ entry.accountName }}</td>
            <td class="px-6 py-4 text-sm text-green-600 font-medium">{{ formatCurrency(entry.debit) }}</td>
            <td class="px-6 py-4 text-sm text-red-600 font-medium">{{ formatCurrency(entry.credit) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
