<template>
  <n-result
    status="403"
    title="403 Accès refusé"
    description="Vous n'avez pas les permissions nécessaires pour accéder à cette page."
  >
    <template #footer>
      <n-space>
        <n-button @click="router.back()">Retour</n-button>
        <n-button type="primary" @click="router.push('/')">Retour à l'accueil</n-button>
      </n-space>
    </template>
  </n-result>
</template>

<script setup lang="ts">
const router = useRouter()
</script>
