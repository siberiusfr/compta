import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { DocumentResponse, CategoryResponse } from '../api/generated'

export interface DocumentFilter {
  categoryId?: number
  search?: string
  uploadedBy?: string
  tag?: string
}

export const useDocumentsStore = defineStore('documents', () => {
  // UI State
  const filter = ref<DocumentFilter>({})
  const selectedDocumentId = ref<number | null>(null)
  const isUploadModalOpen = ref(false)
  const isDetailModalOpen = ref(false)
  const isShareModalOpen = ref(false)

  // View mode
  const viewMode = ref<'grid' | 'list'>('list')

  // Computed
  const hasActiveFilters = computed(() => {
    return !!(filter.value.categoryId || filter.value.search || filter.value.uploadedBy || filter.value.tag)
  })

  // Actions
  function setFilter(newFilter: Partial<DocumentFilter>) {
    filter.value = { ...filter.value, ...newFilter }
  }

  function clearFilter() {
    filter.value = {}
  }

  function setSearch(search: string) {
    filter.value = { ...filter.value, search: search || undefined }
  }

  function setCategory(categoryId: number | undefined) {
    filter.value = { ...filter.value, categoryId }
  }

  function selectDocument(id: number | null) {
    selectedDocumentId.value = id
    if (id) {
      isDetailModalOpen.value = true
    }
  }

  function openUploadModal() {
    isUploadModalOpen.value = true
  }

  function closeUploadModal() {
    isUploadModalOpen.value = false
  }

  function openShareModal(documentId: number) {
    selectedDocumentId.value = documentId
    isShareModalOpen.value = true
  }

  function closeShareModal() {
    isShareModalOpen.value = false
  }

  function closeDetailModal() {
    isDetailModalOpen.value = false
    selectedDocumentId.value = null
  }

  function toggleViewMode() {
    viewMode.value = viewMode.value === 'grid' ? 'list' : 'grid'
  }

  return {
    // State
    filter,
    selectedDocumentId,
    isUploadModalOpen,
    isDetailModalOpen,
    isShareModalOpen,
    viewMode,

    // Computed
    hasActiveFilters,

    // Actions
    setFilter,
    clearFilter,
    setSearch,
    setCategory,
    selectDocument,
    openUploadModal,
    closeUploadModal,
    openShareModal,
    closeShareModal,
    closeDetailModal,
    toggleViewMode,
  }
})
