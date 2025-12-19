<template>
  <div class="loading-spinner" :class="{ 'loading-spinner--fullscreen': fullscreen }">
    <n-spin :size="size" :description="text" />
  </div>
</template>

<script setup lang="ts">
interface Props {
  size?: 'small' | 'medium' | 'large'
  text?: string
  fullscreen?: boolean
}

withDefaults(defineProps<Props>(), {
  size: 'medium',
  text: 'Chargement...',
  fullscreen: false,
})
</script>

