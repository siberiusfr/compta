<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCompanies } from '../composables/useCompanies'
import { Save, ArrowLeft } from 'lucide-vue-next'

const router = useRouter()
const { createCompany } = useCompanies()

const companyForm = ref({
  name: '',
  siret: '',
  siren: '',
  address: '',
  city: '',
  postalCode: '',
  country: 'France',
  phone: '',
  email: '',
  vatNumber: ''
})

function handleCreate() {
  createCompany.value({
    ...companyForm.value,
    active: true
  })
  router.push('/companies/all')
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <button
        @click="router.push('/companies/all')"
        class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
      >
        <ArrowLeft :size="20" class="text-gray-500" />
      </button>
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Créer une entreprise</h1>
        <p class="text-gray-600 mt-1">Ajoutez une nouvelle entreprise</p>
      </div>
    </div>

    <div class="max-w-2xl bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Nom de l'entreprise</label>
          <input
            v-model="companyForm.name"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: TechCorp SAS"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">SIRET</label>
          <input
            v-model="companyForm.siret"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: 123 456 789 00012"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">SIREN</label>
          <input
            v-model="companyForm.siren"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: 123 456 789"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Numéro TVA</label>
          <input
            v-model="companyForm.vatNumber"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: FR12345678901"
          />
        </div>

        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-gray-700 mb-1">Adresse</label>
          <input
            v-model="companyForm.address"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: 15 Rue de la Paix"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Code postal</label>
          <input
            v-model="companyForm.postalCode"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: 75002"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Ville</label>
          <input
            v-model="companyForm.city"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: Paris"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Téléphone</label>
          <input
            v-model="companyForm.phone"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: +33 1 23 45 67 89"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input
            v-model="companyForm.email"
            type="email"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            placeholder="ex: contact@techcorp.com"
          />
        </div>
      </div>

      <div class="flex justify-end gap-3 mt-6">
        <button
          @click="router.push('/companies/all')"
          class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
        >
          Annuler
        </button>
        <button
          @click="handleCreate"
          class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Save :size="18" />
          Créer l'entreprise
        </button>
      </div>
    </div>
  </div>
</template>
