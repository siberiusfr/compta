<script setup lang="ts">
import { computed } from 'vue'
import { useOAuth } from '../composables/useOAuth'
import { RefreshCw, Trash2, Clock, AlertCircle } from 'lucide-vue-next'

const { activeTokens, expiredTokens, revokeToken } = useOAuth()

const safeActiveTokens = computed(() => {
  if (!activeTokens.value || !Array.isArray(activeTokens.value)) {
    return []
  }
  return activeTokens.value
})

const safeExpiredTokens = computed(() => {
  if (!expiredTokens.value || !Array.isArray(expiredTokens.value)) {
    return []
  }
  return expiredTokens.value
})

function getTimeAgo(date: Date): string {
  const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000)
  
  if (seconds < 60) return 'à l\'instant'
  if (seconds < 3600) return `il y a ${Math.floor(seconds / 60)} min`
  if (seconds < 86400) return `il y a ${Math.floor(seconds / 3600)} h`
  return `il y a ${Math.floor(seconds / 86400)} j`
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Tokens OAuth</h1>
      <p class="text-gray-600 mt-1">Gérez vos tokens d'accès OAuth</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <h2 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <RefreshCw :size="20" class="text-green-500" />
          Tokens actifs ({{ safeActiveTokens.length }})
        </h2>

        <div class="space-y-4">
          <div
            v-for="token in safeActiveTokens"
            :key="token.id"
            class="bg-white rounded-xl shadow-sm border border-gray-100 p-6"
          >
            <div class="flex items-start justify-between mb-4">
              <div>
                <h3 class="font-semibold text-gray-900">{{ token.applicationName }}</h3>
                <p class="text-xs text-gray-500 mt-1">ID: {{ token.id }}</p>
              </div>
              <button
                @click="revokeToken(token.id)"
                class="p-2 rounded-lg hover:bg-red-100 transition-colors"
                title="Révoquer"
              >
                <Trash2 :size="16" class="text-gray-500 hover:text-red-600" />
              </button>
            </div>

            <div class="space-y-2">
              <div class="flex items-center gap-2 text-sm">
                <Clock :size="16" class="text-gray-400" />
                <span class="text-gray-600">Expire dans:</span>
                <span class="font-medium text-gray-900">
                  {{ getTimeAgo(token.expiresAt) }}
                </span>
              </div>

              <div v-if="token.lastUsedAt" class="flex items-center gap-2 text-sm">
                <Clock :size="16" class="text-gray-400" />
                <span class="text-gray-600">Dernier usage:</span>
                <span class="font-medium text-gray-900">
                  {{ getTimeAgo(token.lastUsedAt) }}
                </span>
              </div>

              <div>
                <p class="text-xs text-gray-500 font-medium uppercase mb-1">Scopes</p>
                <div class="flex flex-wrap gap-1">
                  <span
                    v-for="scope in token.scopes"
                    :key="scope"
                    class="px-2 py-0.5 bg-blue-100 text-blue-600 rounded text-xs"
                  >
                    {{ scope }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="safeActiveTokens.length === 0" class="text-center py-12 text-gray-500">
            <RefreshCw :size="48" class="text-gray-300 mx-auto mb-4" />
            <p>Aucun token actif</p>
          </div>
        </div>
      </div>

      <div>
        <h2 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <AlertCircle :size="20" class="text-red-500" />
          Tokens expirés ({{ safeExpiredTokens.length }})
        </h2>

        <div class="space-y-4">
          <div
            v-for="token in safeExpiredTokens"
            :key="token.id"
            class="bg-white rounded-xl shadow-sm border border-red-100 p-6"
          >
            <div class="flex items-start justify-between mb-4">
              <div>
                <h3 class="font-semibold text-gray-900">{{ token.applicationName }}</h3>
                <p class="text-xs text-gray-500 mt-1">ID: {{ token.id }}</p>
              </div>
              <span class="px-2 py-1 bg-red-100 text-red-600 rounded text-xs font-medium">
                Expiré
              </span>
            </div>

            <div class="space-y-2">
              <div class="flex items-center gap-2 text-sm">
                <Clock :size="16" class="text-gray-400" />
                <span class="text-gray-600">Expiré depuis:</span>
                <span class="font-medium text-red-600">
                  {{ getTimeAgo(token.expiresAt) }}
                </span>
              </div>

              <div>
                <p class="text-xs text-gray-500 font-medium uppercase mb-1">Scopes</p>
                <div class="flex flex-wrap gap-1">
                  <span
                    v-for="scope in token.scopes"
                    :key="scope"
                    class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded text-xs"
                  >
                    {{ scope }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="safeExpiredTokens.length === 0" class="text-center py-12 text-gray-500">
            <AlertCircle :size="48" class="text-gray-300 mx-auto mb-4" />
            <p>Aucun token expiré</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
