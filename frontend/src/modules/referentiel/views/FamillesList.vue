<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useReferentiel } from '../composables/useReferentiel'
import { Button } from '@/components/ui/button'
import { Folder, Plus, Search, Edit, Trash2, CheckCircle2, XCircle } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const router = useRouter()
const {
  companyId,
  useGetAllFamilles,
  useDeleteFamille,
  invalidateFamilles,
  getStatusColor,
  getStatusLabel,
} = useReferentiel()

const searchQuery = ref('')
const deleteConfirmId = ref<number | null>(null)

const { data: familles, isLoading } = useGetAllFamilles(companyId)

const deleteMutation = useDeleteFamille({
  mutation: {
    onSuccess: () => {
      invalidateFamilles()
      deleteConfirmId.value = null
    },
  },
})

const filteredFamilles = computed(() => {
  if (!familles.value) return []
  if (!searchQuery.value) return familles.value
  const query = searchQuery.value.toLowerCase()
  return familles.value.filter(
    (f) =>
      f.code?.toLowerCase().includes(query) ||
      f.libelle?.toLowerCase().includes(query) ||
      f.description?.toLowerCase().includes(query)
  )
})

function handleCreate() {
  router.push({ name: 'referentiel-familles-create' })
}

function handleEdit(id: number) {
  router.push({ name: 'referentiel-familles-edit', params: { id } })
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
          <Folder class="h-6 w-6" />
          Familles de produits
        </h1>
        <p class="text-muted-foreground">Gérez les catégories de vos produits et services</p>
      </div>
      <Button @click="handleCreate">
        <Plus class="h-4 w-4 mr-2" />
        Nouvelle famille
      </Button>
    </div>

    <!-- Search -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher une famille..."
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
        v-for="famille in filteredFamilles"
        :key="famille.id"
        class="rounded-xl border bg-card p-4 hover:bg-accent/50 transition-colors"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4 flex-1">
            <div class="p-2 rounded-lg bg-primary/10">
              <Folder class="h-5 w-5 text-primary" />
            </div>
            <div class="flex-1">
              <div class="flex items-center gap-3">
                <span class="font-mono font-medium text-sm">{{ famille.code }}</span>
                <span class="font-medium">{{ famille.libelle }}</span>
                <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(famille.actif))">
                  {{ getStatusLabel(famille.actif) }}
                </span>
              </div>
              <p
                v-if="famille.description"
                class="text-sm text-muted-foreground mt-1 line-clamp-1"
              >
                {{ famille.description }}
              </p>
            </div>
          </div>
          <div class="flex items-center gap-1">
            <Button
              variant="ghost"
              size="icon-sm"
              @click="handleEdit(famille.id!)"
            >
              <Edit class="h-4 w-4" />
            </Button>
            <Button
              v-if="deleteConfirmId !== famille.id"
              variant="ghost"
              size="icon-sm"
              class="text-destructive hover:text-destructive"
              @click="deleteConfirmId = famille.id!"
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
                @click="handleDelete(famille.id!)"
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
      v-if="!isLoading && filteredFamilles.length === 0"
      class="text-center py-12"
    >
      <Folder class="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
      <p class="text-muted-foreground">Aucune famille de produits trouvée</p>
      <Button
        variant="outline"
        class="mt-4"
        @click="handleCreate"
      >
        <Plus class="h-4 w-4 mr-2" />
        Créer une famille
      </Button>
    </div>
  </div>
</template>
