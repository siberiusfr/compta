<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useReferentiel } from '../composables/useReferentiel'
import { Button } from '@/components/ui/button'
import {
  Truck,
  Plus,
  Search,
  Edit,
  Trash2,
  CheckCircle2,
  XCircle,
  Mail,
  Phone,
  MapPin,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const router = useRouter()
const {
  companyId,
  useGetAllFournisseurs,
  useDeleteFournisseur,
  invalidateFournisseurs,
  getStatusColor,
  getStatusLabel,
} = useReferentiel()

const searchQuery = ref('')
const deleteConfirmId = ref<number | null>(null)

const { data: fournisseurs, isLoading } = useGetAllFournisseurs(companyId)

const deleteMutation = useDeleteFournisseur({
  mutation: {
    onSuccess: () => {
      invalidateFournisseurs()
      deleteConfirmId.value = null
    },
  },
})

const filteredFournisseurs = computed(() => {
  if (!fournisseurs.value) return []
  if (!searchQuery.value) return fournisseurs.value
  const query = searchQuery.value.toLowerCase()
  return fournisseurs.value.filter(
    (f) =>
      f.code?.toLowerCase().includes(query) ||
      f.raisonSociale?.toLowerCase().includes(query) ||
      f.email?.toLowerCase().includes(query) ||
      f.ville?.toLowerCase().includes(query)
  )
})

function handleCreate() {
  router.push({ name: 'referentiel-fournisseurs-create' })
}

function handleEdit(id: number) {
  router.push({ name: 'referentiel-fournisseurs-edit', params: { id } })
}

function handleDelete(id: number) {
  deleteMutation.mutate({ companyId: companyId.value, id })
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Truck class="h-6 w-6" />
          Fournisseurs
        </h1>
        <p class="text-muted-foreground">Gérez vos fournisseurs et partenaires</p>
      </div>
      <Button @click="handleCreate">
        <Plus class="h-4 w-4 mr-2" />
        Nouveau fournisseur
      </Button>
    </div>

    <!-- Search -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher un fournisseur..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
    </div>

    <!-- Loading -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <!-- List -->
    <div
      v-else
      class="space-y-3"
    >
      <div
        v-for="fournisseur in filteredFournisseurs"
        :key="fournisseur.id"
        class="rounded-xl border bg-card p-4 hover:bg-accent/50 transition-colors"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4 flex-1">
            <div class="p-2 rounded-lg bg-orange-100 dark:bg-orange-900/30">
              <Truck class="h-5 w-5 text-orange-600 dark:text-orange-400" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 flex-wrap">
                <span class="font-mono font-medium text-sm">{{ fournisseur.code }}</span>
                <span class="font-medium truncate">{{ fournisseur.raisonSociale }}</span>
                <span
                  :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(fournisseur.actif))"
                >
                  {{ getStatusLabel(fournisseur.actif) }}
                </span>
              </div>
              <div class="flex items-center gap-4 mt-1 text-xs text-muted-foreground flex-wrap">
                <span
                  v-if="fournisseur.email"
                  class="flex items-center gap-1"
                >
                  <Mail class="h-3 w-3" />
                  {{ fournisseur.email }}
                </span>
                <span
                  v-if="fournisseur.telephone"
                  class="flex items-center gap-1"
                >
                  <Phone class="h-3 w-3" />
                  {{ fournisseur.telephone }}
                </span>
                <span
                  v-if="fournisseur.ville"
                  class="flex items-center gap-1"
                >
                  <MapPin class="h-3 w-3" />
                  {{ fournisseur.ville }}
                </span>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-1">
            <Button
              variant="ghost"
              size="icon-sm"
              @click="handleEdit(fournisseur.id!)"
            >
              <Edit class="h-4 w-4" />
            </Button>
            <Button
              v-if="deleteConfirmId !== fournisseur.id"
              variant="ghost"
              size="icon-sm"
              class="text-destructive hover:text-destructive"
              @click="deleteConfirmId = fournisseur.id!"
            >
              <Trash2 class="h-4 w-4" />
            </Button>
            <div
              v-else
              class="flex items-center gap-1"
            >
              <Button
                variant="ghost"
                size="icon-sm"
                class="text-destructive hover:text-destructive"
                @click="handleDelete(fournisseur.id!)"
                :disabled="deleteMutation.isPending.value"
              >
                <CheckCircle2 class="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon-sm"
                @click="deleteConfirmId = null"
              >
                <XCircle class="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div
      v-if="!isLoading && filteredFournisseurs.length === 0"
      class="text-center py-12"
    >
      <Truck class="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
      <p class="text-muted-foreground">Aucun fournisseur trouvé</p>
      <Button
        variant="outline"
        class="mt-4"
        @click="handleCreate"
      >
        <Plus class="h-4 w-4 mr-2" />
        Créer un fournisseur
      </Button>
    </div>
  </div>
</template>
