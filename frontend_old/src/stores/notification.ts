// src/stores/notification.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Notification {
  id: number
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  duration?: number
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  let notificationId = 0

  function addNotification(type: Notification['type'], message: string, duration = 5000) {
    const id = notificationId++

    notifications.value.push({
      id,
      type,
      message,
      duration
    })

    if (duration > 0) {
      setTimeout(() => {
        removeNotification(id)
      }, duration)
    }
  }

  function removeNotification(id: number) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  function success(message: string, duration?: number) {
    addNotification('success', message, duration)
  }

  function error(message: string, duration?: number) {
    addNotification('error', message, duration)
  }

  function warning(message: string, duration?: number) {
    addNotification('warning', message, duration)
  }

  function info(message: string, duration?: number) {
    addNotification('info', message, duration)
  }

  return {
    notifications,
    success,
    error,
    warning,
    info,
    removeNotification
  }
})
