import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useNotificationsStore } from '../stores/notificationsStore'

export function useNotifications() {
  const store = useNotificationsStore()
  const {
    notifications,
    templates,
    sentNotifications,
    settings,
    isLoading,
    unreadCount,
    unreadNotifications,
    archivedNotifications,
    urgentNotifications
  } = storeToRefs(store)

  const formatDate = (date: Date): string => {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date)
  }

  const formatRelativeTime = (date: Date): string => {
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / 1000 / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (minutes < 1) return 'A l\'instant'
    if (minutes < 60) return `Il y a ${minutes} min`
    if (hours < 24) return `Il y a ${hours}h`
    if (days < 7) return `Il y a ${days}j`
    return formatDate(date)
  }

  const getTypeColor = (type: string): string => {
    const colors: Record<string, string> = {
      info: 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/30',
      warning: 'text-yellow-600 bg-yellow-100 dark:text-yellow-400 dark:bg-yellow-900/30',
      error: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
      success: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30'
    }
    return colors[type] ?? colors.info!
  }

  const getPriorityColor = (priority: string): string => {
    const colors: Record<string, string> = {
      low: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30',
      medium: 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/30',
      high: 'text-orange-600 bg-orange-100 dark:text-orange-400 dark:bg-orange-900/30',
      urgent: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30'
    }
    return colors[priority] ?? colors.low!
  }

  onMounted(() => {
    store.fetchNotifications()
  })

  return {
    notifications,
    templates,
    sentNotifications,
    settings,
    isLoading,
    unreadCount,
    unreadNotifications,
    archivedNotifications,
    urgentNotifications,
    formatDate,
    formatRelativeTime,
    getTypeColor,
    getPriorityColor,
    markAsRead: store.markAsRead,
    markAllAsRead: store.markAllAsRead,
    archiveNotification: store.archiveNotification,
    deleteNotification: store.deleteNotification,
    updateSettings: store.updateSettings
  }
}
