<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from './stores/authStore'
import { VueQueryDevtools } from '@tanstack/vue-query-devtools'
import ErrorBoundary from '@/core/components/ErrorBoundary.vue'

const authStore = useAuthStore()

const showLoading = computed(() => {
  // Afficher le chargement uniquement sur l'étape initiale
  return authStore.isLoading && !authStore.user
})
</script>

<template>
  <ErrorBoundary>
    <div v-if="showLoading" class="min-h-screen bg-gray-50 flex items-center justify-center">
      <div class="text-center">
        <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
        <p class="text-gray-600 text-lg">Chargement de l'application...</p>
      </div>
    </div>
    <template v-else>
      <RouterView />
      <VueQueryDevtools />
    </template>
  </ErrorBoundary>
</template>
