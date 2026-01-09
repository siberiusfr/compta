import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification, NotificationTemplate, SentNotification, NotificationSettings } from '../types/notifications.types'
import {
  mockNotifications,
  mockNotificationTemplates,
  mockSentNotifications,
  mockNotificationSettings
} from '../mock-data/notifications.mock'

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref<Notification[]>(mockNotifications)
  const templates = ref<NotificationTemplate[]>(mockNotificationTemplates)
  const sentNotifications = ref<SentNotification[]>(mockSentNotifications)
  const settings = ref<NotificationSettings>(mockNotificationSettings)
  const isLoading = ref(false)

  const unreadCount = computed(() =>
    notifications.value.filter(n => !n.read && !n.archived).length
  )

  const unreadNotifications = computed(() =>
    notifications.value.filter(n => !n.read && !n.archived)
  )

  const archivedNotifications = computed(() =>
    notifications.value.filter(n => n.archived)
  )

  const urgentNotifications = computed(() =>
    notifications.value.filter(n => n.priority === 'urgent' && !n.read && !n.archived)
  )

  async function fetchNotifications() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      notifications.value = mockNotifications
    } finally {
      isLoading.value = false
    }
  }

  function markAsRead(id: string) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
    }
  }

  function markAllAsRead() {
    notifications.value.forEach(n => {
      if (!n.archived) {
        n.read = true
      }
    })
  }

  function archiveNotification(id: string) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.archived = true
    }
  }

  function deleteNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  function updateSettings(newSettings: Partial<NotificationSettings>) {
    settings.value = { ...settings.value, ...newSettings }
  }

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
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    archiveNotification,
    deleteNotification,
    updateSettings
  }
})
