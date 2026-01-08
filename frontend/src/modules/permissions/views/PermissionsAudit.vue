<script setup lang="ts">
import { usePermissions } from '../composables/usePermissions'
import { Activity } from 'lucide-vue-next'

const { auditLogs } = usePermissions()
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Audit</h1>
      <p class="text-gray-600 mt-1">Historique des actions</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <div class="divide-y divide-gray-100">
        <div
          v-for="log in auditLogs"
          :key="log.id"
          class="p-6 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-start gap-4">
            <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
              <Activity :size="20" class="text-blue-600" />
            </div>
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <div>
                  <span
                    :class="[
                      'px-2 py-1 rounded text-xs font-medium',
                      log.action === 'CREATE' ? 'bg-green-100 text-green-600' :
                      log.action === 'UPDATE' ? 'bg-blue-100 text-blue-600' :
                      log.action === 'DELETE' ? 'bg-red-100 text-red-600' :
                      'bg-gray-100 text-gray-600'
                    ]"
                  >
                    {{ log.action }}
                  </span>
                  <span class="ml-2 text-sm text-gray-600">{{ log.userName }}</span>
                </div>
                <span class="text-xs text-gray-500">{{ log.timestamp.toLocaleString('fr-FR') }}</span>
              </div>
              <p class="text-sm text-gray-600 mt-1">
                {{ log.resource }}: {{ log.resourceId }}
              </p>
              <p class="text-xs text-gray-400 mt-1">IP: {{ log.ip }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
