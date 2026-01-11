<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import { Button } from '@/components/ui/button'
import {
  Folder,
  Plus,
  Trash2,
  Edit,
  Loader2,
  ChevronRight,
  ChevronDown,
  X,
  Save,
  FolderTree,
} from 'lucide-vue-next'
import {
  useGetTree,
  useCreate1,
  useUpdate2,
  useDelete1,
  getGetTreeQueryKey,
  getGetAll2QueryKey,
  type CategoryResponse,
} from '@/api/documents'

const queryClient = useQueryClient()

const { data: categoryTree, isLoading } = useGetTree()

const createMutation = useCreate1({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetTreeQueryKey() })
      queryClient.invalidateQueries({ queryKey: getGetAll2QueryKey() })
    },
  },
})

const updateMutation = useUpdate2({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetTreeQueryKey() })
      queryClient.invalidateQueries({ queryKey: getGetAll2QueryKey() })
    },
  },
})

const deleteMutation = useDelete1({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetTreeQueryKey() })
      queryClient.invalidateQueries({ queryKey: getGetAll2QueryKey() })
    },
  },
})

// Form state
const showForm = ref(false)
const editingId = ref<number | null>(null)
const parentCategoryId = ref<number | undefined>()
const name = ref('')
const description = ref('')

// Expanded state for tree
const expandedIds = ref<Set<number>>(new Set())

const isSubmitting = computed(
  () => createMutation.isPending.value || updateMutation.isPending.value
)
const isDeleting = ref<number | null>(null)

function toggleExpand(id: number) {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id)
  } else {
    expandedIds.value.add(id)
  }
}

function openCreateForm(parentId?: number) {
  editingId.value = null
  parentCategoryId.value = parentId
  name.value = ''
  description.value = ''
  showForm.value = true
}

function openEditForm(category: CategoryResponse) {
  editingId.value = category.id!
  parentCategoryId.value = category.parentCategoryId ?? undefined
  name.value = category.name || ''
  description.value = category.description || ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
  parentCategoryId.value = undefined
  name.value = ''
  description.value = ''
}

async function handleSubmit() {
  if (!name.value.trim()) return

  try {
    if (editingId.value) {
      await updateMutation.mutateAsync({
        id: editingId.value,
        data: {
          name: name.value.trim(),
          description: description.value.trim() || undefined,
          parentCategoryId: parentCategoryId.value,
        },
      })
    } else {
      await createMutation.mutateAsync({
        data: {
          name: name.value.trim(),
          description: description.value.trim() || undefined,
          parentCategoryId: parentCategoryId.value,
        },
      })
    }
    closeForm()
  } catch (error) {
    console.error('Erreur:', error)
  }
}

async function handleDelete(id: number) {
  if (!confirm('Etes-vous sur de vouloir supprimer cette categorie ?')) return

  isDeleting.value = id
  try {
    await deleteMutation.mutateAsync({ id })
  } catch (error) {
    console.error('Erreur lors de la suppression:', error)
  } finally {
    isDeleting.value = null
  }
}

