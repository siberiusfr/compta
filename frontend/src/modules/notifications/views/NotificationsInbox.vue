<script setup lang="ts">
import { ref, computed } from 'vue'
import { useNotifications } from '../composables/useNotifications'
import { Check, Trash2, BellOff } from 'lucide-vue-next'

const {
  notifications,
  unreadCount,
  unreadNotifications,
  markAsRead,
  markAllAsRead,
  deleteNotification
} = useNotifications()

const filteredNotifications = computed(() => {
  if (!notifications.value || !Array.isArray(notifications.value)) {
    return []
  }
  return notifications.value
})

const typeFilter = ref<'all' | 'unread'>('all')

const displayNotifications = computed(() => {
  const items = typeFilter.value === 'unread' 
    ? (unreadNotifications.value || []) 
    : filteredNotifications.value
  
  return items
})

function getTypeColor(type: string) {
  switch (type) {
    case 'info': return 'bg-blue-100 text-blue-600'
    case 'warning': return 'bg-yellow-100 text-yellow-600'
    case 'error': return 'bg-red-100 text-red-600'
    case 'success': return 'bg-green-100 text-green-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}
</script>

<template>
  <div v-if="!notifications" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des notifications...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Boîte de réception</h1>
        <p class="text-gray-600 mt-1">{{ unreadCount }} notification(s) non lue(s)</p>
      </div>
      <button
        v-if="unreadCount > 0"
        @click="markAllAsRead"
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        <Check :size="18" />
        Tout marquer comme lu
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <div class="p-4 border-b border-gray-200">
        <div class="flex gap-2">
          <button
            @click="typeFilter = 'all'"
            :class="[
              'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
              typeFilter === 'all'
                ? 'bg-blue-100 text-blue-600'
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            Toutes
          </button>
          <button
            @click="typeFilter = 'unread'"
            :class="[
              'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
              typeFilter === 'unread'
                ? 'bg-blue-100 text-blue-600'
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            Non lues ({{ unreadCount }})
          </button>
        </div>
      </div>

      <div class="divide-y divide-gray-100">
        <div
          v-for="notification in displayNotifications"
          :key="notification.id"
          :class="[
            'p-4 hover:bg-gray-50 transition-colors',
            !notification.read && 'bg-blue-50'
          ]"
        >
          <div class="flex items-start gap-4">
            <div
              :class="[
                'w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0',
                getTypeColor(notification.type)
              ]"
            >
              <Bell :size="20" />
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <h3 class="font-semibold text-gray-900">{{ notification.title }}</h3>
                  <p class="text-sm text-gray-600 mt-1">{{ notification.message }}</p>
                  <p class="text-xs text-gray-500 mt-2">
                    {{ notification.sender }} · {{ notification.createdAt.toLocaleString('fr-FR') }}
                  </p>
                </div>
                <div class="flex items-center gap-2 flex-shrink-0">
                  <button
                    v-if="!notification.read"
                    @click="markAsRead(notification.id)"
                    class="p-2 rounded-lg hover:bg-gray-200 transition-colors"
                    title="Marquer comme lu"
                  >
                    <Check :size="18" class="text-gray-500" />
                  </button>
                  <button
                    @click="deleteNotification(notification.id)"
                    class="p-2 rounded-lg hover:bg-red-100 transition-colors"
                    title="Supprimer"
                  >
                    <Trash2 :size="18" class="text-gray-500 hover:text-red-600" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="displayNotifications.length === 0" class="p-12 text-center">
          <BellOff :size="48" class="text-gray-300 mx-auto mb-4" />
          <p class="text-gray-500">Aucune notification</p>
        </div>
      </div>
    </div>
  </div>
</template>
