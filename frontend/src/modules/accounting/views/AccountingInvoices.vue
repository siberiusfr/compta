<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAccounting } from '../composables/useAccounting'
import { formatCurrency, formatDate } from '@/shared/utils/format'
import { Plus, Search } from 'lucide-vue-next'

const { invoices, pendingInvoices, paidInvoices, totalRevenue } = useAccounting()

const searchQuery = ref('')
const statusFilter = ref<string | null>(null)

const filteredInvoices = computed(() => {
  return invoices.value.filter((invoice) => {
    const matchesSearch = !searchQuery.value ||
      invoice.number.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      invoice.customerName.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesStatus = !statusFilter.value || invoice.status === statusFilter.value
    return matchesSearch && matchesStatus
  })
})

function getStatusColor(status: string) {
  switch (status) {
    case 'draft': return 'bg-gray-100 text-gray-600'
    case 'sent': return 'bg-blue-100 text-blue-600'
    case 'paid': return 'bg-green-100 text-green-600'
    case 'overdue': return 'bg-red-100 text-red-600'
    case 'cancelled': return 'bg-gray-100 text-gray-400'
    default: return 'bg-gray-100 text-gray-600'
  }
}

function getStatusLabel(status: string) {
  switch (status) {
    case 'draft': return 'Brouillon'
    case 'sent': return 'Envoyée'
    case 'paid': return 'Payée'
    case 'overdue': return 'En retard'
    case 'cancelled': return 'Annulée'
    default: return status
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Factures</h1>
        <p class="text-gray-600 mt-1">Total: {{ formatCurrency(totalRevenue) }} | En attente: {{ pendingInvoices.length }} | Payées: {{ paidInvoices.length }}</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
        <Plus :size="18" />
        Nouvelle facture
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-1 relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="18" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Rechercher une facture..."
            class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
        </div>

        <select
          v-model="statusFilter"
          class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
        >
          <option :value="null">Tous les statuts</option>
          <option value="draft">Brouillon</option>
          <option value="sent">Envoyée</option>
          <option value="paid">Payée</option>
          <option value="overdue">En retard</option>
          <option value="cancelled">Annulée</option>
        </select>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Numéro</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Client</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Échéance</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Montant HT</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Montant TTC</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="invoice in filteredInvoices" :key="invoice.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm font-medium text-gray-900">{{ invoice.number }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ invoice.customerName }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ formatDate(invoice.date) }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ formatDate(invoice.dueDate) }}</td>
            <td class="px-6 py-4">
              <span :class="['px-2 py-1 rounded text-xs font-medium', getStatusColor(invoice.status)]">
                {{ getStatusLabel(invoice.status) }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-right text-gray-900">{{ formatCurrency(invoice.amount) }}</td>
            <td class="px-6 py-4 text-sm text-right font-medium text-gray-900">{{ formatCurrency(invoice.total) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
