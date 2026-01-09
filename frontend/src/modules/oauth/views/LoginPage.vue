<script setup lang="ts">
import { ref } from 'vue'
import { useAuth } from '../composables/useAuth'
import { Button } from '@/components/ui/button'
import { LogIn, Loader2, AlertCircle } from 'lucide-vue-next'

const { login, isLoading, error } = useAuth()

const isLoginLoading = ref(false)

const handleLogin = async () => {
  isLoginLoading.value = true
  try {
    await login()
  } catch (e) {
    console.error('Login error:', e)
  } finally {
    isLoginLoading.value = false
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="text-center">
      <h2 class="text-2xl font-bold">Connexion</h2>
      <p class="text-muted-foreground mt-2">
        Connectez-vous pour acceder a votre espace
      </p>
    </div>

    <!-- Error Message -->
    <div
      v-if="error"
      class="flex items-center gap-3 p-4 rounded-lg bg-destructive/10 text-destructive"
    >
      <AlertCircle class="h-5 w-5 shrink-0" />
      <p class="text-sm">{{ error }}</p>
    </div>

    <!-- Login Button -->
    <Button
      class="w-full"
      size="lg"
      :disabled="isLoginLoading || isLoading"
      @click="handleLogin"
    >
      <Loader2 v-if="isLoginLoading || isLoading" class="h-5 w-5 mr-2 animate-spin" />
      <LogIn v-else class="h-5 w-5 mr-2" />
      Se connecter
    </Button>

    <!-- Help Text -->
    <p class="text-center text-sm text-muted-foreground">
      Vous serez redirige vers la page de connexion securisee
    </p>
  </div>
</template>
