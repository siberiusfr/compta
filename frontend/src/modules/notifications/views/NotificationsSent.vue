<script setup lang="ts">
import { computed } from 'vue'
import { useNotifications } from '../composables/useNotifications'
import { formatDate } from '@/shared/utils/format'
import { Send, Trash2 } from 'lucide-vue-next'

const { sentNotifications, deleteNotification } = useNotifications()

const safeSentNotifications = computed(() => {
  if (!sentNotifications.value || !Array.isArray(sentNotifications.value)) {
    return []
  }
  return sentNotifications.value
})
</script>

<template>
  <div v-if="!sentNotifications" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des notifications envoyées...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Notifications envoyées</h1>
      <p class="text-gray-600 mt-1">{{ safeSentNotifications.length }} notification(s) envoyée(s)</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <div class="divide-y divide-gray-100">
        <div
          v-for="notification in safeSentNotifications"
          :key="notification.id"
          class="p-4 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-start gap-4">
            <div class="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center flex-shrink-0">
              <Send :size="20" class="text-gray-600" />
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <h3 class="font-semibold text-gray-900">{{ notification.title }}</h3>
                  <p class="text-sm text-gray-600 mt-1">{{ notification.message }}</p>
                  <p class="text-xs text-gray-500 mt-2">
                    {{ formatDate(notification.createdAt) }}
                  </p>
                </div>
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

        <div v-if="safeSentNotifications.length === 0" class="p-12 text-center">
          <Send :size="48" class="text-gray-300 mx-auto mb-4" />
          <p class="text-gray-500">Aucune notification envoyée</p>
        </div>
      </div>
    </div>
  </div>
</template>

