<template>
  <n-result status="404" title="404 Page introuvable" description="La page que vous recherchez n'existe pas.">
    <template #footer>
      <n-button @click="router.push('/')">Retour à l'accueil</n-button>
    </template>
  </n-result>
</template>

<script setup lang="ts">
const router = useRouter()
</script>
