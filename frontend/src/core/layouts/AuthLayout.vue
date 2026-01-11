<script setup lang="ts">
import { Calculator } from 'lucide-vue-next'
</script>

<template>
  <div class="min-h-screen flex">
    <!-- Left side - Branding -->
    <div class="hidden lg:flex lg:w-1/2 bg-primary items-center justify-center">
      <div class="max-w-md text-center text-primary-foreground">
        <div class="flex justify-center mb-8">
          <div
            class="flex h-20 w-20 items-center justify-center rounded-2xl bg-primary-foreground/10"
          >
            <Calculator class="h-12 w-12" />
          </div>
        </div>
        <h1 class="text-4xl font-bold mb-4">Compta</h1>
        <p class="text-lg opacity-90">
          Solution complete de gestion comptable pour les entreprises modernes
        </p>
      </div>
    </div>

    <!-- Right side - Content -->
    <div class="flex-1 flex items-center justify-center p-8 bg-background">
      <div class="w-full max-w-md">
        <!-- Mobile logo -->
        <div class="lg:hidden flex justify-center mb-8">
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-primary">
              <Calculator class="h-7 w-7 text-primary-foreground" />
            </div>
            <span class="text-2xl font-bold">Compta</span>
          </div>
        </div>

        <RouterView />
      </div>
    </div>
  </div>
</template>
