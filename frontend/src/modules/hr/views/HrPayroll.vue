<script setup lang="ts">
import { useHr } from '../composables/useHr'
import { FileText } from 'lucide-vue-next'

const { payroll } = useHr()

function getStatusColor(status: string) {
  switch (status) {
    case 'draft': return 'bg-gray-100 text-gray-600'
    case 'validated': return 'bg-blue-100 text-blue-600'
    case 'paid': return 'bg-green-100 text-green-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

function getStatusLabel(status: string) {
  switch (status) {
    case 'draft': return 'Brouillon'
    case 'validated': return 'Validé'
    case 'paid': return 'Payé'
    default: return status
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Paie</h1>
      <p class="text-gray-600 mt-1">Gérez les fiches de paie</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <div class="divide-y divide-gray-100">
        <div
          v-for="pay in payroll"
          :key="pay.id"
          class="p-6 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-start gap-4">
            <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
              <FileText :size="20" class="text-blue-600" />
            </div>

            <div class="flex-1">
              <div class="flex items-start justify-between">
                <div>
                  <h3 class="font-semibold text-gray-900">{{ pay.employeeName }}</h3>
                  <p class="text-sm text-gray-600 mt-1">{{ pay.month }}</p>
                </div>
                <span
                  :class="[
                    'px-2 py-1 rounded text-xs font-medium',
                    getStatusColor(pay.status)
                  ]"
                >
                  {{ getStatusLabel(pay.status) }}
                </span>
              </div>

              <div class="mt-4 grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                <div>
                  <p class="text-gray-500">Salaire brut</p>
                  <p class="font-medium text-gray-900">{{ new Intl.NumberFormat('fr-FR').format(pay.grossSalary) }} €</p>
                </div>
                <div>
                  <p class="text-gray-500">Salaire net</p>
                  <p class="font-medium text-gray-900">{{ new Intl.NumberFormat('fr-FR').format(pay.netSalary) }} €</p>
                </div>
                <div>
                  <p class="text-gray-500">Impôts</p>
                  <p class="font-medium text-gray-900">{{ new Intl.NumberFormat('fr-FR').format(pay.taxes) }} €</p>
                </div>
                <div>
                  <p class="text-gray-500">Charges sociales</p>
                  <p class="font-medium text-gray-900">{{ new Intl.NumberFormat('fr-FR').format(pay.socialContributions) }} €</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
