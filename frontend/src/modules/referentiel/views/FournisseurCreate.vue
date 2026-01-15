<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useReferentiel } from '../composables/useReferentiel'
import { Button } from '@/components/ui/button'
import { Truck, ArrowLeft, Save } from 'lucide-vue-next'
import type { FournisseurRequest } from '../types/referentiel.types'

const router = useRouter()
const { companyId, useCreateFournisseur, invalidateFournisseurs } = useReferentiel()

const isSubmitting = ref(false)
const error = ref<string | null>(null)

const form = ref<FournisseurRequest>({
  code: '',
  raisonSociale: '',
  matriculeFiscal: '',
  adresse: '',
  ville: '',
  codePostal: '',
  telephone: '',
  email: '',
  actif: true,
})

const createMutation = useCreateFournisseur({
  mutation: {
    onSuccess: () => {
      invalidateFournisseurs()
      router.push({ name: 'referentiel-fournisseurs-list' })
    },
    onError: (err: any) => {
      error.value = err.message || 'Une erreur est survenue'
      isSubmitting.value = false
    },
  },
})

async function handleSubmit() {
  error.value = null
  isSubmitting.value = true

  createMutation.mutate({
    companyId: companyId.value,
    data: form.value,
  })
}

function handleCancel() {
  router.push({ name: 'referentiel-fournisseurs-list' })
}
</script>

<template>
  <div class="space-y-6 max-w-3xl">
    <!-- Header -->
    <div class="flex items-center gap-4">
      <Button
        variant="ghost"
        size="icon"
        @click="handleCancel"
      >
        <ArrowLeft class="h-5 w-5" />
      </Button>
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Truck class="h-6 w-6" />
          Nouveau fournisseur
        </h1>
        <p class="text-muted-foreground">Ajoutez un nouveau fournisseur</p>
      </div>
    </div>

    <!-- Error -->
    <div
      v-if="error"
      class="rounded-lg border border-destructive bg-destructive/10 p-4 text-destructive"
    >
      {{ error }}
    </div>

    <!-- Form -->
    <form
      @submit.prevent="handleSubmit"
      class="space-y-6"
    >
      <!-- Informations générales -->
      <div class="rounded-xl border bg-card p-6">
        <h2 class="text-lg font-semibold mb-4">Informations générales</h2>
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="text-sm font-medium mb-1 block">Code *</label>
            <input
              v-model="form.code"
              type="text"
              required
              maxlength="50"
              placeholder="FRN001"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Matricule fiscal</label>
            <input
              v-model="form.matriculeFiscal"
              type="text"
              maxlength="50"
              placeholder="Numéro de matricule fiscal"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div class="md:col-span-2">
            <label class="text-sm font-medium mb-1 block">Raison sociale *</label>
            <input
              v-model="form.raisonSociale"
              type="text"
              required
              maxlength="255"
              placeholder="Nom de l'entreprise"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>
      </div>

      <!-- Coordonnées -->
      <div class="rounded-xl border bg-card p-6">
        <h2 class="text-lg font-semibold mb-4">Coordonnées</h2>
        <div class="grid gap-4 md:grid-cols-2">
          <div class="md:col-span-2">
            <label class="text-sm font-medium mb-1 block">Adresse</label>
            <input
              v-model="form.adresse"
              type="text"
              placeholder="Adresse complète"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Code postal</label>
            <input
              v-model="form.codePostal"
              type="text"
              maxlength="10"
              placeholder="75001"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Ville</label>
            <input
              v-model="form.ville"
              type="text"
              maxlength="100"
              placeholder="Paris"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Téléphone</label>
            <input
              v-model="form.telephone"
              type="tel"
              maxlength="20"
              placeholder="+33 1 23 45 67 89"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Email</label>
            <input
              v-model="form.email"
              type="email"
              maxlength="100"
              placeholder="contact@fournisseur.com"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>
      </div>

      <!-- Statut -->
      <div class="rounded-xl border bg-card p-6">
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            v-model="form.actif"
            type="checkbox"
            class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
          />
          <span class="text-sm font-medium">Fournisseur actif</span>
        </label>
      </div>

      <!-- Actions -->
      <div class="flex items-center justify-end gap-4">
        <Button
          variant="outline"
          type="button"
          @click="handleCancel"
        >
          Annuler
        </Button>
        <Button
          type="submit"
          :disabled="isSubmitting"
        >
          <Save class="h-4 w-4 mr-2" />
          {{ isSubmitting ? 'Enregistrement...' : 'Enregistrer' }}
        </Button>
      </div>
    </form>
  </div>
</template>
