<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useReferentiel } from '../composables/useReferentiel'
import { Button } from '@/components/ui/button'
import { Package, ArrowLeft, Save } from 'lucide-vue-next'
import type { ProduitRequest } from '../types/referentiel.types'
import {
  ProduitRequestTypeArticle,
  ProduitRequestTypeStock,
} from '@/api/referentiel/gen/generated.schemas'

const router = useRouter()
const { companyId, useCreateProduit, useGetAllFamilles, invalidateProduits } = useReferentiel()

const isSubmitting = ref(false)
const error = ref<string | null>(null)

const { data: familles } = useGetAllFamilles(companyId)

const form = ref<ProduitRequest>({
  reference: '',
  designation: '',
  description: '',
  prixAchat: undefined,
  prixVente: undefined,
  tauxTva: 20,
  unite: '',
  typeStock: ProduitRequestTypeStock.STOCKABLE,
  typeArticle: ProduitRequestTypeArticle.PRODUIT,
  familleId: undefined,
  actif: true,
})

const createMutation = useCreateProduit({
  mutation: {
    onSuccess: () => {
      invalidateProduits()
      router.push({ name: 'referentiel-produits-list' })
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
  router.push({ name: 'referentiel-produits-list' })
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
          <Package class="h-6 w-6" />
          Nouveau produit
        </h1>
        <p class="text-muted-foreground">Ajoutez un nouveau produit ou service à votre catalogue</p>
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
            <label class="text-sm font-medium mb-1 block">Référence *</label>
            <input
              v-model="form.reference"
              type="text"
              required
              maxlength="50"
              placeholder="PRD001"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Désignation *</label>
            <input
              v-model="form.designation"
              type="text"
              required
              maxlength="255"
              placeholder="Nom du produit"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div class="md:col-span-2">
            <label class="text-sm font-medium mb-1 block">Description</label>
            <textarea
              v-model="form.description"
              rows="3"
              placeholder="Description du produit..."
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring resize-none"
            />
          </div>
        </div>
      </div>

      <!-- Classification -->
      <div class="rounded-xl border bg-card p-6">
        <h2 class="text-lg font-semibold mb-4">Classification</h2>
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="text-sm font-medium mb-1 block">Type d'article *</label>
            <select
              v-model="form.typeArticle"
              required
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option :value="ProduitRequestTypeArticle.PRODUIT">Produit</option>
              <option :value="ProduitRequestTypeArticle.SERVICE">Service</option>
            </select>
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Type de stock *</label>
            <select
              v-model="form.typeStock"
              required
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option :value="ProduitRequestTypeStock.STOCKABLE">Stockable</option>
              <option :value="ProduitRequestTypeStock.NON_STOCKABLE">Non stockable</option>
            </select>
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Famille</label>
            <select
              v-model="form.familleId"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option :value="undefined">-- Aucune --</option>
              <option
                v-for="famille in familles"
                :key="famille.id"
                :value="famille.id"
              >
                {{ famille.libelle }} ({{ famille.code }})
              </option>
            </select>
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Unité</label>
            <input
              v-model="form.unite"
              type="text"
              maxlength="20"
              placeholder="Unité, pièce, kg..."
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>
      </div>

      <!-- Tarification -->
      <div class="rounded-xl border bg-card p-6">
        <h2 class="text-lg font-semibold mb-4">Tarification</h2>
        <div class="grid gap-4 md:grid-cols-3">
          <div>
            <label class="text-sm font-medium mb-1 block">Prix d'achat HT</label>
            <input
              v-model.number="form.prixAchat"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Prix de vente HT</label>
            <input
              v-model.number="form.prixVente"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
              class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div>
            <label class="text-sm font-medium mb-1 block">Taux TVA (%)</label>
            <input
              v-model.number="form.tauxTva"
              type="number"
              step="0.1"
              min="0"
              max="100"
              placeholder="20"
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
          <span class="text-sm font-medium">Produit actif</span>
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
