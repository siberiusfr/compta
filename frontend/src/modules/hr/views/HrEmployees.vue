<script setup lang="ts">
import { ref, computed } from 'vue'
import { useHr } from '../composables/useHr'
import { Users, Plus, Search, Mail, Phone, MapPin } from 'lucide-vue-next'

const { employees, activeEmployees } = useHr()
const searchQuery = ref('')

const filteredEmployees = computed(() => {
  if (!searchQuery.value) return employees.value
  const query = searchQuery.value.toLowerCase()
  return employees.value.filter((e) =>
    e.firstName.toLowerCase().includes(query) ||
    e.lastName.toLowerCase().includes(query) ||
    e.email.toLowerCase().includes(query)
  )
})

function getStatusColor(status: string) {
  switch (status) {
    case 'active': return 'bg-green-100 text-green-600'
    case 'on_leave': return 'bg-yellow-100 text-yellow-600'
    case 'terminated': return 'bg-red-100 text-red-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

function getStatusLabel(status: string) {
  switch (status) {
    case 'active': return 'Actif'
    case 'on_leave': return 'En congé'
    case 'terminated': return 'Terminé'
    default: return status
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Employés</h1>
        <p class="text-gray-600 mt-1">{{ activeEmployees.length }} employé(s) actif(s)</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
        <Plus :size="18" />
        Nouvel employé
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div class="relative">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="18" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher un employé..."
          class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="employee in filteredEmployees"
        :key="employee.id"
        class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
            <Users :size="24" class="text-blue-600" />
          </div>
          <span
            :class="[
              'px-2 py-1 rounded text-xs font-medium',
              getStatusColor(employee.status)
            ]"
          >
            {{ getStatusLabel(employee.status) }}
          </span>
        </div>

        <h3 class="font-semibold text-gray-900">{{ employee.firstName }} {{ employee.lastName }}</h3>
        <p class="text-sm text-gray-600 mt-1">{{ employee.position }}</p>
        
        <div class="mt-4 space-y-2 text-sm text-gray-600">
          <div class="flex items-center gap-2">
            <Mail :size="14" class="text-gray-400" />
            <span>{{ employee.email }}</span>
          </div>
          <div class="flex items-center gap-2">
            <Phone :size="14" class="text-gray-400" />
            <span>{{ employee.phone }}</span>
          </div>
          <div class="flex items-center gap-2">
            <MapPin :size="14" class="text-gray-400" />
            <span>{{ employee.department }}</span>
          </div>
        </div>

        <div class="mt-4 pt-4 border-t border-gray-100">
          <div class="flex justify-between text-sm">
            <span class="text-gray-500">Contrat</span>
            <span class="font-medium text-gray-900">{{ employee.contractType }}</span>
          </div>
          <div class="flex justify-between text-sm mt-2">
            <span class="text-gray-500">Salaire</span>
            <span class="font-medium text-gray-900">{{ new Intl.NumberFormat('fr-FR').format(employee.salary) }} €/an</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
