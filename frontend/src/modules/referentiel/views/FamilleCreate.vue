<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useReferentiel } from '../composables/useReferentiel'
import { Button } from '@/components/ui/button'
import { Folder, ArrowLeft, Save } from 'lucide-vue-next'
import type { FamilleProduitRequest } from '../types/referentiel.types'

const router = useRouter()
const { companyId, useCreateFamille, invalidateFamilles } = useReferentiel()

const isSubmitting = ref(false)
const error = ref<string | null>(null)

const form = ref<FamilleProduitRequest>({
  code: '',
  libelle: '',
  description: '',
  actif: true,
})

const createMutation = useCreateFamille({
  mutation: {
    onSuccess: () => {
      invalidateFamilles()
      router.push({ name: 'referentiel-familles-list' })
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
  router.push({ name: 'referentiel-familles-list' })
}
</script>

<template>
  <div class="space-y-6 max-w-2xl">
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
          <Folder class="h-6 w-6" />
          Nouvelle famille de produits
        </h1>
        <p class="text-muted-foreground">Créez une nouvelle catégorie pour vos produits</p>
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
              placeholder="FAM001"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Libellé *</label>
            <input
              v-model="form.libelle"
              type="text"
              required
              maxlength="255"
              placeholder="Fournitures de bureau"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div class="md:col-span-2">
            <label class="text-sm font-medium mb-1 block">Description</label>
            <textarea
              v-model="form.description"
              rows="3"
              placeholder="Description de la famille de produits..."
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring resize-none"
            />
          </div>
          <div class="md:col-span-2">
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                v-model="form.actif"
                type="checkbox"
                class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
              />
              <span class="text-sm font-medium">Actif</span>
            </label>
          </div>
        </div>
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
