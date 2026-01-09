<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui/button'
import { Building2, ArrowLeft, Save } from 'lucide-vue-next'

const router = useRouter()

const form = ref({
  name: '',
  legalName: '',
  type: 'sarl',
  siret: '',
  siren: '',
  vatNumber: '',
  capital: '',
  address: {
    street: '',
    city: '',
    postalCode: '',
    country: 'France'
  },
  phone: '',
  email: '',
  website: '',
  fiscalYearEnd: '12-31'
})

const companyTypes = [
  { value: 'sarl', label: 'SARL' },
  { value: 'sas', label: 'SAS' },
  { value: 'sa', label: 'SA' },
  { value: 'eurl', label: 'EURL' },
  { value: 'ei', label: 'EI' },
  { value: 'auto-entrepreneur', label: 'Auto-entrepreneur' }
]

const handleSubmit = () => {
  console.log('Submit:', form.value)
  router.push('/companies')
}
</script>

<template>
  <div class="space-y-6 max-w-3xl">
    <!-- Header -->
    <div class="flex items-center gap-4">
      <Button variant="ghost" size="icon" @click="router.back()">
        <ArrowLeft class="h-5 w-5" />
      </Button>
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Building2 class="h-6 w-6" />
          Nouvelle entreprise
        </h1>
        <p class="text-muted-foreground">
          Creer une nouvelle entreprise
        </p>
      </div>
    </div>

    <!-- Form -->
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- General Info -->
      <div class="rounded-xl border bg-card p-6">
        <h2 class="text-lg font-semibold mb-4">Informations generales</h2>
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="text-sm font-medium mb-1 block">Nom commercial *</label>
            <input
              v-model="form.name"
              type="text"
              required
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Raison sociale *</label>
            <input
              v-model="form.legalName"
              type="text"
              required
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Forme juridique *</label>
            <select
              v-model="form.type"
              required
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option v-for="type in companyTypes" :key="type.value" :value="type.value">
                {{ type.label }}
              </option>
            </select>
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Capital social</label>
            <input
              v-model="form.capital"
              type="number"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>
      </div>

      <!-- Legal Info -->
      <div class="rounded-xl border bg-card p-6">
        <h2 class="text-lg font-semibold mb-4">Informations legales</h2>
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="text-sm font-medium mb-1 block">SIRET *</label>
            <input
              v-model="form.siret"
              type="text"
              required
              maxlength="14"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">SIREN</label>
            <input
              v-model="form.siren"
              type="text"
              maxlength="9"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Numero TVA</label>
            <input
              v-model="form.vatNumber"
              type="text"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Cloture exercice</label>
            <input
              v-model="form.fiscalYearEnd"
              type="text"
              placeholder="MM-DD"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>
      </div>

      <!-- Address -->
      <div class="rounded-xl border bg-card p-6">
        <h2 class="text-lg font-semibold mb-4">Adresse</h2>
        <div class="grid gap-4">
          <div>
            <label class="text-sm font-medium mb-1 block">Rue *</label>
            <input
              v-model="form.address.street"
              type="text"
              required
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div class="grid gap-4 md:grid-cols-3">
            <div>
              <label class="text-sm font-medium mb-1 block">Code postal *</label>
              <input
                v-model="form.address.postalCode"
                type="text"
                required
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label class="text-sm font-medium mb-1 block">Ville *</label>
              <input
                v-model="form.address.city"
                type="text"
                required
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label class="text-sm font-medium mb-1 block">Pays *</label>
              <input
                v-model="form.address.country"
                type="text"
                required
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Contact -->
      <div class="rounded-xl border bg-card p-6">
        <h2 class="text-lg font-semibold mb-4">Contact</h2>
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="text-sm font-medium mb-1 block">Email</label>
            <input
              v-model="form.email"
              type="email"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Telephone</label>
            <input
              v-model="form.phone"
              type="tel"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div class="md:col-span-2">
            <label class="text-sm font-medium mb-1 block">Site web</label>
            <input
              v-model="form.website"
              type="url"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center justify-end gap-4">
        <Button variant="outline" type="button" @click="router.back()">
          Annuler
        </Button>
        <Button type="submit">
          <Save class="h-4 w-4 mr-2" />
          Creer l'entreprise
        </Button>
      </div>
    </form>
  </div>
</template>
