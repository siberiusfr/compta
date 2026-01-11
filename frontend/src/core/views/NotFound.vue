<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui/button'
import { Home, ArrowLeft, FileQuestion } from 'lucide-vue-next'

const router = useRouter()

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background">
    <div class="max-w-md w-full p-8 text-center">
      <div class="flex h-20 w-20 items-center justify-center mx-auto rounded-full bg-muted mb-6">
        <FileQuestion class="h-10 w-10 text-muted-foreground" />
      </div>

      <h1 class="text-4xl font-bold mb-2">404</h1>
      <h2 class="text-xl font-semibold mb-4">Page non trouvee</h2>
      <p class="text-muted-foreground mb-8">
        La page que vous recherchez n'existe pas ou a ete deplacee.
      </p>

      <div class="flex items-center justify-center gap-4">
        <Button
          variant="outline"
          @click="goBack"
        >
          <ArrowLeft class="h-4 w-4 mr-2" />
          Retour
        </Button>
        <Button @click="goHome">
          <Home class="h-4 w-4 mr-2" />
          Accueil
        </Button>
      </div>
    </div>
  </div>
</template>
