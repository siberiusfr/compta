<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import { Button } from '@/components/ui/button'
import { Tag, Plus, Trash2, Loader2, X, Save, Search } from 'lucide-vue-next'
import {
  useGetAll,
  useCreate,
  useDelete2,
  getGetAllQueryKey,
  type TagResponse,
} from '@/api/documents'

const queryClient = useQueryClient()

const { data: tags, isLoading } = useGetAll()

const createMutation = useCreate({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllQueryKey() })
    },
  },
})

const deleteMutation = useDelete2({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAllQueryKey() })
    },
  },
})

// Form state
const showForm = ref(false)
const name = ref('')
const searchQuery = ref('')
const isDeleting = ref<number | null>(null)

const isSubmitting = computed(() => createMutation.isPending.value)

const filteredTags = computed(() => {
  if (!tags.value) return []
  if (!searchQuery.value) return tags.value as TagResponse[]

  const query = searchQuery.value.toLowerCase()
  return (tags.value as TagResponse[]).filter((tag) => tag.name?.toLowerCase().includes(query))
})

function openForm() {
  name.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  name.value = ''
}

async function handleSubmit() {
  if (!name.value.trim()) return

  try {
    await createMutation.mutateAsync({
      data: { name: name.value.trim() },
    })
    closeForm()
  } catch (error) {
    console.error('Erreur:', error)
  }
}

async function handleDelete(id: number) {
  if (!confirm('Etes-vous sur de vouloir supprimer ce tag ?')) return

  isDeleting.value = id
  try {
    await deleteMutation.mutateAsync({ id })
  } catch (error) {
    console.error('Erreur lors de la suppression:', error)
  } finally {
    isDeleting.value = null
  }
}

function formatDate(dateString?: string): string {
  if (!dateString) return '-'
  return new Intl.DateTimeFormat('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(dateString))
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Tag class="h-6 w-6" />
          Gestion des tags
        </h1>
        <p class="text-muted-foreground">Gerez les tags pour classer vos documents</p>
      </div>
      <Button @click="openForm">
        <Plus class="h-4 w-4 mr-2" />
        Nouveau tag
      </Button>
    </div>

    <!-- Form -->
    <div
      v-if="showForm"
      class="rounded-xl border bg-card p-6"
    >
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold">Nouveau tag</h2>
        <Button
          variant="ghost"
          size="icon"
          @click="closeForm"
        >
          <X class="h-4 w-4" />
        </Button>
      </div>

      <form
        class="flex gap-4"
        @submit.prevent="handleSubmit"
      >
        <input
          v-model="name"
          type="text"
          class="flex-1 px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
          placeholder="Nom du tag"
          required
        />
        <Button
          type="submit"
          :disabled="!name.trim() || isSubmitting"
        >
          <Loader2
            v-if="isSubmitting"
            class="h-4 w-4 mr-2 animate-spin"
          />
          <Save
            v-else
            class="h-4 w-4 mr-2"
          />
          Creer
        </Button>
      </form>
    </div>

    <!-- Search -->
    <div class="relative max-w-sm">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Rechercher un tag..."
        class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
      />
    </div>

    <!-- Loading -->
    <div
      v-if="isLoading"
      class="flex items-center justify-center py-12 text-muted-foreground"
    >
      <Loader2 class="h-6 w-6 animate-spin mr-2" />
      Chargement...
    </div>

    <!-- Empty state -->
    <div
      v-else-if="!tags || tags.length === 0"
      class="text-center py-12"
    >
      <Tag class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucun tag</p>
      <p class="text-muted-foreground mb-4">Creez votre premier tag pour classer vos documents</p>
      <Button @click="openForm">
        <Plus class="h-4 w-4 mr-2" />
        Creer un tag
      </Button>
    </div>

    <!-- No results -->
    <div
      v-else-if="filteredTags.length === 0"
      class="text-center py-12"
    >
      <Search class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucun resultat</p>
      <p class="text-muted-foreground">Aucun tag ne correspond a "{{ searchQuery }}"</p>
    </div>

    <!-- Tags grid -->
    <div
      v-else
      class="grid gap-3 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
    >
      <div
        v-for="tag in filteredTags"
        :key="tag.id"
        class="flex items-center justify-between p-4 rounded-xl border bg-card hover:shadow-sm transition-shadow"
      >
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
            <Tag class="h-5 w-5 text-primary" />
          </div>
          <div>
            <p class="font-medium">{{ tag.name }}</p>
            <p class="text-xs text-muted-foreground">Cree le {{ formatDate(tag.createdAt) }}</p>
          </div>
        </div>
        <Button
          variant="ghost"
          size="icon-sm"
          class="text-destructive hover:text-destructive"
          title="Supprimer"
          :disabled="isDeleting === tag.id"
          @click="handleDelete(tag.id!)"
        >
          <Loader2
            v-if="isDeleting === tag.id"
            class="h-4 w-4 animate-spin"
          />
          <Trash2
            v-else
            class="h-4 w-4"
          />
        </Button>
      </div>
    </div>

    <!-- Stats -->
    <div
      v-if="tags && tags.length > 0"
      class="text-sm text-muted-foreground text-center"
    >
      {{ filteredTags.length }} tag{{ filteredTags.length > 1 ? 's' : '' }}
      <span v-if="searchQuery"> sur {{ tags.length }}</span>
    </div>
  </div>
</template>
