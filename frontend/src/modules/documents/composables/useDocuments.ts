import { computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useDocumentsStore } from '../stores/documentsStore'
import { useDocumentsApi } from './useDocumentsApi'
import type { DocumentResponse } from '@/api/documents'

export function useDocuments() {
  const store = useDocumentsStore()
  const { filter, viewMode, selectedDocumentId, hasActiveFilters } = storeToRefs(store)

  const {
    documents,
    allDocuments,
    myDocuments,
    sharedWithMe,
    publicDocuments,
    categories,
    categoryTree,
    isLoading,
    isLoadingDocuments,
    isSearching,
    isLoadingCategories,
    isUploading,
    isUpdating,
    isDeleting,
    documentsError,
    uploadDocument,
    updateDocument,
    deleteDocument,
    createCategory,
    shareDocument,
    revokeShare,
    setSearchQuery,
    setCategory,
    refetchDocuments,
    formatFileSize,
    formatDate,
    formatDateTime,
  } = useDocumentsApi()

  // Sync store filter with API
  watch(
    () => filter.value.search,
    (search) => setSearchQuery(search || ''),
    { immediate: true }
  )

  watch(
    () => filter.value.categoryId,
    (categoryId) => setCategory(categoryId),
    { immediate: true }
  )

  // Helpers
  const getStatusColor = (isPublic?: boolean): string => {
    if (isPublic) {
      return 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30'
    }
    return 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/30'
  }

  const getTypeIcon = (mimeType?: string): string => {
    if (!mimeType) return 'File'
    if (mimeType.includes('pdf')) return 'FileText'
    if (mimeType.includes('image')) return 'Image'
    if (mimeType.includes('word') || mimeType.includes('document')) return 'FileText'
    if (mimeType.includes('spreadsheet') || mimeType.includes('excel')) return 'Table'
    if (mimeType.includes('presentation') || mimeType.includes('powerpoint')) return 'Presentation'
    if (mimeType.includes('zip') || mimeType.includes('rar') || mimeType.includes('archive'))
      return 'Archive'
    return 'File'
  }

  const getMimeTypeLabel = (mimeType?: string): string => {
    if (!mimeType) return 'Fichier'
    if (mimeType.includes('pdf')) return 'PDF'
    if (mimeType.includes('image')) return 'Image'
    if (mimeType.includes('word') || mimeType.includes('document')) return 'Document'
    if (mimeType.includes('spreadsheet') || mimeType.includes('excel')) return 'Tableur'
    if (mimeType.includes('presentation') || mimeType.includes('powerpoint')) return 'Presentation'
    if (mimeType.includes('zip') || mimeType.includes('rar') || mimeType.includes('archive'))
      return 'Archive'
    return 'Fichier'
  }

  // Stats
  const totalDocuments = computed(
    () => (allDocuments.value as DocumentResponse[] | undefined)?.length ?? 0
  )
  const totalShared = computed(
    () => (sharedWithMe.value as DocumentResponse[] | undefined)?.length ?? 0
  )
  const totalPublic = computed(
    () => (publicDocuments.value as DocumentResponse[] | undefined)?.length ?? 0
  )

  return {
    // Data
    documents,
    allDocuments,
    myDocuments,
    sharedWithMe,
    publicDocuments,
    categories,
    categoryTree,

    // Loading states
    isLoading,
    isLoadingDocuments,
    isSearching,
    isLoadingCategories,
    isUploading,
    isUpdating,
    isDeleting,

    // Error
    documentsError,

    // Store state
    filter,
    viewMode,
    selectedDocumentId,
    hasActiveFilters,

    // Stats
    totalDocuments,
    totalShared,
    totalPublic,

    // Actions
    uploadDocument,
    updateDocument,
    deleteDocument,
    createCategory,
    shareDocument,
    revokeShare,
    refetchDocuments,

    // Store actions
    setFilter: store.setFilter,
    clearFilter: store.clearFilter,
    setSearch: store.setSearch,
    selectDocument: store.selectDocument,
    openUploadModal: store.openUploadModal,
    closeUploadModal: store.closeUploadModal,
    openShareModal: store.openShareModal,
    closeShareModal: store.closeShareModal,
    closeDetailModal: store.closeDetailModal,
    toggleViewMode: store.toggleViewMode,

    // Helpers
    formatFileSize,
    formatDate,
    formatDateTime,
    getStatusColor,
    getTypeIcon,
    getMimeTypeLabel,
  }
}
