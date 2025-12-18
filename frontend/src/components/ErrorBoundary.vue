<template>
  <div v-if="hasError" class="error-boundary">
    <n-result status="error" title="Une erreur est survenue" :description="errorMessage">
      <template #footer>
        <n-space>
          <n-button @click="handleReset">Réinitialiser</n-button>
          <n-button type="primary" @click="router.push('/')">Retour à l'accueil</n-button>
        </n-space>
      </template>
    </n-result>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
const router = useRouter()
const hasError = ref(false)
const errorMessage = ref('')

onErrorCaptured((err: Error) => {
  hasError.value = true
  errorMessage.value = err.message || 'Une erreur inconnue est survenue'
  console.error('Error captured:', err)
  return false
})

function handleReset() {
  hasError.value = false
  errorMessage.value = ''
}
</script>

<style scoped>
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 24px;
}
</style>
