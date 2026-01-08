import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { mockNotifications, mockSentNotifications, mockNotificationSettings } from '../mock-data/notifications.mock'
import type { Notification, NotificationSettings } from '../types/notifications.types'

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref<Notification[]>(mockNotifications)
  const sentNotifications = ref<Notification[]>(mockSentNotifications)
  const settings = ref<NotificationSettings>(mockNotificationSettings)

  const unreadCount = computed(() => 
    notifications.value.filter(n => !n.read).length
  )

  const unreadNotifications = computed(() =>
    notifications.value.filter(n => !n.read)
  )

  function markAsRead(id: string) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
    }
  }

  function markAllAsRead() {
    notifications.value.forEach(n => n.read = true)
  }

  function deleteNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  function updateSettings(newSettings: Partial<NotificationSettings>) {
    settings.value = { ...settings.value, ...newSettings }
  }

  return {
    notifications,
    sentNotifications,
    settings,
    unreadCount,
    unreadNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    updateSettings
  }
})
