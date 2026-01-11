<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { Button } from '@/components/ui/button'

const authStore = useAuthStore()
const isLoggingIn = ref(false)

async function handleLogin() {
  isLoggingIn.value = true
  try {
    await authStore.login()
  } catch (error) {
    console.error('[LoginButton] Login failed:', error)
    isLoggingIn.value = false
  }
  // Note: isLoggingIn reste true car on redirige vers le serveur OAuth
}
</script>

<template>
  <Button
    :disabled="isLoggingIn"
    size="lg"
    @click="handleLogin"
  >
    <template v-if="isLoggingIn">
      <span
        class="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"
      />
      Connexion en cours...
    </template>
    <template v-else> Se connecter </template>
  </Button>
</template>
