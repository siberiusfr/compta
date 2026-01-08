<script setup lang="ts">
import { ref } from 'vue'
import { mockNotificationTemplates } from '../mock-data/notifications.mock'
import type { NotificationTemplate } from '../types/notifications.types'
import { Plus, Edit, Trash2, Copy } from 'lucide-vue-next'

const templates = ref<NotificationTemplate[]>(mockNotificationTemplates)

function formatVariable(variable: string): string {
  return `{${variable}}`
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Modèles de notification</h1>
        <p class="text-gray-600 mt-1">Gérez vos modèles de notification</p>
      </div>
      <button class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
        <Plus :size="18" />
        Nouveau modèle
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="template in templates"
        :key="template.id"
        class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start justify-between mb-4">
          <div>
            <h3 class="font-semibold text-gray-900">{{ template.name }}</h3>
            <span
              :class="[
                'inline-block px-2 py-1 rounded-full text-xs font-medium mt-2',
                template.type === 'email' ? 'bg-blue-100 text-blue-600' :
                template.type === 'sms' ? 'bg-green-100 text-green-600' :
                'bg-purple-100 text-purple-600'
              ]"
            >
              {{ template.type.toUpperCase() }}
            </span>
          </div>
          <div class="flex gap-2">
            <button class="p-2 rounded-lg hover:bg-gray-100 transition-colors" title="Modifier">
              <Edit :size="16" class="text-gray-500" />
            </button>
            <button class="p-2 rounded-lg hover:bg-gray-100 transition-colors" title="Dupliquer">
              <Copy :size="16" class="text-gray-500" />
            </button>
            <button class="p-2 rounded-lg hover:bg-red-100 transition-colors" title="Supprimer">
              <Trash2 :size="16" class="text-gray-500 hover:text-red-600" />
            </button>
          </div>
        </div>

        <div class="space-y-3">
          <div>
            <p class="text-xs text-gray-500 font-medium uppercase">Sujet</p>
            <p class="text-sm text-gray-900 mt-1">{{ template.subject }}</p>
          </div>

          <div>
            <p class="text-xs text-gray-500 font-medium uppercase">Contenu</p>
            <p class="text-sm text-gray-600 mt-1 line-clamp-3">{{ template.body }}</p>
          </div>

          <div>
            <p class="text-xs text-gray-500 font-medium uppercase">Variables</p>
            <div class="flex flex-wrap gap-1 mt-1">
              <span
                v-for="variable in template.variables"
                :key="variable"
                class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded text-xs"
              >
                {{ formatVariable(variable) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="border-2 border-dashed border-gray-300 rounded-xl p-6 flex flex-col items-center justify-center text-gray-400 hover:border-blue-400 hover:text-blue-400 transition-colors cursor-pointer">
        <Plus :size="48" class="mb-4" />
        <p class="font-medium">Créer un nouveau modèle</p>
      </div>
    </div>
  </div>
</template>
