<script setup lang="ts">
import { ref } from 'vue'
import { useDocuments } from '../composables/useDocuments'
import { useDocument } from '../composables/useDocumentsApi'
import { Button } from '@/components/ui/button'
import {
  FileText,
  Plus,
  Search,
  Filter,
  Download,
  Eye,
  Trash2,
  Share2,
  Calendar,
  Tag,
  Folder,
  LayoutGrid,
  LayoutList,
  Loader2,
  X,
  Upload
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'
import type { DocumentResponse } from '../api/generated'

const {
  documents,
  categories,
  isLoading,
  isDeleting,
  filter,
  viewMode,
  hasActiveFilters,
  formatDate,
  formatFileSize,
  getStatusColor,
  getMimeTypeLabel,
  setSearch,
  setFilter,
  clearFilter,
  deleteDocument,
  toggleViewMode,
  openUploadModal,
  selectDocument,
  openShareModal,
  refetchDocuments,
} = useDocuments()

const searchInput = ref('')
const confirmDeleteId = ref<number | null>(null)

function handleSearch() {
  setSearch(searchInput.value)
}

function handleCategoryChange(categoryId: number | undefined) {
  setFilter({ categoryId })
}

async function handleDelete(doc: DocumentResponse) {
  if (confirmDeleteId.value === doc.id) {
    try {
      await deleteDocument(doc.id!)
      confirmDeleteId.value = null
    } catch (error) {
      console.error('Erreur lors de la suppression:', error)
    }
  } else {
    confirmDeleteId.value = doc.id!
    setTimeout(() => {
      if (confirmDeleteId.value === doc.id) {
        confirmDeleteId.value = null
      }
    }, 3000)
  }
}

async function handleDownload(doc: DocumentResponse) {
  if (doc.downloadUrl) {
    window.open(doc.downloadUrl, '_blank')
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <FileText class="h-6 w-6" />
          Documents
        </h1>
        <p class="text-muted-foreground">
          Gerez tous vos documents
        </p>
      </div>
      <Button @click="openUploadModal">
        <Upload class="h-4 w-4 mr-2" />
        Nouveau document
      </Button>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4 flex-wrap">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          v-model="searchInput"
          type="text"
          placeholder="Rechercher un document..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- Category filter -->
      <select
        :value="filter.categoryId"
        class="px-3 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        @change="handleCategoryChange(($event.target as HTMLSelectElement).value ? Number(($event.target as HTMLSelectElement).value) : undefined)"
      >
        <option :value="undefined">Toutes les categories</option>
        <option
          v-for="category in categories"
          :key="category.id"
          :value="category.id"
        >
          {{ category.name }}
        </option>
      </select>

      <Button variant="outline" size="icon" @click="handleSearch">
        <Filter class="h-4 w-4" />
      </Button>

      <Button
        v-if="hasActiveFilters"
        variant="ghost"
        size="sm"
        @click="clearFilter(); searchInput = ''"
      >
        <X class="h-4 w-4 mr-1" />
        Effacer les filtres
      </Button>

      <div class="ml-auto flex items-center gap-1">
        <Button
          variant="ghost"
          size="icon"
          :class="{ 'bg-muted': viewMode === 'list' }"
          @click="toggleViewMode"
        >
          <LayoutList class="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          :class="{ 'bg-muted': viewMode === 'grid' }"
          @click="toggleViewMode"
        >
          <LayoutGrid class="h-4 w-4" />
        </Button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-12 text-muted-foreground">
      <Loader2 class="h-6 w-6 animate-spin mr-2" />
      Chargement...
    </div>

    <!-- Empty State -->
    <div v-else-if="!documents || documents.length === 0" class="text-center py-12">
      <FileText class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucun document</p>
      <p class="text-muted-foreground mb-4">
        {{ hasActiveFilters ? 'Aucun document ne correspond a vos criteres' : 'Ajoutez votre premier document' }}
      </p>
      <Button v-if="!hasActiveFilters" @click="openUploadModal">
        <Plus class="h-4 w-4 mr-2" />
        Ajouter un document
      </Button>
    </div>

    <!-- List View -->
    <div v-else-if="viewMode === 'list'" class="rounded-xl border bg-card overflow-hidden">
      <table class="w-full">
        <thead class="bg-muted/50">
          <tr>
            <th class="text-left p-4 font-medium">Document</th>
            <th class="text-left p-4 font-medium">Categorie</th>
            <th class="text-left p-4 font-medium">Type</th>
            <th class="text-left p-4 font-medium">Taille</th>
            <th class="text-left p-4 font-medium">Date</th>
            <th class="text-right p-4 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr
            v-for="doc in documents"
            :key="doc.id"
            class="hover:bg-muted/30 transition-colors"
          >
            <td class="p-4">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <FileText class="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p class="font-medium">{{ doc.title }}</p>
                  <p class="text-xs text-muted-foreground">{{ doc.fileName }}</p>
                  <div v-if="doc.tags && doc.tags.length > 0" class="flex items-center gap-1 mt-1">
                    <Tag class="h-3 w-3 text-muted-foreground" />
                    <span
                      v-for="tag in doc.tags.slice(0, 3)"
                      :key="tag.id"
                      class="text-xs bg-muted px-1.5 py-0.5 rounded"
                    >
                      {{ tag.name }}
                    </span>
                    <span v-if="doc.tags.length > 3" class="text-xs text-muted-foreground">
                      +{{ doc.tags.length - 3 }}
                    </span>
                  </div>
                </div>
              </div>
            </td>
            <td class="p-4">
              <span v-if="doc.categoryName" class="flex items-center gap-1 text-sm">
                <Folder class="h-4 w-4 text-muted-foreground" />
                {{ doc.categoryName }}
              </span>
              <span v-else class="text-muted-foreground">-</span>
            </td>
            <td class="p-4">
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(doc.isPublic))">
                {{ doc.isPublic ? 'Public' : 'Prive' }}
              </span>
            </td>
            <td class="p-4 text-sm text-muted-foreground">
              {{ formatFileSize(doc.fileSize) }}
            </td>
            <td class="p-4">
              <div class="flex items-center gap-1 text-sm text-muted-foreground">
                <Calendar class="h-4 w-4" />
                {{ formatDate(doc.createdAt) }}
              </div>
            </td>
            <td class="p-4">
              <div class="flex items-center justify-end gap-1">
                <Button variant="ghost" size="icon-sm" title="Voir" @click="selectDocument(doc.id!)">
                  <Eye class="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="icon-sm" title="Partager" @click="openShareModal(doc.id!)">
                  <Share2 class="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  title="Telecharger"
                  :disabled="!doc.downloadUrl"
                  @click="handleDownload(doc)"
                >
                  <Download class="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  :title="confirmDeleteId === doc.id ? 'Confirmer la suppression' : 'Supprimer'"
                  :class="confirmDeleteId === doc.id ? 'bg-destructive text-destructive-foreground' : 'text-destructive hover:text-destructive'"
                  :disabled="isDeleting"
                  @click="handleDelete(doc)"
                >
                  <Loader2 v-if="isDeleting && confirmDeleteId === doc.id" class="h-4 w-4 animate-spin" />
                  <Trash2 v-else class="h-4 w-4" />
                </Button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Grid View -->
    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      <div
        v-for="doc in documents"
        :key="doc.id"
        class="rounded-xl border bg-card p-4 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start gap-3 mb-3">
          <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
            <FileText class="h-6 w-6 text-primary" />
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-medium truncate">{{ doc.title }}</p>
            <p class="text-xs text-muted-foreground truncate">{{ doc.fileName }}</p>
          </div>
        </div>

        <div class="space-y-2 text-sm text-muted-foreground mb-4">
          <div v-if="doc.categoryName" class="flex items-center gap-1">
            <Folder class="h-4 w-4" />
            {{ doc.categoryName }}
          </div>
          <div class="flex items-center gap-1">
            <Calendar class="h-4 w-4" />
            {{ formatDate(doc.createdAt) }}
          </div>
          <div class="flex items-center justify-between">
            <span>{{ formatFileSize(doc.fileSize) }}</span>
            <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(doc.isPublic))">
              {{ doc.isPublic ? 'Public' : 'Prive' }}
            </span>
          </div>
        </div>

        <div v-if="doc.tags && doc.tags.length > 0" class="flex flex-wrap gap-1 mb-3">
          <span
            v-for="tag in doc.tags.slice(0, 3)"
            :key="tag.id"
            class="text-xs bg-muted px-2 py-0.5 rounded"
          >
            {{ tag.name }}
          </span>
        </div>

        <div class="flex items-center gap-1 pt-3 border-t">
          <Button variant="ghost" size="sm" @click="selectDocument(doc.id!)">
            <Eye class="h-4 w-4 mr-1" />
            Voir
          </Button>
          <Button variant="ghost" size="sm" :disabled="!doc.downloadUrl" @click="handleDownload(doc)">
            <Download class="h-4 w-4 mr-1" />
            PDF
          </Button>
          <Button
            variant="ghost"
            size="icon-sm"
            class="ml-auto text-destructive hover:text-destructive"
            @click="handleDelete(doc)"
          >
            <Trash2 class="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
