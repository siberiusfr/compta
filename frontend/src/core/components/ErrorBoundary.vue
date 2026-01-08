<script setup lang="ts">
import { onErrorCaptured, ref } from 'vue'

const error = ref<Error | null>(null)
const showError = ref(false)

onErrorCaptured((err) => {
  console.error('[ErrorBoundary] Erreur capturée:', err)
  error.value = err
  showError.value = true
  return false
})

function dismissError() {
  showError.value = false
  error.value = null
}

function reloadPage() {
  window.location.reload()
}
</script>

<template>
  <div v-if="showError" class="fixed inset-0 z-50 flex items-center justify-center bg-red-50 p-4">
    <div class="max-w-2xl w-full bg-white rounded-xl shadow-lg p-6">
      <div class="flex items-start gap-4">
        <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0">
          <svg class="w-6 h-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        
        <div class="flex-1">
          <h2 class="text-xl font-bold text-gray-900 mb-2">Une erreur s'est produite</h2>
          <p class="text-gray-600 mb-4">
            Une erreur inattendue s'est produite lors du chargement de cette page.
          </p>
          
          <div v-if="error" class="bg-gray-100 rounded-lg p-4 mb-4">
            <p class="text-sm font-mono text-gray-800 break-all">{{ error.message }}</p>
            <details v-if="error.stack" class="mt-2">
              <summary class="cursor-pointer text-sm text-gray-600 hover:text-gray-900">
                Afficher la pile d'appels
              </summary>
              <pre class="mt-2 text-xs text-gray-700 overflow-auto max-h-40">{{ error.stack }}</pre>
            </details>
          </div>
          
          <div class="flex gap-3">
            <button
              @click="dismissError"
              class="px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors font-medium"
            >
              Fermer
            </button>
            <button
              @click="reloadPage"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-medium"
            >
              Recharger la page
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <slot v-else />
</template>
