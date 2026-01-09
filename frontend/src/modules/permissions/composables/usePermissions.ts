import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { usePermissionsStore } from '../stores/permissionsStore'
import { useUsers } from './useUsers'

export function usePermissions() {
  // Utilise le nouveau composable useUsers avec Vue Query
  const {
    users,
    filteredUsers,
    isLoading: isLoadingUsers,
    userCount,
    activeUsers,
    pendingUsers,
    updateUserStatus,
    searchQuery,
    statusFilter,
    setSearchQuery,
    setStatusFilter,
    isCreating,
    isUpdating,
    isDeleting,
    isEnabling,
    isDisabling,
  } = useUsers()

  // Garde le store pour les roles et groupes (mock pour l'instant)
  const store = usePermissionsStore()
  const {
    roles,
    groups,
    permissions,
    isLoading: isLoadingStore,
    roleCount,
    groupCount,
  } = storeToRefs(store)

  // Combine les etats de chargement
  const isLoading = computed(() => isLoadingUsers.value || isLoadingStore.value)

  // Indicateur d'operation en cours
  const isMutating = computed(
    () =>
      isCreating.value ||
      isUpdating.value ||
      isDeleting.value ||
      isEnabling.value ||
      isDisabling.value
  )

  const formatDate = (date: Date): string => {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date)
  }

  const formatRelativeTime = (date?: Date): string => {
    if (!date) return 'Jamais'

    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / 1000 / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (minutes < 1) return "A l'instant"
    if (minutes < 60) return `Il y a ${minutes} min`
    if (hours < 24) return `Il y a ${hours}h`
    if (days < 7) return `Il y a ${days}j`
    return formatDate(date)
  }

  const getStatusColor = (status: string): string => {
    const colors: Record<string, string> = {
      active: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      inactive: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30',
      pending: 'text-yellow-600 bg-yellow-100 dark:text-yellow-400 dark:bg-yellow-900/30',
      suspended: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
    }
    return colors[status] ?? colors.inactive!
  }

  const getStatusLabel = (status: string): string => {
    const labels: Record<string, string> = {
      active: 'Actif',
      inactive: 'Inactif',
      pending: 'En attente',
      suspended: 'Suspendu',
    }
    return labels[status] ?? status
  }

  const getInitials = (name: string): string => {
    const parts = name.split(' ')
    if (parts.length >= 2 && parts[0] && parts[1]) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase()
    }
    return name.slice(0, 2).toUpperCase()
  }

  return {
    // Utilisateurs (depuis Vue Query)
    users,
    filteredUsers,
    userCount,
    activeUsers,
    pendingUsers,

    // Roles et groupes (depuis le store, mock pour l'instant)
    roles,
    groups,
    permissions,
    roleCount,
    groupCount,

    // Etats
    isLoading,
    isMutating,

    // Filtres
    searchQuery,
    statusFilter,
    setSearchQuery,
    setStatusFilter,

    // Utilitaires
    formatDate,
    formatRelativeTime,
    getStatusColor,
    getStatusLabel,
    getInitials,

    // Actions utilisateurs
    updateUserStatus,

    // Actions groupes (toujours depuis le store)
    addUserToGroup: store.addUserToGroup,
    removeUserFromGroup: store.removeUserFromGroup,
  }
}
