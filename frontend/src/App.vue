<script setup lang="ts">
import { VueQueryDevtools } from '@tanstack/vue-query-devtools'
import { Toaster, ConfirmDialog } from '@/shared/components'
</script>

<template>
  <RouterView />
  <Toaster />
  <ConfirmDialog />
  <VueQueryDevtools />
</template>
