import { computed, ref } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  useGetAll1,
  useSearchGet,
  useGetById,
  useUpload,
  useUpdate,
  useDelete,
  useGetDownloadUrl,
  useGetVersions,
  useUploadVersion,
  useGetAll2,
  useCreate1,
  useGetTree,
  useShare,
  useGetSharesByDocument,
  useRevoke,
  useGetMyDocuments,
  useGetSharedWithMe,
  useGetPublic,
  getGetAll1QueryKey,
  getGetMyDocumentsQueryKey,
  type DocumentResponse,
  type DocumentUpdateRequest,
  type DocumentUploadRequest,
  type DocumentShareRequest,
  type SearchGetParams,
} from "@/api/documents"

export function useDocumentsApi() {
  const queryClient = useQueryClient()
  const searchQuery = ref('')
  const selectedCategoryId = ref<number | undefined>()

  // Search params
  const searchParams = computed<SearchGetParams>(() => ({
    query: searchQuery.value || undefined,
    categoryId: selectedCategoryId.value,
  }))

  // Queries
  const {
    data: documents,
    isLoading: isLoadingDocuments,
    error: documentsError,
    refetch: refetchDocuments,
  } = useGetAll1()

  const {
    data: searchResults,
    isLoading: isSearching,
    refetch: executeSearch,
  } = useSearchGet(searchParams, {
    query: {
      enabled: computed(() => !!searchQuery.value || !!selectedCategoryId.value),
    },
  })

  const {
    data: myDocuments,
    isLoading: isLoadingMyDocuments,
    refetch: refetchMyDocuments,
  } = useGetMyDocuments()

  const {
    data: sharedWithMe,
    isLoading: isLoadingSharedWithMe,
  } = useGetSharedWithMe()

  const {
    data: publicDocuments,
    isLoading: isLoadingPublicDocuments,
  } = useGetPublic()

  const {
    data: categories,
    isLoading: isLoadingCategories,
  } = useGetAll2()

  const {
    data: categoryTree,
    isLoading: isLoadingCategoryTree,
  } = useGetTree()

  // Mutations
  const uploadMutation = useUpload({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAll1QueryKey() })
        queryClient.invalidateQueries({ queryKey: getGetMyDocumentsQueryKey() })
      },
    },
  })

  const updateMutation = useUpdate({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAll1QueryKey() })
      },
    },
  })

  const deleteMutation = useDelete({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetAll1QueryKey() })
        queryClient.invalidateQueries({ queryKey: getGetMyDocumentsQueryKey() })
      },
    },
  })

  const createCategoryMutation = useCreate1({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['api', 'documents', 'categories'] })
      },
    },
  })

  const shareMutation = useShare({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['api', 'documents'] })
      },
    },
  })

  const revokeMutation = useRevoke({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['api', 'documents'] })
      },
    },
  })

  // Computed
  const displayedDocuments = computed(() => {
    if (searchQuery.value || selectedCategoryId.value) {
      return (searchResults.value as DocumentResponse[] | undefined) ?? []
    }
    return (documents.value as DocumentResponse[] | undefined) ?? []
  })

  const isLoading = computed(() =>
    isLoadingDocuments.value || isSearching.value
  )

  // Actions
  async function uploadDocument(file: File, metadata: DocumentUploadRequest) {
    return uploadMutation.mutateAsync({
      data: { file, data: metadata },
    })
  }

  async function updateDocument(id: number, data: DocumentUpdateRequest) {
    return updateMutation.mutateAsync({ id, data })
  }

  async function deleteDocument(id: number) {
    return deleteMutation.mutateAsync({ id })
  }

  async function createCategory(name: string, description?: string, parentCategoryId?: number) {
    return createCategoryMutation.mutateAsync({
      data: { name, description, parentCategoryId },
    })
  }

  async function shareDocument(documentId: number, shareData: DocumentShareRequest) {
    return shareMutation.mutateAsync({
      documentId,
      data: shareData,
    })
  }

  async function revokeShare(shareId: number) {
    return revokeMutation.mutateAsync({ shareId })
  }

  function setSearchQuery(query: string) {
    searchQuery.value = query
  }

  function setCategory(categoryId: number | undefined) {
    selectedCategoryId.value = categoryId
  }

  function clearFilters() {
    searchQuery.value = ''
    selectedCategoryId.value = undefined
  }

  // Helpers
  const formatFileSize = (bytes?: number): string => {
    if (!bytes || bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
  }

  const formatDate = (dateString?: string): string => {
    if (!dateString) return '-'
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(new Date(dateString))
  }

  const formatDateTime = (dateString?: string): string => {
    if (!dateString) return '-'
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(dateString))
  }

  return {
    // State
    searchQuery,
    selectedCategoryId,

    // Data
    documents: displayedDocuments,
    allDocuments: documents,
    myDocuments,
    sharedWithMe,
    publicDocuments,
    categories,
    categoryTree,

    // Loading states
    isLoading,
    isLoadingDocuments,
    isSearching,
    isLoadingMyDocuments,
    isLoadingSharedWithMe,
    isLoadingPublicDocuments,
    isLoadingCategories,
    isLoadingCategoryTree,

    // Errors
    documentsError,

    // Mutation states
    isUploading: uploadMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,

    // Actions
    uploadDocument,
    updateDocument,
    deleteDocument,
    createCategory,
    shareDocument,
    revokeShare,
    setSearchQuery,
    setCategory,
    clearFilters,
    refetchDocuments,
    refetchMyDocuments,
    executeSearch,

    // Helpers
    formatFileSize,
    formatDate,
    formatDateTime,
  }
}

// Hook pour un document spécifique
export function useDocument(id: number | (() => number)) {
  const documentId = computed(() => typeof id === 'function' ? id() : id)

  const {
    data: document,
    isLoading,
    error,
    refetch,
  } = useGetById(documentId, {
    query: {
      enabled: computed(() => !!documentId.value),
    },
  })

  const {
    data: versions,
    isLoading: isLoadingVersions,
  } = useGetVersions(documentId, {
    query: {
      enabled: computed(() => !!documentId.value),
    },
  })

  const {
    data: shares,
    isLoading: isLoadingShares,
  } = useGetSharesByDocument(documentId, {
    query: {
      enabled: computed(() => !!documentId.value),
    },
  })

  const {
    data: downloadUrl,
    refetch: fetchDownloadUrl,
  } = useGetDownloadUrl(documentId, {
    query: {
      enabled: false, // Manual fetch only
    },
  })

  const uploadVersionMutation = useUploadVersion()

  async function uploadNewVersion(file: File, changeDescription?: string) {
    return uploadVersionMutation.mutateAsync({
      documentId: documentId.value,
      data: { file, data: changeDescription ? { changeDescription } : undefined },
    })
  }

  async function download() {
    const result = await fetchDownloadUrl()
    if (result.data?.url) {
      window.open(result.data.url, '_blank')
    }
    return result
  }

  return {
    document,
    versions,
    shares,
    downloadUrl,
    isLoading,
    isLoadingVersions,
    isLoadingShares,
    error,
    refetch,
    uploadNewVersion,
    download,
    isUploadingVersion: uploadVersionMutation.isPending,
  }
}
