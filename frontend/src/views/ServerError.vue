<template>
  <n-result
    status="500"
    title="500 Erreur serveur"
    description="Une erreur interne du serveur s'est produite. Veuillez réessayer plus tard."
  >
    <template #footer>
      <n-space>
        <n-button @click="handleRetry">Réessayer</n-button>
        <n-button type="primary" @click="router.push('/')">Retour à l'accueil</n-button>
      </n-space>
    </template>
  </n-result>
</template>

<script setup lang="ts">
const router = useRouter()

function handleRetry() {
  window.location.reload()
}
</script>
