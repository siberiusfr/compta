<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore'
import LoginButton from '@/components/LoginButton.vue'

const authStore = useAuthStore()
</script>

<template>
  <div class="flex min-h-screen flex-col items-center justify-center gap-8 p-8">
    <h1 class="text-4xl font-bold">Compta</h1>

    <div
      v-if="authStore.isLoading"
      class="flex items-center gap-2"
    >
      <div class="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
      <span class="text-muted-foreground">Chargement...</span>
    </div>

    <template v-else-if="authStore.isAuthenticated">
      <div class="rounded-lg border bg-card p-6 text-card-foreground shadow-sm">
        <h2 class="mb-4 text-xl font-semibold">Bienvenue!</h2>
        <div class="space-y-2 text-sm">
          <p>
            <span class="text-muted-foreground">Utilisateur:</span>
            {{ authStore.userProfile?.sub }}
          </p>
          <p v-if="authStore.userProfile?.email">
            <span class="text-muted-foreground">Email:</span>
            {{ authStore.userProfile.email }}
          </p>
          <p v-if="authStore.userProfile?.name">
            <span class="text-muted-foreground">Nom:</span>
            {{ authStore.userProfile.name }}
          </p>
        </div>
        <button
          class="mt-6 w-full rounded-md bg-destructive px-4 py-2 text-destructive-foreground hover:bg-destructive/90"
          @click="authStore.logout()"
        >
          Se deconnecter
        </button>
      </div>
    </template>

    <template v-else>
      <p class="text-muted-foreground">Connectez-vous pour acceder a l'application</p>
      <LoginButton />
    </template>
  </div>
</template>
