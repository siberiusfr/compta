<script setup lang="ts">
import { useCompanies } from '../composables/useCompanies'
import { Building2, Plus, MapPin } from 'lucide-vue-next'

const { companies } = useCompanies()
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Toutes les entreprises</h1>
        <p class="text-gray-600 mt-1">{{ companies.length }} entreprise(s)</p>
      </div>
      <RouterLink
        to="/companies/create"
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        <Plus :size="18" />
        Nouvelle entreprise
      </RouterLink>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="company in companies"
        :key="company.id"
        class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Building2 :size="24" class="text-blue-600" />
          </div>
          <span
            :class="[
              'px-2 py-1 rounded text-xs font-medium',
              company.active ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-600'
            ]"
          >
            {{ company.active ? 'Active' : 'Inactive' }}
          </span>
        </div>

        <h3 class="font-semibold text-gray-900 mb-2">{{ company.name }}</h3>
        
        <div class="space-y-2 text-sm text-gray-600">
          <div class="flex items-center gap-2">
            <MapPin :size="16" class="text-gray-400" />
            <span>{{ company.city }} ({{ company.postalCode }})</span>
          </div>
          <p>SIRET: {{ company.siret }}</p>
          <p>TVA: {{ company.vatNumber }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
