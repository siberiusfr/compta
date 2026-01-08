<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  if (authStore.isAuthenticated) {
    router.replace('/dashboard')
    return
  }
  
  await authStore.login()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center p-4">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Redirection vers l'authentification...</p>
    </div>
  </div>
</template>
