<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOAuth2AuthStore } from '@/stores/oauth2Auth'

const router = useRouter()
const authStore = useOAuth2AuthStore()

onMounted(async () => {
  try {
    await authStore.handleCallback()
    // Rediriger vers la page demandée ou le dashboard
    const redirect = router.currentRoute.value.query.redirect as string
    router.push(redirect || '/accounting')
  } catch (error) {
    console.error('Auth callback failed:', error)
    router.push('/login?error=auth_failed')
  }
})
</script>

<template>
  <n-layout style="height: 100vh">
    <n-flex justify="center" align="center" style="height: 100%">
      <n-card style="max-width: 450px;">
        <n-space vertical align="center">
          <n-spin size="large" />
          <n-text>Authentification en cours...</n-text>
        </n-space>
      </n-card>
    </n-flex>
  </n-layout>
</template>
