<script setup lang="ts">
import { useHr } from '../composables/useHr'
import { FileText } from 'lucide-vue-next'

const { contracts } = useHr()

function getStatusColor(status: string) {
  switch (status) {
    case 'active': return 'bg-green-100 text-green-600'
    case 'expired': return 'bg-red-100 text-red-600'
    case 'terminated': return 'bg-gray-100 text-gray-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

function getStatusLabel(status: string) {
  switch (status) {
    case 'active': return 'Actif'
    case 'expired': return 'Expiré'
    case 'terminated': return 'Terminé'
    default: return status
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Contrats</h1>
      <p class="text-gray-600 mt-1">Gérez les contrats des employés</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <div class="divide-y divide-gray-100">
        <div
          v-for="contract in contracts"
          :key="contract.id"
          class="p-6 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-start gap-4">
            <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
              <FileText :size="20" class="text-blue-600" />
            </div>

            <div class="flex-1">
              <div class="flex items-start justify-between">
                <div>
                  <h3 class="font-semibold text-gray-900">{{ contract.employeeName }}</h3>
                  <p class="text-sm text-gray-600 mt-1">{{ contract.type }}</p>
                </div>
                <span
                  :class="[
                    'px-2 py-1 rounded text-xs font-medium',
                    getStatusColor(contract.status)
                  ]"
                >
                  {{ getStatusLabel(contract.status) }}
                </span>
              </div>

              <div class="mt-3 grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                <div>
                  <p class="text-gray-500">Date de début</p>
                  <p class="font-medium text-gray-900">{{ contract.startDate.toLocaleDateString('fr-FR') }}</p>
                </div>
                <div>
                  <p class="text-gray-500">Date de fin</p>
                  <p class="font-medium text-gray-900">{{ contract.endDate ? contract.endDate.toLocaleDateString('fr-FR') : 'Indéterminée' }}</p>
                </div>
                <div>
                  <p class="text-gray-500">Salaire annuel</p>
                  <p class="font-medium text-gray-900">{{ new Intl.NumberFormat('fr-FR').format(contract.salary) }} €</p>
                </div>
                <div>
                  <p class="text-gray-500">Heures/semaine</p>
                  <p class="font-medium text-gray-900">{{ contract.hoursPerWeek }}h</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
