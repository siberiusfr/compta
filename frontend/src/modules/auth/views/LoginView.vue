<template>
  <n-layout style="height: 100vh">
    <n-flex justify="center" align="center" style="height: 100%">
      <n-card style="max-width: 450px;">
        <n-space vertical align="center">
          <n-spin size="large" />
          <n-text>Redirection vers la page de connexion...</n-text>
          <n-alert v-if="errorMessage" type="error" :title="errorMessage" />
        </n-space>
      </n-card>
    </n-flex>
  </n-layout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NLayout, NFlex, NCard, NSpace, NSpin, NText, NAlert } from 'naive-ui'
import { useOAuth2AuthStore } from '@/stores/oauth2Auth'

const router = useRouter()
const authStore = useOAuth2AuthStore()
const errorMessage = ref<string>('')

onMounted(async () => {
  try {
    // Vérifier si déjà authentifié
    const isAuth = await authStore.checkAuth()
    if (isAuth) {
      router.push({ name: 'accounting' })
      return
    }

    // Rediriger vers OAuth2
    console.log('Redirecting to OAuth2 server...')
    await authStore.login()
  } catch (error) {
    console.error('Login error:', error)
    errorMessage.value = error instanceof Error ? error.message : 'Erreur lors de la redirection vers OAuth2'
  }
})
</script>
