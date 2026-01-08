<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const errorMessage = ref<string | null>(null)

onMounted(async () => {
  try {
    await authStore.handleCallback()

    // Rediriger vers la page d'origine ou le dashboard
    const returnUrl = sessionStorage.getItem('auth_return_url') || '/'
    sessionStorage.removeItem('auth_return_url')

    router.replace(returnUrl)
  } catch (error) {
    console.error('[AuthorizedPage] Callback error:', error)
    errorMessage.value = 'Erreur lors de l\'authentification. Veuillez reessayer.'
  }
})
</script>

<template>
  <div class="flex min-h-screen items-center justify-center">
    <div class="text-center">
      <template v-if="errorMessage">
        <div class="rounded-lg border border-destructive/50 bg-destructive/10 p-6">
          <h1 class="mb-2 text-xl font-semibold text-destructive">
            Erreur d'authentification
          </h1>
          <p class="mb-4 text-muted-foreground">
            {{ errorMessage }}
          </p>
          <button
            class="rounded-md bg-primary px-4 py-2 text-primary-foreground hover:bg-primary/90"
            @click="router.push('/')"
          >
            Retour a l'accueil
          </button>
        </div>
      </template>
      <template v-else>
        <div class="flex flex-col items-center gap-4">
          <div
            class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"
          />
          <p class="text-muted-foreground">Authentification en cours...</p>
        </div>
      </template>
    </div>
  </div>
</template>
