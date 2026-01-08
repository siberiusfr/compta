<script setup lang="ts">
import { computed } from 'vue'
import { useOAuth } from '../composables/useOAuth'
import { formatDate } from '@/shared/utils/format'
import { Shield, Calendar, Trash2 } from 'lucide-vue-next'

const { consents, revokeConsent } = useOAuth()

const safeConsents = computed(() => {
  if (!consents.value || !Array.isArray(consents.value)) {
    return []
  }
  return consents.value
})
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Consentements</h1>
      <p class="text-gray-600 mt-1">Gérez les consentements OAuth de vos applications</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <div class="divide-y divide-gray-100">
        <div
          v-for="consent in safeConsents"
          :key="consent.id"
          class="p-6 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-start justify-between">
            <div class="flex items-start gap-4">
              <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                <Shield :size="20" class="text-blue-600" />
              </div>

              <div>
                <div class="flex items-center gap-2">
                  <h3 class="font-semibold text-gray-900">{{ consent.applicationName }}</h3>
                  <span class="px-2 py-0.5 bg-green-100 text-green-600 rounded text-xs font-medium">
                    Actif
                  </span>
                </div>

                <div class="mt-3 space-y-2">
                  <div class="flex items-center gap-2 text-sm text-gray-600">
                    <Calendar :size="16" class="text-gray-400" />
                    <span>Accordé le {{ formatDate(consent.grantedAt) }}</span>
                  </div>
                  <div class="flex items-center gap-2 text-sm text-gray-600">
                    <Calendar :size="16" class="text-gray-400" />
                    <span>Expire le {{ formatDate(consent.expiresAt) }}</span>
                  </div>
                </div>

                <div class="mt-3">
                  <p class="text-xs text-gray-500 font-medium uppercase mb-1">Permissions accordées</p>
                  <div class="flex flex-wrap gap-1">
                    <span
                        v-for="scope in consent.scopes"
                        :key="scope"
                        class="px-2 py-0.5 bg-blue-100 text-blue-600 rounded text-xs"
                      >
                        {{ scope }}
                      </span>
                  </div>
                </div>
              </div>
            </div>

            <button
              @click="revokeConsent(consent.id)"
              class="p-2 rounded-lg hover:bg-red-100 transition-colors"
              title="Révoquer le consentement"
            >
              <Trash2 :size="18" class="text-gray-500 hover:text-red-600" />
            </button>
          </div>
        </div>

        <div v-if="safeConsents.length === 0" class="p-12 text-center text-gray-500">
          <Shield :size="48" class="text-gray-300 mx-auto mb-4" />
          <p>Aucun consentement accordé</p>
        </div>
      </div>
    </div>
  </div>
</template>
