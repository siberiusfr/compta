<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useReferentiel } from '../composables/useReferentiel'
import { Button } from '@/components/ui/button'
import { Users, ArrowLeft, Save } from 'lucide-vue-next'
import type { ClientRequest } from '../types/referentiel.types'
import { ClientRequestTypeClient } from '@/api/referentiel/gen/generated.schemas'

const router = useRouter()
const route = useRoute()
const { companyId, useGetClientById, useUpdateClient, invalidateClients } = useReferentiel()

const id = Number(route.params.id)
const isSubmitting = ref(false)
const error = ref<string | null>(null)

const { data: client, isLoading } = useGetClientById(companyId, id)

const form = ref<ClientRequest>({
  code: '',
  raisonSociale: '',
  matriculeFiscal: '',
  adresse: '',
  ville: '',
  codePostal: '',
  telephone: '',
  email: '',
  typeClient: ClientRequestTypeClient.ENTREPRISE,
  actif: true,
})

watch(
  client,
  (newValue) => {
    if (newValue) {
      form.value = {
        code: newValue.code ?? '',
        raisonSociale: newValue.raisonSociale ?? '',
        matriculeFiscal: newValue.matriculeFiscal ?? '',
        adresse: newValue.adresse ?? '',
        ville: newValue.ville ?? '',
        codePostal: newValue.codePostal ?? '',
        telephone: newValue.telephone ?? '',
        email: newValue.email ?? '',
        typeClient:
          (newValue.typeClient as ClientRequestTypeClient) ?? ClientRequestTypeClient.ENTREPRISE,
        actif: newValue.actif ?? true,
      }
    }
  },
  { immediate: true }
)

const updateMutation = useUpdateClient({
  mutation: {
    onSuccess: () => {
      invalidateClients()
      router.push({ name: 'referentiel-clients-list' })
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

  updateMutation.mutate({
    companyId: companyId.value,
    id,
    data: form.value,
  })
}

function handleCancel() {
  router.push({ name: 'referentiel-clients-list' })
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
          <Users class="h-6 w-6" />
          Modifier le client
        </h1>
        <p class="text-muted-foreground">Modifiez les informations du client</p>
      </div>
    </div>

    <!-- Loading -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <template v-else>
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
                placeholder="CLI001"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label class="text-sm font-medium mb-1 block">Type de client *</label>
              <select
                v-model="form.typeClient"
                required
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option :value="ClientRequestTypeClient.ENTREPRISE">Entreprise</option>
                <option :value="ClientRequestTypeClient.PARTICULIER">Particulier</option>
              </select>
            </div>
            <div class="md:col-span-2">
              <label class="text-sm font-medium mb-1 block">Raison sociale / Nom *</label>
              <input
                v-model="form.raisonSociale"
                type="text"
                required
                maxlength="255"
                placeholder="Nom de l'entreprise ou du particulier"
                class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div class="md:col-span-2">
              <label class="text-sm font-medium mb-1 block">Matricule fiscal</label>
              <input
                v-model="form.matriculeFiscal"
                type="text"
                maxlength="50"
                placeholder="Numéro de matricule fiscal"
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
                placeholder="contact@entreprise.com"
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
            <span class="text-sm font-medium">Client actif</span>
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
    </template>
  </div>
</template>
