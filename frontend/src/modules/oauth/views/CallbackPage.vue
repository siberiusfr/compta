<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { Button } from '@/components/ui/button'
import { Loader2, AlertCircle, RefreshCw } from 'lucide-vue-next'

const router = useRouter()
const { handleCallback, getReturnUrl, clearReturnUrl, error } = useAuth()

const isProcessing = ref(true)
const callbackError = ref<string | null>(null)

const processCallback = async () => {
  isProcessing.value = true
  callbackError.value = null

  try {
    await handleCallback()
    const returnUrl = getReturnUrl()
    clearReturnUrl()
    router.replace(returnUrl)
  } catch (e) {
    console.error('Callback error:', e)
    callbackError.value = e instanceof Error ? e.message : 'Erreur lors de l\'authentification'
  } finally {
    isProcessing.value = false
  }
}

const retry = () => {
  router.push({ name: 'login' })
}

onMounted(() => {
  processCallback()
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background">
    <div class="max-w-md w-full p-8 text-center">
      <!-- Loading State -->
      <template v-if="isProcessing">
        <Loader2 class="h-12 w-12 mx-auto text-primary animate-spin mb-4" />
        <h2 class="text-xl font-semibold mb-2">Connexion en cours...</h2>
        <p class="text-muted-foreground">
          Veuillez patienter pendant que nous traitons votre authentification
        </p>
      </template>

      <!-- Error State -->
      <template v-else-if="callbackError || error">
        <div class="flex h-12 w-12 items-center justify-center mx-auto rounded-full bg-destructive/10 mb-4">
          <AlertCircle class="h-6 w-6 text-destructive" />
        </div>
        <h2 class="text-xl font-semibold mb-2">Erreur d'authentification</h2>
        <p class="text-muted-foreground mb-6">
          {{ callbackError || error }}
        </p>
        <Button @click="retry">
          <RefreshCw class="h-4 w-4 mr-2" />
          Reessayer
        </Button>
      </template>
    </div>
  </div>
</template>
