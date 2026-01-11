<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui/button'
import { ShieldX, Home, ArrowLeft } from 'lucide-vue-next'

const router = useRouter()

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push({ name: 'dashboard' })
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background">
    <div class="max-w-md w-full p-8 text-center">
      <div
        class="flex h-16 w-16 items-center justify-center mx-auto rounded-full bg-destructive/10 mb-6"
      >
        <ShieldX class="h-8 w-8 text-destructive" />
      </div>

      <h1 class="text-2xl font-bold mb-2">Acces non autorise</h1>
      <p class="text-muted-foreground mb-8">
        Vous n'avez pas les permissions necessaires pour acceder a cette page. Contactez votre
        administrateur si vous pensez qu'il s'agit d'une erreur.
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
