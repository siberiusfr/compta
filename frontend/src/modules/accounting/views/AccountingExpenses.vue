<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAccounting } from '../composables/useAccounting'
import { formatCurrency, formatDate } from '@/shared/utils/format'
import { Plus, Search } from 'lucide-vue-next'

const { expenses } = useAccounting()

const safeExpenses = computed(() => {
  if (!expenses.value || !Array.isArray(expenses.value)) {
    return []
  }
  return expenses.value
})

const searchQuery = ref('')
const statusFilter = ref<string | null>(null)

const filteredExpenses = computed(() => {
  return safeExpenses.value.filter(expense => {
    const matchesSearch = !searchQuery.value ||
      expense.description.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      expense.vendor.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      expense.category.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesStatus = !statusFilter.value || expense.status === statusFilter.value
    return matchesSearch && matchesStatus
  })
})

function getStatusColor(status: string) {
  switch (status) {
    case 'pending': return 'bg-yellow-100 text-yellow-600'
    case 'validated': return 'bg-blue-100 text-blue-600'
    case 'paid': return 'bg-green-100 text-green-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

function getStatusLabel(status: string) {
  switch (status) {
    case 'pending': return 'En attente'
    case 'validated': return 'Validée'
    case 'paid': return 'Payée'
    default: return status
  }
}
</script>

<template>
  <div v-if="!expenses" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des dépenses...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Dépenses</h1>
        <p class="text-gray-600 mt-1">Gérez vos dépenses et justificatifs</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
        <Plus :size="18" />
        Nouvelle dépense
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-1 relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="18" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Rechercher une dépense..."
            class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
        </div>

        <select
          v-model="statusFilter"
          class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
        >
          <option :value="null">Tous les statuts</option>
          <option value="pending">En attente</option>
          <option value="validated">Validée</option>
          <option value="paid">Payée</option>
        </select>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Catégorie</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fournisseur</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Montant HT</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">TVA</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Total TTC</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="expense in filteredExpenses" :key="expense.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-600">{{ formatDate(expense.date) }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ expense.description }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ expense.category }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ expense.vendor }}</td>
            <td class="px-6 py-4">
              <span :class="['px-2 py-1 rounded text-xs font-medium', getStatusColor(expense.status)]">
                {{ getStatusLabel(expense.status) }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-right text-gray-900">{{ formatCurrency(expense.amount) }}</td>
            <td class="px-6 py-4 text-sm text-right text-gray-600">{{ formatCurrency(expense.tax) }}</td>
            <td class="px-6 py-4 text-sm text-right font-medium text-gray-900">{{ formatCurrency(expense.amount + expense.tax) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
