<script setup lang="ts">
import { computed } from 'vue'
import { useNotifications } from '../composables/useNotifications'
import { Save } from 'lucide-vue-next'

const { settings, updateSettings } = useNotifications()

const safeSettings = computed(() => {
  if (!settings.value) {
    return {
      emailEnabled: true,
      smsEnabled: false,
      pushEnabled: true,
      frequency: 'immediate' as 'immediate' | 'daily' | 'weekly'
    }
  }
  return settings.value
})
</script>

<template>
  <div v-if="!settings" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des paramètres...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Paramètres de notification</h1>
      <p class="text-gray-600 mt-1">Gérez vos préférences de notification</p>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-6">Canaux de notification</h2>

      <div class="space-y-6">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-medium text-gray-900">Notifications par email</h3>
            <p class="text-sm text-gray-500 mt-1">Recevez les notifications par email</p>
          </div>
          <button
            @click="updateSettings({ emailEnabled: !safeSettings.emailEnabled })"
            :class="[
              'w-12 h-6 rounded-full transition-colors relative',
              safeSettings.emailEnabled ? 'bg-blue-600' : 'bg-gray-300'
            ]"
          >
            <span
              :class="[
                'absolute top-1 w-4 h-4 bg-white rounded-full transition-transform',
                safeSettings.emailEnabled ? 'left-7' : 'left-1'
              ]"
            ></span>
          </button>
        </div>

        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-medium text-gray-900">Notifications SMS</h3>
            <p class="text-sm text-gray-500 mt-1">Recevez les alertes importantes par SMS</p>
          </div>
          <button
            @click="updateSettings({ smsEnabled: !safeSettings.smsEnabled })"
            :class="[
              'w-12 h-6 rounded-full transition-colors relative',
              safeSettings.smsEnabled ? 'bg-blue-600' : 'bg-gray-300'
            ]"
          >
            <span
              :class="[
                'absolute top-1 w-4 h-4 bg-white rounded-full transition-transform',
                safeSettings.smsEnabled ? 'left-7' : 'left-1'
              ]"
            ></span>
          </button>
        </div>

        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-medium text-gray-900">Notifications push</h3>
            <p class="text-sm text-gray-500 mt-1">Recevez les notifications dans le navigateur</p>
          </div>
          <button
            @click="updateSettings({ pushEnabled: !safeSettings.pushEnabled })"
            :class="[
              'w-12 h-6 rounded-full transition-colors relative',
              safeSettings.pushEnabled ? 'bg-blue-600' : 'bg-gray-300'
            ]"
          >
            <span
              :class="[
                'absolute top-1 w-4 h-4 bg-white rounded-full transition-transform',
                safeSettings.pushEnabled ? 'left-7' : 'left-1'
              ]"
            ></span>
          </button>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-6">Fréquence des notifications</h2>

      <div class="space-y-4">
        <label class="flex items-center gap-3 p-4 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors">
          <input
            type="radio"
            :checked="safeSettings.frequency === 'immediate'"
            @change="updateSettings({ frequency: 'immediate' })"
            class="w-4 h-4 text-blue-600"
          />
          <div>
            <p class="font-medium text-gray-900">Immédiat</p>
            <p class="text-sm text-gray-500">Recevez chaque notification dès qu'elle arrive</p>
          </div>
        </label>

        <label class="flex items-center gap-3 p-4 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors">
          <input
            type="radio"
            :checked="safeSettings.frequency === 'daily'"
            @change="updateSettings({ frequency: 'daily' })"
            class="w-4 h-4 text-blue-600"
          />
          <div>
            <p class="font-medium text-gray-900">Quotidien</p>
            <p class="text-sm text-gray-500">Recevez un résumé quotidien des notifications</p>
          </div>
        </label>

        <label class="flex items-center gap-3 p-4 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors">
          <input
            type="radio"
            :checked="safeSettings.frequency === 'weekly'"
            @change="updateSettings({ frequency: 'weekly' })"
            class="w-4 h-4 text-blue-600"
          />
          <div>
            <p class="font-medium text-gray-900">Hebdomadaire</p>
            <p class="text-sm text-gray-500">Recevez un résumé hebdomadaire des notifications</p>
          </div>
        </label>
      </div>
    </div>

    <div class="flex justify-end">
      <button
        class="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
      >
        <Save :size="18" />
        Enregistrer les modifications
      </button>
    </div>
  </div>
</template>
