<script setup lang="ts">
import { computed } from 'vue'
import { useAccounting } from '../composables/useAccounting'
import { formatCurrency } from '@/shared/utils/format'
import { Scale } from 'lucide-vue-next'

const { ledgerAccounts } = useAccounting()

const safeLedgerAccounts = computed(() => {
  if (!ledgerAccounts || !Array.isArray(ledgerAccounts)) {
    return []
  }
  return ledgerAccounts
})

const assetAccounts = computed(() => safeLedgerAccounts.value.filter(a => a.type === 'asset'))
const liabilityAccounts = computed(() => safeLedgerAccounts.value.filter(a => a.type === 'liability'))
const equityAccounts = computed(() => safeLedgerAccounts.value.filter(a => a.type === 'equity'))

const totalAssets = computed(() =>
  assetAccounts.value.reduce((sum, a) => sum + a.balance, 0)
)

const totalLiabilities = computed(() =>
  Math.abs(liabilityAccounts.value.reduce((sum, a) => sum + a.balance, 0))
)

const totalEquity = computed(() =>
  equityAccounts.value.reduce((sum, a) => sum + a.balance, 0)
)

const totalLiabilitiesEquity = computed(() => totalLiabilities.value + totalEquity.value)
</script>

<template>
  <div v-if="!ledgerAccounts" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des données...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Bilan</h1>
      <p class="text-gray-600 mt-1">État financier de la société</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <h2 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Scale :size="20" />
          Actif
        </h2>

        <div class="space-y-3">
          <div
            v-for="account in assetAccounts"
            :key="account.id"
            class="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
          >
            <div>
              <p class="font-medium text-gray-900">{{ account.name }}</p>
              <p class="text-xs text-gray-500">{{ account.code }}</p>
            </div>
            <p class="font-semibold text-gray-900">{{ formatCurrency(account.balance) }}</p>
          </div>
        </div>

        <div class="flex justify-between items-center p-4 bg-blue-50 rounded-lg border-t-2 border-blue-600">
          <span class="font-bold text-gray-900">Total Actif</span>
          <span class="font-bold text-gray-900 text-lg">{{ formatCurrency(totalAssets) }}</span>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Passif et Capitaux propres</h2>

        <div class="space-y-3">
          <div>
            <p class="text-sm font-medium text-gray-700 mb-2">Passif</p>
            <div
              v-for="account in liabilityAccounts"
              :key="account.id"
              class="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
            >
              <div>
                <p class="font-medium text-gray-900">{{ account.name }}</p>
                <p class="text-xs text-gray-500">{{ account.code }}</p>
              </div>
              <p class="font-semibold text-gray-900">{{ formatCurrency(Math.abs(account.balance)) }}</p>
            </div>
            <div class="flex justify-between items-center p-3 bg-red-50 rounded-lg mt-2">
              <span class="font-medium text-gray-900">Total Passif</span>
              <span class="font-semibold text-gray-900">{{ formatCurrency(totalLiabilities) }}</span>
            </div>
          </div>

          <div>
            <p class="text-sm font-medium text-gray-700 mb-2">Capitaux propres</p>
            <div
              v-for="account in equityAccounts"
              :key="account.id"
              class="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
            >
              <div>
                <p class="font-medium text-gray-900">{{ account.name }}</p>
                <p class="text-xs text-gray-500">{{ account.code }}</p>
              </div>
              <p class="font-semibold text-gray-900">{{ formatCurrency(account.balance) }}</p>
            </div>
            <div class="flex justify-between items-center p-3 bg-purple-50 rounded-lg mt-2">
              <span class="font-medium text-gray-900">Total Capitaux propres</span>
              <span class="font-semibold text-gray-900">{{ formatCurrency(totalEquity) }}</span>
            </div>
          </div>

          <div class="flex justify-between items-center p-4 bg-red-50 rounded-lg border-t-2 border-red-600">
            <span class="font-bold text-gray-900">Total Passif + Capitaux propres</span>
            <span class="font-bold text-gray-900 text-lg">{{ formatCurrency(totalLiabilitiesEquity) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
