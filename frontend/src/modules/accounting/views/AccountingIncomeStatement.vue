<script setup lang="ts">
import { computed } from 'vue'
import { useAccounting } from '../composables/useAccounting'
import { formatCurrency } from '@/shared/utils/format'
import { TrendingUp, TrendingDown } from 'lucide-vue-next'

const { ledgerAccounts } = useAccounting()

const totalRevenue = computed(() =>
  Math.abs(ledgerAccounts.value.filter((a) => a.type === 'revenue').reduce((sum, a) => sum + a.balance, 0))
)

const totalExpenses = computed(() =>
  ledgerAccounts.value.filter((a) => a.type === 'expense').reduce((sum, a) => sum + a.balance, 0)
)

const netProfit = computed(() => totalRevenue.value - totalExpenses.value)
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Compte de résultat</h1>
      <p class="text-gray-600 mt-1">Produits et charges</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Code</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Compte</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Montant</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr class="bg-green-50">
            <td colspan="3" class="px-6 py-3">
              <h3 class="font-bold text-gray-900">Produits</h3>
            </td>
          </tr>
          <tr v-for="account in ledgerAccounts.filter(a => a.type === 'revenue')" :key="account.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-600">{{ account.code }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ account.name }}</td>
            <td class="px-6 py-4 text-sm text-right text-green-600 font-medium">{{ formatCurrency(Math.abs(account.balance)) }}</td>
          </tr>
          <tr class="bg-green-100">
            <td colspan="2" class="px-6 py-4 font-bold text-gray-900">Total Produits</td>
            <td class="px-6 py-4 text-sm text-right font-bold text-green-700">{{ formatCurrency(totalRevenue) }}</td>
          </tr>

          <tr class="bg-red-50">
            <td colspan="3" class="px-6 py-3">
              <h3 class="font-bold text-gray-900">Charges</h3>
            </td>
          </tr>
          <tr v-for="account in ledgerAccounts.filter(a => a.type === 'expense')" :key="account.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-600">{{ account.code }}</td>
            <td class="px-6 py-4 text-sm text-gray-900">{{ account.name }}</td>
            <td class="px-6 py-4 text-sm text-right text-red-600 font-medium">{{ formatCurrency(account.balance) }}</td>
          </tr>
          <tr class="bg-red-100">
            <td colspan="2" class="px-6 py-4 font-bold text-gray-900">Total Charges</td>
            <td class="px-6 py-4 text-sm text-right font-bold text-red-700">{{ formatCurrency(totalExpenses) }}</td>
          </tr>

          <tr :class="netProfit >= 0 ? 'bg-green-100' : 'bg-red-100'">
            <td colspan="2" class="px-6 py-4 font-bold text-gray-900 flex items-center gap-2">
              <component :is="netProfit >= 0 ? TrendingUp : TrendingDown" :size="20" />
              Résultat Net
            </td>
            <td class="px-6 py-4 text-sm text-right font-bold" :class="netProfit >= 0 ? 'text-green-700' : 'text-red-700'">
              {{ formatCurrency(netProfit) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