function flattenCategories(
  categories: CategoryResponse[],
  level = 0
): Array<{ category: CategoryResponse; level: number }> {
  const result: Array<{ category: CategoryResponse; level: number }> = []
  for (const cat of categories) {
    result.push({ category: cat, level })
    if (cat.children) {
      result.push(...flattenCategories(cat.children as CategoryResponse[], level + 1))
    }
  }
  return result
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <FolderTree class="h-6 w-6" />
          Gestion des categories
        </h1>
        <p class="text-muted-foreground">Organisez vos documents par categories</p>
      </div>
      <Button @click="openCreateForm()">
        <Plus class="h-4 w-4 mr-2" />
        Nouvelle categorie
      </Button>
    </div>

    <!-- Form -->
    <div
      v-if="showForm"
      class="rounded-xl border bg-card p-6"
    >
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold">
          {{ editingId ? 'Modifier la categorie' : 'Nouvelle categorie' }}
        </h2>
        <Button
          variant="ghost"
          size="icon"
          @click="closeForm"
        >
          <X class="h-4 w-4" />
        </Button>
      </div>

      <form
        class="space-y-4"
        @submit.prevent="handleSubmit"
      >
        <div>
          <label class="block text-sm font-medium mb-1">Nom *</label>
          <input
            v-model="name"
            type="text"
            class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
            placeholder="Nom de la categorie"
            required
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-1">Description</label>
          <textarea
            v-model="description"
            rows="2"
            class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring resize-none"
            placeholder="Description optionnelle"
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-1">Categorie parente</label>
          <select
            v-model="parentCategoryId"
            class="w-full px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
          >
            <option :value="undefined">Racine (aucune)</option>
            <option
              v-for="{ category, level } in flattenCategories(
                (categoryTree as CategoryResponse[]) || []
              )"
              :key="category.id"
              :value="category.id"
              :disabled="category.id === editingId"
            >
              {{ '—'.repeat(level) }} {{ category.name }}
            </option>
          </select>
        </div>

        <div class="flex justify-end gap-2">
          <Button
            type="button"
            variant="outline"
            @click="closeForm"
          >
            Annuler
          </Button>
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
            {{ editingId ? 'Enregistrer' : 'Creer' }}
          </Button>
        </div>
      </form>
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
      v-else-if="!categoryTree || categoryTree.length === 0"
      class="text-center py-12"
    >
      <Folder class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucune categorie</p>
      <p class="text-muted-foreground mb-4">
        Creez votre premiere categorie pour organiser vos documents
      </p>
      <Button @click="openCreateForm()">
        <Plus class="h-4 w-4 mr-2" />
        Creer une categorie
      </Button>
    </div>

    <!-- Category tree -->
    <div
      v-else
      class="rounded-xl border bg-card overflow-hidden"
    >
      <div class="divide-y">
        <template
          v-for="category in categoryTree as CategoryResponse[]"
          :key="category.id"
        >
          <!-- Root category -->
          <div class="p-4 hover:bg-muted/30 transition-colors">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <button
                  v-if="category.children && category.children.length > 0"
                  class="p-1 hover:bg-muted rounded"
                  @click="toggleExpand(category.id!)"
                >
                  <ChevronDown
                    v-if="expandedIds.has(category.id!)"
                    class="h-4 w-4"
                  />
                  <ChevronRight
                    v-else
                    class="h-4 w-4"
                  />
                </button>
                <div
                  v-else
                  class="w-6"
                />
                <Folder class="h-5 w-5 text-primary" />
                <div>
                  <p class="font-medium">{{ category.name }}</p>
                  <p
                    v-if="category.description"
                    class="text-sm text-muted-foreground"
                  >
                    {{ category.description }}
                  </p>
                </div>
              </div>
              <div class="flex items-center gap-1">
                <Button
                  variant="ghost"
                  size="icon-sm"
                  title="Ajouter une sous-categorie"
                  @click="openCreateForm(category.id)"
                >
                  <Plus class="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  title="Modifier"
                  @click="openEditForm(category)"
                >
                  <Edit class="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  class="text-destructive hover:text-destructive"
                  title="Supprimer"
                  :disabled="
                    isDeleting === category.id ||
                    (category.children && category.children.length > 0)
                  "
                  @click="handleDelete(category.id!)"
                >
                  <Loader2
                    v-if="isDeleting === category.id"
                    class="h-4 w-4 animate-spin"
                  />
                  <Trash2
                    v-else
                    class="h-4 w-4"
                  />
                </Button>
              </div>
            </div>

            <!-- Children -->
            <div
              v-if="
                category.children && category.children.length > 0 && expandedIds.has(category.id!)
              "
              class="mt-2 ml-8 space-y-2"
            >
              <div
                v-for="child in category.children as CategoryResponse[]"
                :key="child.id"
                class="flex items-center justify-between p-3 border rounded-lg bg-muted/20"
              >
                <div class="flex items-center gap-2">
                  <Folder class="h-4 w-4 text-muted-foreground" />
                  <div>
                    <p class="font-medium text-sm">{{ child.name }}</p>
                    <p
                      v-if="child.description"
                      class="text-xs text-muted-foreground"
                    >
                      {{ child.description }}
                    </p>
                  </div>
                </div>
                <div class="flex items-center gap-1">
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    title="Modifier"
                    @click="openEditForm(child)"
                  >
                    <Edit class="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    class="text-destructive hover:text-destructive"
                    title="Supprimer"
                    :disabled="isDeleting === child.id"
                    @click="handleDelete(child.id!)"
                  >
                    <Loader2
                      v-if="isDeleting === child.id"
                      class="h-4 w-4 animate-spin"
                    />
                    <Trash2
                      v-else
                      class="h-4 w-4"
                    />
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
