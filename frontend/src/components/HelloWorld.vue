<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { ref } from 'vue'

defineProps<{ msg: string }>()

const count = ref(0)
</script>

<template>
  <h1 class="text-3xl font-bold underline bg-red-500">
    Hello world!
  </h1>
  <Button>Click me</Button>
</template>

<style scoped>
</style>
