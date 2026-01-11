<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore'
import { Button } from '@/components/ui/button'

const authStore = useAuthStore()
</script>

<template>
  <div class="min-h-screen p-8">
    <header class="mb-8 flex items-center justify-between">
      <h1 class="text-2xl font-bold">Dashboard</h1>
      <div class="flex items-center gap-4">
        <span class="text-sm text-muted-foreground">
          {{ authStore.userProfile?.sub }}
        </span>
        <Button
          variant="outline"
          size="sm"
          @click="authStore.logout()"
        >
          Deconnexion
        </Button>
      </div>
    </header>

    <main>
      <div class="rounded-lg border bg-card p-6 text-card-foreground shadow-sm">
        <h2 class="mb-4 text-lg font-semibold">Bienvenue sur le dashboard</h2>
        <p class="text-muted-foreground">
          Cette page est protegee et necessite une authentification.
        </p>

        <div class="mt-6">
          <h3 class="mb-2 font-medium">Informations utilisateur</h3>
          <pre class="overflow-auto rounded bg-muted p-4 text-sm">{{
            JSON.stringify(authStore.userProfile, null, 2)
          }}</pre>
        </div>

        <div class="mt-6">
          <h3 class="mb-2 font-medium">Token d'acces</h3>
          <pre class="overflow-auto rounded bg-muted p-4 text-xs break-all">{{
            authStore.accessToken
          }}</pre>
        </div>
      </div>
    </main>
  </div>
</template>
